package com.github.baibeicha.ioc.application.event.context;

import com.github.baibeicha.ioc.TeaApplication;
import com.github.baibeicha.ioc.application.event.AppEvent;
import com.github.baibeicha.ioc.application.event.EventType;
import com.github.baibeicha.ioc.application.event.annotation.ApplicationEvent;
import com.github.baibeicha.ioc.application.event.annotation.ApplicationEventListener;
import com.github.baibeicha.ioc.application.event.exception.ApplicationEventListenerEventTypeNotFoundException;
import com.github.baibeicha.ioc.application.event.listener.AppEventListener;
import com.github.baibeicha.reflection.PackageScanner;
import com.github.baibeicha.reflection.util.AnnotationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventContext {

    protected final Class<?> configClass;
    protected final Map<EventType, List<AppEvent>> events;
    private final Map<EventType, List<AppEventListener>> listeners;
    protected final PackageScanner scanner;
    protected final TeaApplication.ContextConfiguration config;

    public EventContext(Class<?> configClass, PackageScanner scanner,
                        TeaApplication.ContextConfiguration config) {
        this.configClass = configClass;
        this.events = new HashMap<>();
        this.listeners = new ConcurrentHashMap<>();
        this.scanner = scanner;
        this.config = config;
    }

    public void start() {
        scanPackages();
    }

    public void registerEvent(AppEvent event) {
        if (AnnotationUtils.isAnnotated(event.getClass(), ApplicationEvent.class)) {
            EventType eventTypeFromAnnotation = AnnotationUtils.findAnnotation(
                    event.getClass(), ApplicationEvent.class
            ).value();
            event.setEventType(eventTypeFromAnnotation);
        }

        List<AppEvent> eventList = events.get(event.getEventType());
        if (eventList == null) {
            eventList = new ArrayList<>();
        }
        eventList.add(event);
        events.put(event.getEventType(), eventList);

        sendEvent(event);
    }

    protected void sendEvent(AppEvent event) {
        listeners.computeIfAbsent(event.getEventType(), k -> new ArrayList<>());
        for (AppEventListener listener : listeners.get(event.getEventType())) {
            listener.onEvent(event);
        }
    }

    public void registerListener(AppEventListener listener) {
        if (AnnotationUtils.isAnnotated(listener.getClass(), ApplicationEventListener.class)) {
            EventType eventType = AnnotationUtils.findAnnotation(
                    listener.getClass(), ApplicationEventListener.class
            ).value();

            List<AppEventListener> listenerList = listeners.get(eventType);
            if (listenerList == null) {
                listenerList = new ArrayList<>();
            }
            listenerList.add(listener);
            listeners.put(eventType, listenerList);
        } else {
            throw new ApplicationEventListenerEventTypeNotFoundException(
                    "@AppEventListener not found on " + listener.getClass() +
                            " can not find EventType"
            );
        }
    }

    public void clear() {
        for (List<AppEvent> eventList : events.values()) {
            eventList.clear();
        }
        events.clear();

        for (List<AppEventListener> listenerList : listeners.values()) {
            listenerList.clear();
        }
        listeners.clear();
    }

    protected void scanPackages() {
        for (String pack : config.packages) {
            scanner.findAllClassesWithAnnotation(pack, ApplicationEventListener.class).stream()
                    .map(this::instantiateListener)
                    .forEach(this::registerListener);
        }
    }

    public List<AppEvent> findEvents(final EventType eventType) {
        List<AppEvent> eventList = events.get(eventType);
        if (eventList == null) {
            return new ArrayList<>();
        }

        for (String pack : config.packages) {
            eventList.addAll(scanner.findAllClassesWithAnnotation(pack, ApplicationEvent.class).stream()
                    .map(this::instantiateEvent)
                    .filter(event -> event.getEventType().equals(eventType))
                    .toList()
            );
        }

        return eventList;
    }

    protected AppEventListener instantiateListener(Class<?> clazz) {
        try {
            return (AppEventListener) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create listener: " + clazz.getName(), e);
        }
    }

    protected AppEvent instantiateEvent(Class<?> clazz) {
        try {
            return (AppEvent) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create event: " + clazz.getName(), e);
        }
    }
}
