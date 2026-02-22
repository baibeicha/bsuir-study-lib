package com.github.baibeicha.database.context;

import com.github.baibeicha.database.Database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenceContext {

    private final Map<String, Object> identityMap = new ConcurrentHashMap<>();
    private final Map<Object, EntityState> entityStates = new ConcurrentHashMap<>();
    private final Map<Object, EntitySnapshot> snapshots = new ConcurrentHashMap<>();

    public Object getFromCache(Class<?> clazz, Object id) {
        return identityMap.get(clazz.getName() + ":" + id);
    }

    public void manage(Object entity, Object id, Database db) {
        identityMap.put(entity.getClass().getName() + ":" + id, entity);
        entityStates.put(entity, EntityState.MANAGED);
        snapshots.put(entity, new EntitySnapshot(entity, db));
    }

    public void scheduleForInsertion(Object entity) {
        entityStates.putIfAbsent(entity, EntityState.NEW);
    }

    public void scheduleForRemoval(Object entity) {
        entityStates.put(entity, EntityState.REMOVED);
    }

    public EntityState getState(Object entity) {
        return entityStates.get(entity);
    }

    public Map<Object, EntityState> getEntityStates() {
        return entityStates;
    }

    public EntitySnapshot getSnapshot(Object entity) {
        return snapshots.get(entity);
    }

    public void clear() {
        identityMap.clear();
        entityStates.clear();
        snapshots.clear();
    }
}