package com.github.baibeicha.ioc.application.event.listener.listeners;


import com.github.baibeicha.ioc.application.event.AppEvent;
import com.github.baibeicha.ioc.application.event.EventType;
import com.github.baibeicha.ioc.application.event.annotation.ApplicationEventListener;
import com.github.baibeicha.ioc.application.event.events.AppStartedTimeEvent;
import com.github.baibeicha.ioc.application.event.listener.AppEventListener;

@ApplicationEventListener(EventType.APPLICATION_RUNNING)
public class AppStartedTimeEventListener implements AppEventListener {

    @Override
    public void onEvent(AppEvent event) {
        if (event instanceof AppStartedTimeEvent) {
            System.out.println(event.getEventData(Long.class));
        } else {
            System.out.println(event.getEventData(Object.class).toString());
        }
    }
}
