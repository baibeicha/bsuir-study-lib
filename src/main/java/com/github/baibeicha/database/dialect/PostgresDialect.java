package com.github.baibeicha.database.dialect;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PostgresDialect implements SqlDialect {

    @Override
    public String resolveType(Class<?> type, int length) {
        if (type == String.class) {
            return "VARCHAR(" + length + ")";
        }
        if (type == int.class || type == Integer.class) {
            return "INTEGER";
        }
        if (type == long.class || type == Long.class) {
            return "BIGINT";
        }
        if (type == boolean.class || type == Boolean.class) {
            return "BOOLEAN";
        }
        if (type == double.class || type == Double.class) {
            return "DOUBLE PRECISION";
        }
        if (type == LocalDate.class) {
            return "DATE";
        }
        if (type == LocalDateTime.class) {
            return "TIMESTAMP";
        }
        throw new IllegalArgumentException("PostgreSQL Dialect: Unsupported type " + type.getName());
    }
}
