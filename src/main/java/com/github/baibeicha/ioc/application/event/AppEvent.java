package com.github.baibeicha.ioc.application.event;

public interface AppEvent {
    <T> T getEventData(Class<T> clazz);
    void setEventData(Object eventData);
    void setEventType(EventType eventType);
    EventType getEventType();
}
