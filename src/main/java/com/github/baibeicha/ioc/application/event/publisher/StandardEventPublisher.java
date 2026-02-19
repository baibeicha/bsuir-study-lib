package com.github.baibeicha.ioc.application.event.publisher;

import com.github.baibeicha.ioc.application.event.AppEvent;
import com.github.baibeicha.ioc.application.event.context.EventContext;

public class StandardEventPublisher implements EventPublisher {

    private final EventContext eventContext;

    public StandardEventPublisher(final EventContext eventContext) {
        this.eventContext = eventContext;
    }

    @Override
    public void publish(AppEvent event) {
        eventContext.registerEvent(event);
    }
}
