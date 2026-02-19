package com.github.baibeicha.ioc.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

public class Dependency {

    private final String name;
    private final Class<?> type;
    private Method method;
    private Field field;

    public Dependency(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    public String name() {
        return name;
    }

    public Class<?> type() {
        return type;
    }

    public Method method() {
        return method;
    }

    public Field field() {
        return field;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setField(Field field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "Dependency [name=" + name + ", type=" + type + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof Dependency other) {
            return name.equals(other.name) && type.equals(other.type);
        } else {
            return false;
        }
    }
}
