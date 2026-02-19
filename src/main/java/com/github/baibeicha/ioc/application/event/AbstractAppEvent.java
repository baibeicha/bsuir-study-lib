package com.github.baibeicha.ioc.application.event;

public abstract class AbstractAppEvent implements AppEvent {

    protected EventType eventType;
    protected Object eventData;

    public AbstractAppEvent(EventType eventType, Object eventData) {
        this.eventType = eventType;
        this.eventData = eventData;
    }

    @Override
    public <T> T getEventData(Class<T> clazz) {
        return clazz.cast(eventData);
    }

    @Override
    public void setEventData(Object eventData) {
        this.eventData = eventData;
    }

    @Override
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public EventType getEventType() {
        return eventType;
    }
}
