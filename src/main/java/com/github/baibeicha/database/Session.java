package com.github.baibeicha.database;

import com.github.baibeicha.database.annotation.*;
import com.github.baibeicha.database.context.EntitySnapshot;
import com.github.baibeicha.database.context.EntityState;
import com.github.baibeicha.database.context.PersistenceContext;
import com.github.baibeicha.database.exception.DatabaseException;
import com.github.baibeicha.reflection.util.AnnotationUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Session implements AutoCloseable {

    private final Database db;
    private final Connection connection;
    private final PersistenceContext context;
    private boolean isTransactionActive = false;

    public Session(Database database, Connection connection) {
        this.db = database;
        this.connection = connection;
        this.context = new PersistenceContext();
    }

    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
        isTransactionActive = true;
    }

    public void commit() throws SQLException, DatabaseException {
        if (!isTransactionActive) throw new IllegalStateException("Transaction not active");

        try {
            for (Map.Entry<Object, EntityState> entry : context.getEntityStates().entrySet()) {
                if (entry.getValue() == EntityState.NEW) {
                    Object entity = entry.getKey();
                    db.executeInsert(connection, entity);
                    context.manage(entity, db.getIdValue(entity), db);
                }
            }

            for (Map.Entry<Object, EntityState> entry : context.getEntityStates().entrySet()) {
                if (entry.getValue() == EntityState.MANAGED) {
                    Object entity = entry.getKey();
                    if (isDirty(entity, context.getSnapshot(entity))) {
                        db.executeUpdate(connection, entity);
                        context.manage(entity, db.getIdValue(entity), db);
                    }
                }
            }

            for (Map.Entry<Object, EntityState> entry : context.getEntityStates().entrySet()) {
                if (entry.getValue() == EntityState.REMOVED) {
                    db.executeDelete(connection, entry.getKey());
                }
            }

            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw new DatabaseException("Commit failed", e);
        } finally {
            connection.setAutoCommit(true);
            isTransactionActive = false;
            context.clear();
        }
    }

    public void rollback() throws SQLException {
        if (isTransactionActive) {
            connection.rollback();
            connection.setAutoCommit(true);
            isTransactionActive = false;
            context.clear();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> findById(Class<T> entityClass, Object id) throws DatabaseException {
        try {
            T cached = (T) context.getFromCache(entityClass, id);
            if (cached != null) {
                return Optional.of(cached);
            }
        } catch (Exception ignored) {
        }

        String tableName = entityClass.getAnnotation(Table.class).name();
        String idColumnName = db.getIdColumnName(entityClass);
        String sql = "SELECT * FROM " + tableName + " WHERE " + idColumnName + " = ?";

        Optional<T> result = db.queryForObject(connection, sql, entityClass, id);

        result.ifPresent(entity -> context.manage(entity, id, db));
        return result;
    }

    public void persist(Object entity) throws DatabaseException {
        if (context.getState(entity) == EntityState.MANAGED) return;

        try {
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (AnnotationUtils.isAnnotated(field, ManyToOne.class)
                        || AnnotationUtils.isAnnotated(field, OneToOne.class)) {
                    field.setAccessible(true);
                    Object related = field.get(entity);
                    if (related != null) {
                        CascadeType[] cascades = AnnotationUtils.isAnnotated(field, ManyToOne.class) ?
                                AnnotationUtils.findAnnotation(field, ManyToOne.class).cascade() :
                                AnnotationUtils.findAnnotation(field, OneToOne.class).cascade();
                        if (Arrays.asList(cascades).contains(CascadeType.PERSIST)) {
                            persist(related);
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new DatabaseException(e);
        }

        context.scheduleForInsertion(entity);
    }

    public void remove(Object entity) {
        context.scheduleForRemoval(entity);
    }

    private boolean isDirty(Object entity, EntitySnapshot snapshot) {
        try {
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (AnnotationUtils.isAnnotated(field, Transient.class)
                        || AnnotationUtils.isAnnotated(field, OneToMany.class)
                        || AnnotationUtils.isAnnotated(field, ManyToMany.class)) {
                    continue;
                }

                field.setAccessible(true);
                Object currentValue = field.get(entity);
                Object snapshotValue = snapshot.getFieldStates().get(field.getName());

                if (AnnotationUtils.isAnnotated(field, ManyToOne.class)
                        || AnnotationUtils.isAnnotated(field, OneToOne.class)) {
                    Object currentId = currentValue != null ? db.getIdValue(currentValue) : null;
                    if (!Objects.equals(currentId, snapshotValue)) {
                        return true;
                    }
                } else {
                    if (!Objects.equals(currentValue, snapshotValue)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public void close() throws Exception {
        if (isTransactionActive) {
            rollback();
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
