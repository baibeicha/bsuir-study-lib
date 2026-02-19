package com.github.baibeicha.ioc.application.event.listener;

import com.github.baibeicha.ioc.application.event.AppEvent;

@FunctionalInterface
public interface AppEventListener {
    void onEvent(AppEvent event);
}
