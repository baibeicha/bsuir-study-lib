package com.github.baibeicha.database;

import com.github.baibeicha.database.annotation.CascadeType;
import com.github.baibeicha.database.annotation.Column;
import com.github.baibeicha.database.annotation.Entity;
import com.github.baibeicha.database.annotation.Id;
import com.github.baibeicha.database.annotation.ManyToMany;
import com.github.baibeicha.database.annotation.ManyToOne;
import com.github.baibeicha.database.annotation.OneToMany;
import com.github.baibeicha.database.annotation.OneToOne;
import com.github.baibeicha.database.annotation.Table;
import com.github.baibeicha.database.annotation.Transient;
import com.github.baibeicha.database.connection.ConnectionManager;
import com.github.baibeicha.database.dialect.MySqlDialect;
import com.github.baibeicha.database.dialect.PostgresDialect;
import com.github.baibeicha.database.dialect.SqlDialect;
import com.github.baibeicha.database.exception.DatabaseException;
import com.github.baibeicha.reflection.util.AnnotationUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Простой ORM фреймворк с поддержкой чистого SQL.
 */
public class Database implements AutoCloseable {

    /**
     * Стандартное количество подключений к бд в пуле.
     */
    public static final int STANDARD_CONNECTION_POOL_SIZE = 10;

    private final ConnectionManager connectionManager;
    private final SqlDialect dialect;

    private final Map<Class<?>, RowMapper<?>> entityMappers = new ConcurrentHashMap<>();
    private final Map<Class<?>, String> tableNameCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Field> idFieldCache = new ConcurrentHashMap<>();

    public Database(DataSource dataSource) throws SQLException {
        this.connectionManager = new ConnectionManager(dataSource, STANDARD_CONNECTION_POOL_SIZE);
        this.dialect = initDialect(dataSource.databaseType());
    }

    public Database(DataSource dataSource, int poolSize) throws SQLException {
        this.connectionManager = new ConnectionManager(dataSource, poolSize);
        this.dialect = initDialect(dataSource.databaseType());
    }

    private SqlDialect initDialect(DatabaseType type) {
        return switch (type) {
            case POSTGRESQL -> new PostgresDialect();
            case MYSQL -> new MySqlDialect();
            default -> throw new UnsupportedOperationException("Unsupported database type: " + type);
        };
    }

    /**
     * Получить сессию бд для работы с Persistence Context.
     * @return сессия бд.
     */
    public Session getSession() {
        return new Session(this, connectionManager.getConnection());
    }

    /**
     * Получить подключение к бд.
     * @return объект подключения к бд.
     * @throws DatabaseException ошибка бд.
     */
    public Connection getConnection() throws DatabaseException {
        return connectionManager.getConnection();
    }

    /**
     * Регистрация маппера для сущности.
     * @param clazz тип сущности.
     * @param mapper маппер сущности.
     */
    public void registerMapper(Class<?> clazz, RowMapper<?> mapper) {
        entityMappers.put(clazz, mapper);
    }

    /**
     * SELECT запрос к бд.
     * @param sql SQL запрос.
     * @param targetClass тип сущности.
     * @param params параметры для вставки в запрос.
     * @return полученная сущность или null.
     * @throws DatabaseException ошибка бд при более 1 сущности и др.
     */
    public <T> Optional<T> queryForObject(String sql, Class<T> targetClass, Object... params)
            throws DatabaseException {
        try (Connection conn = getConnection()) {
            return queryForObject(conn, sql, targetClass, params);
        } catch (SQLException e) {
            throw new DatabaseException("Error executing queryForObject: " + sql, e);
        }
    }

    /**
     * SELECT запрос к бд.
     * @param sql SQL запрос.
     * @param targetClass тип сущности.
     * @param params параметры для вставки в запрос.
     * @return список полученных сущностей.
     * @throws DatabaseException ошибка бд.
     */
    public <T> List<T> queryForList(String sql, Class<T> targetClass, Object... params)
            throws DatabaseException {
        try (Connection conn = getConnection()) {
            return queryForList(conn, sql, targetClass, params);
        } catch (SQLException e) {
            throw new DatabaseException("Error executing queryForList: " + sql, e);
        }
    }

