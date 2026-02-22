package com.github.baibeicha.database.context;

import com.github.baibeicha.database.Database;
import com.github.baibeicha.database.annotation.ManyToMany;
import com.github.baibeicha.database.annotation.ManyToOne;
import com.github.baibeicha.database.annotation.OneToMany;
import com.github.baibeicha.database.annotation.OneToOne;
import com.github.baibeicha.database.annotation.Transient;
import com.github.baibeicha.reflection.util.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class EntitySnapshot {
    private final Map<String, Object> fieldStates = new HashMap<>();

    public EntitySnapshot(Object entity, Database db) {
        try {
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (AnnotationUtils.isAnnotated(field, Transient.class) ||
                        AnnotationUtils.isAnnotated(field, OneToMany.class) ||
                        AnnotationUtils.isAnnotated(field, ManyToMany.class)) continue;

                field.setAccessible(true);
                Object value = field.get(entity);

                if (AnnotationUtils.isAnnotated(field, ManyToOne.class)
                        || AnnotationUtils.isAnnotated(field, OneToOne.class)) {
                    fieldStates.put(field.getName(), value != null ? db.getIdValue(value) : null);
                } else {
                    fieldStates.put(field.getName(), value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create snapshot", e);
        }
    }

    public Map<String, Object> getFieldStates() {
        return fieldStates;
    }
}