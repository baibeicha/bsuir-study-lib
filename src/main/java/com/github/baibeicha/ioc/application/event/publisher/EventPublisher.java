package com.github.baibeicha.ioc.application.event.publisher;

import com.github.baibeicha.ioc.application.event.AppEvent;

public interface EventPublisher {
    void publish(AppEvent event);
}
