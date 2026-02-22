package com.github.baibeicha.database.dialect;

@FunctionalInterface
public interface SqlDialect {
    String resolveType(Class<?> javaType, int length);
}
