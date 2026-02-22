package com.github.baibeicha.database;

import com.github.baibeicha.database.annotation.*;
import com.github.baibeicha.reflection.util.AnnotationUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class AutoRowMapper<T> implements RowMapper<T> {

    private final Class<T> entityClass;
    private final Database database;

    public AutoRowMapper(Class<T> entityClass, Database database) {
        this.entityClass = entityClass;
        this.database = database;
    }

    @Override
    public T mapRow(ResultSet rs) throws SQLException {
        try {
            T instance = entityClass.getDeclaredConstructor().newInstance();

            for (Field field : entityClass.getDeclaredFields()) {
                if (AnnotationUtils.isAnnotated(field, Transient.class) ||
                        AnnotationUtils.isAnnotated(field, OneToMany.class) ||
                        AnnotationUtils.isAnnotated(field, ManyToMany.class)) {
                    continue;
                }

                field.setAccessible(true);

                if (AnnotationUtils.isAnnotated(field, ManyToOne.class) ||
                        AnnotationUtils.isAnnotated(field, OneToOne.class)) {

                    String joinColumnName;
                    FetchType fetchType;

                    if (AnnotationUtils.isAnnotated(field, ManyToOne.class)) {
                        ManyToOne m2o = AnnotationUtils.findAnnotation(field, ManyToOne.class);
                        joinColumnName = m2o.joinColumn();
                        fetchType = m2o.fetch();
                    } else {
                        OneToOne o2o = AnnotationUtils.findAnnotation(field, OneToOne.class);
                        joinColumnName = o2o.joinColumn();
                        fetchType = o2o.fetch();
                    }

                    if (fetchType == FetchType.LAZY) {
                        continue;
                    }

                    Object foreignKeyValue = rs.getObject(joinColumnName);

                    if (foreignKeyValue != null) {
                        Class<?> targetClass = field.getType();

                        String targetTable = database.getTableName(targetClass);
                        String targetIdCol = database.getIdColumnName(targetClass);
                        String sql = "SELECT * FROM " + targetTable + " WHERE " + targetIdCol + " = ?";

                        Optional<?> relatedObjectOpt = database.queryForObject(sql, targetClass, foreignKeyValue);

                        if (relatedObjectOpt.isPresent()) {
                            field.set(instance, relatedObjectOpt.get());
                        }
                    }
                    continue;
                }

                String columnName = getColumnName(field);

                if (columnName != null) {
                    Object value = rs.getObject(columnName);

                    if (value != null) {
                        value = convertSqlValueToJavaType(value, field.getType());
                        field.set(instance, value);
                    }
                }
            }
            return instance;

        } catch (Exception e) {
            throw new SQLException("Failed to auto-map row to entity: " + entityClass.getName(), e);
        }
    }

    private Object convertSqlValueToJavaType(Object sqlValue, Class<?> targetType) {
        if (sqlValue instanceof java.sql.Date && targetType == LocalDate.class) {
            return ((java.sql.Date) sqlValue).toLocalDate();
        }
        if (sqlValue instanceof java.sql.Timestamp && targetType == LocalDateTime.class) {
            return ((java.sql.Timestamp) sqlValue).toLocalDateTime();
        }
        if (sqlValue instanceof Number && (targetType == boolean.class || targetType == Boolean.class)) {
            return ((Number) sqlValue).intValue() != 0;
        }
        return sqlValue;
    }

    private String getColumnName(Field field) {
        if (AnnotationUtils.isAnnotated(field, Column.class)) {
            return AnnotationUtils.findAnnotation(field, Column.class).name();
        }
        return field.getName().toLowerCase();
    }
}