    /**
     * UPDATE запрос к бд.
     * @param sql SQL запрос.
     * @param params параметры для вставки.
     * @return кол-во изменённых строк.
     * @throws DatabaseException ошибка бд.
     */
    public int update(String sql, Object... params) throws DatabaseException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error executing update: " + sql, e);
        }
    }

    /**
     * @param conn подключение к бд.
     * @param sql SQL запрос.
     * @param targetClass тип сущности.
     * @param params параметры для вставки в запрос.
     * @return полученная сущность или null.
     * @throws DatabaseException ошибка бд при более 1 сущности и др.
     */
    public <T> Optional<T> queryForObject(Connection conn, String sql, Class<T> targetClass, Object... params)
            throws DatabaseException {
        List<T> results = queryForList(conn, sql, targetClass, params);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() > 1) {
            throw new DatabaseException("Query returned more than 1 row");
        }
        return Optional.of(results.getFirst());
    }

    /**
     * @param conn подключение к бд.
     * @param sql SQL запрос.
     * @param targetClass тип сущности.
     * @param params параметры для вставки в запрос.
     * @return список полученных сущностей.
     * @throws DatabaseException ошибка бд.
     */
    public <T> List<T> queryForList(Connection conn, String sql, Class<T> targetClass, Object... params)
            throws DatabaseException {
        List<T> results = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                RowMapper<T> mapper = getMapper(targetClass);
                while (rs.next()) {
                    results.add(mapper.mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Error in queryForList", e);
        }
    }

    /**
     * Добавление сущности в бд.
     * @param conn подключение к бд.
     * @param entity сущность для добавления.
     * @throws DatabaseException ошибка бд.
     */
    public void executeInsert(Connection conn, Object entity) throws DatabaseException {
        try {
            Class<?> clazz = entity.getClass();
            String tableName = getTableName(clazz);
            Field idField = getIdField(clazz);

            List<String> columns = new ArrayList<>();
            List<Object> values = new ArrayList<>();

            for (Field field : clazz.getDeclaredFields()) {
                if (AnnotationUtils.isAnnotated(field, Id.class) || isIgnoredField(field)) {
                    continue;
                }

                field.setAccessible(true);
                Object val = field.get(entity);

                if (AnnotationUtils.isAnnotated(field, ManyToOne.class)
                        || AnnotationUtils.isAnnotated(field, OneToOne.class)) {
                    String joinCol = AnnotationUtils.isAnnotated(field, ManyToOne.class) ?
                            AnnotationUtils.findAnnotation(field, ManyToOne.class).joinColumn() :
                            AnnotationUtils.findAnnotation(field, OneToOne.class).joinColumn();
                    columns.add(joinCol);
                    values.add(val != null ? getIdValue(val) : null);
                } else {
                    columns.add(getColumnName(field));
                    values.add(val);
                }
            }

            StringBuilder sql = new StringBuilder("INSERT INTO ")
                    .append(tableName)
                    .append(" (")
                    .append(String.join(", ", columns))
                    .append(") VALUES (");
            for (int i = 0; i < columns.size(); i++) {
                sql.append(i == 0 ? "?" : ", ?");
            }
            sql.append(")");

            try (PreparedStatement stmt = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
                setParameters(stmt, values.toArray());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Object newId = rs.getObject(1);
                        if (newId instanceof Number) {
                            if (idField.getType() == long.class || idField.getType() == Long.class)
                                newId = ((Number) newId).longValue();
                            else if (idField.getType() == int.class || idField.getType() == Integer.class)
                                newId = ((Number) newId).intValue();
                        }
                        idField.set(entity, newId);
                    }
                }
            }
        } catch (Exception e) {
            throw new DatabaseException("Failed to execute INSERT", e);
        }
    }

    /**
     * Обновление сущности в бд.
     * @param conn подключение к бд.
     * @param entity сущность для изменения.
     * @throws DatabaseException ошибка бд.
     */
    public void executeUpdate(Connection conn, Object entity) throws DatabaseException {
        try {
            Class<?> clazz = entity.getClass();
            String tableName = getTableName(clazz);
            Field idField = getIdField(clazz);
            Object idValue = idField.get(entity);
            String idColName = getIdColumnName(clazz);

            if (idValue == null) {
                throw new DatabaseException("Cannot update entity with null ID");
            }

            List<String> setClauses = new ArrayList<>();
            List<Object> values = new ArrayList<>();

            for (Field field : clazz.getDeclaredFields()) {
                if (AnnotationUtils.isAnnotated(field, Id.class) || isIgnoredField(field)) {
                    continue;
                }

                field.setAccessible(true);
                Object val = field.get(entity);

                if (AnnotationUtils.isAnnotated(field, ManyToOne.class)
                        || AnnotationUtils.isAnnotated(field, OneToOne.class)) {
                    String joinCol = AnnotationUtils.isAnnotated(field, ManyToOne.class) ?
                            AnnotationUtils.findAnnotation(field, ManyToOne.class).joinColumn() :
                            AnnotationUtils.findAnnotation(field, OneToOne.class).joinColumn();
                    setClauses.add(joinCol + " = ?");
                    values.add(val != null ? getIdValue(val) : null);
                } else {
                    setClauses.add(getColumnName(field) + " = ?");
                    values.add(val);
                }
            }

            if (setClauses.isEmpty()) return;

            String sql = "UPDATE " + tableName
                    + " SET " + String.join(", ", setClauses)
                    + " WHERE " + idColName + " = ?";
            values.add(idValue);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                setParameters(stmt, values.toArray());
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new DatabaseException("Failed to execute UPDATE", e);
        }
    }

    /**
     * Удаление объекта
     *
     * @param conn подключение к бд.
     * @param entity сущность для удаления.
     * @throws DatabaseException ошибка бд.
     */
    public void executeDelete(Connection conn, Object entity) throws DatabaseException {
        try {
            Class<?> clazz = entity.getClass();
            String sql = "DELETE FROM " + getTableName(clazz) + " WHERE " + getIdColumnName(clazz) + " = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, getIdValue(entity));
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new DatabaseException("Failed to execute DELETE", e);
        }
    }

    /**
     * Создаёт таблицу в базе данных для @Entity
     *
     * @param entityClass класс модели данных для создания таблицы.
     * @throws DatabaseException если класс не содержит @Entity и при иной ошибке.
     */
    public void createTable(Class<?> entityClass) throws DatabaseException {
        String tableName = getTableName(entityClass);
        List<String> cols = new ArrayList<>();
        List<String> constraints = new ArrayList<>();
        String primaryKey = null;

        for (Field field : entityClass.getDeclaredFields()) {
            if (isIgnoredField(field)) continue;

            if (AnnotationUtils.isAnnotated(field, ManyToOne.class)
                    || AnnotationUtils.isAnnotated(field, OneToOne.class)) {
                boolean isOneToOne = AnnotationUtils.isAnnotated(field, OneToOne.class);
                String joinCol = isOneToOne ?
                        AnnotationUtils.findAnnotation(field, OneToOne.class).joinColumn() :
                        AnnotationUtils.findAnnotation(field, ManyToOne.class).joinColumn();

                CascadeType[] cascades = isOneToOne ?
                        AnnotationUtils.findAnnotation(field, OneToOne.class).cascade() :
                        AnnotationUtils.findAnnotation(field, ManyToOne.class).cascade();

                Class<?> targetClass = field.getType();
                if (targetClass.isPrimitive()
                        || Number.class.isAssignableFrom(targetClass)
                        || targetClass == String.class) {
                    throw new DatabaseException("Relation must point to another @Entity, not primitive ID.");
                }

                String targetTable = getTableName(targetClass);
                String targetIdCol = getIdColumnName(targetClass);
                Field targetIdField = getIdField(targetClass);

                int targetLength = AnnotationUtils.isAnnotated(targetIdField, Column.class) ?
                        AnnotationUtils.findAnnotation(targetIdField, Column.class).length() : 255;

                String sqlType = dialect.resolveType(targetIdField.getType(), targetLength);
                cols.add(joinCol + " " + sqlType + (isOneToOne ? " UNIQUE" : ""));

                StringBuilder fk = new StringBuilder("FOREIGN KEY (")
                        .append(joinCol)
                        .append(") REFERENCES ")
                        .append(targetTable)
                        .append("(")
                        .append(targetIdCol)
                        .append(")");
                if (Arrays.asList(cascades).contains(CascadeType.DELETE)) {
                    fk.append(" ON DELETE CASCADE");
                }
                constraints.add(fk.toString());
            } else {
                String colName = getColumnName(field);

                int length = 255;
                boolean nullable = true;
                boolean unique = false;

                if (AnnotationUtils.isAnnotated(field, Column.class)) {
                    Column col = AnnotationUtils.findAnnotation(field, Column.class);
                    length = col.length();
                    nullable = col.nullable();
                    unique = col.unique();
                }

                String sqlType = dialect.resolveType(field.getType(), length);
                StringBuilder def = new StringBuilder(colName).append(" ").append(sqlType);

                if (!nullable) {
                    def.append(" NOT NULL");
                }
                if (unique) {
                    def.append(" UNIQUE");
                }

                if (AnnotationUtils.isAnnotated(field, Id.class)) {
                    primaryKey = colName;
                }
                cols.add(def.toString());
            }
        }

        if (primaryKey == null) throw new DatabaseException("No @Id found in " + entityClass.getName());

        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + String.join(", ", cols) +
                ", PRIMARY KEY (" + primaryKey + ")" +
                (constraints.isEmpty() ? "" : ", " + String.join(", ", constraints)) + ")";

        update(sql);
    }

    @SuppressWarnings("unchecked")
    private <T> RowMapper<T> getMapper(Class<T> clazz) throws DatabaseException {
        return (RowMapper<T>) entityMappers.computeIfAbsent(clazz, k -> {
            if (!AnnotationUtils.isAnnotated(k, Entity.class))
                throw new RuntimeException("Class is not @Entity: " + k.getName());
            return new AutoRowMapper<>(k, this);
        });
    }

    /**
     * Получает имя таблицы из аннотации @Table или генерирует из имени класса.
     */
    public String getTableName(Class<?> clazz) {
        return tableNameCache.computeIfAbsent(clazz, k -> {
            if (AnnotationUtils.isAnnotated(k, Table.class)) {
                return AnnotationUtils.findAnnotation(k, Table.class).name();
            }
            return k.getSimpleName().toLowerCase();
        });
    }

    String getColumnName(Field field) {
        if (AnnotationUtils.isAnnotated(field, Column.class)) {
            return AnnotationUtils.findAnnotation(field, Column.class).name();
        }
        return field.getName().toLowerCase();
    }

    Field getIdField(Class<?> clazz) {
        return idFieldCache.computeIfAbsent(clazz, k -> {
            for (Field f : k.getDeclaredFields()) {
                if (AnnotationUtils.isAnnotated(f, Id.class)) {
                    f.setAccessible(true);
                    return f;
                }
            }
            throw new RuntimeException("Missing @Id on " + k.getName());
        });
    }

    String getIdColumnName(Class<?> clazz) {
        try {
            Field f = getIdField(clazz);
            return getColumnName(f);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Получение значения ID у сущности.
     * @param entity сущность бд.
     * @return значение ID.
     * @throws DatabaseException ошибка бд.
     */
    public Object getIdValue(Object entity) throws DatabaseException {
        if (entity == null) return null;
        try {
            return getIdField(entity.getClass()).get(entity);
        } catch (IllegalAccessException e) {
            throw new DatabaseException("Could not read @Id", e);
        }
    }

    boolean isIgnoredField(Field field) {
        return AnnotationUtils.isAnnotated(field, Transient.class) ||
                AnnotationUtils.isAnnotated(field, OneToMany.class) ||
                AnnotationUtils.isAnnotated(field, ManyToMany.class);
    }

    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    @Override
    public void close() throws Exception {
        connectionManager.shutdownPool();
    }
}
