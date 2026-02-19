package com.github.baibeicha.ioc.application.event.events;

import com.github.baibeicha.ioc.application.event.AbstractAppEvent;
import com.github.baibeicha.ioc.application.event.EventType;
import com.github.baibeicha.ioc.application.event.annotation.ApplicationEvent;

@ApplicationEvent(EventType.APPLICATION_RUNNING)
public class AppStartedTimeEvent extends AbstractAppEvent {

    public AppStartedTimeEvent() {
        super(EventType.APPLICATION_RUNNING, System.currentTimeMillis());
    }
}
