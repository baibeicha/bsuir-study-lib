package com.github.baibeicha.database.dialect;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MySqlDialect implements SqlDialect {
    @Override
    public String resolveType(Class<?> type, int length) {
        if (type == String.class) {
            return "VARCHAR(" + length + ")";
        }
        if (type == int.class || type == Integer.class) {
            return "INT";
        }
        if (type == long.class || type == Long.class) {
            return "BIGINT";
        }
        if (type == boolean.class || type == Boolean.class) {
            return "TINYINT(1)";
        }
        if (type == double.class || type == Double.class) {
            return "DOUBLE";
        }
        if (type == LocalDate.class) {
            return "DATE";
        }
        if (type == LocalDateTime.class) {
            return "DATETIME";
        }
        throw new IllegalArgumentException("MySQL Dialect: Unsupported type " + type.getName());
    }
}
