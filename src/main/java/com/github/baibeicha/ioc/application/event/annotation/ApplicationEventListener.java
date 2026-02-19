package com.github.baibeicha.ioc.application.event.annotation;

import com.github.baibeicha.ioc.application.event.EventType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ApplicationEventListener {
    EventType value();
}
