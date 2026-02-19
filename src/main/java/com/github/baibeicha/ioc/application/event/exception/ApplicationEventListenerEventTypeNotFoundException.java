package com.github.baibeicha.ioc.application.event.exception;

public class ApplicationEventListenerEventTypeNotFoundException extends RuntimeException {
  public ApplicationEventListenerEventTypeNotFoundException(String message) {
    super(message);
  }
}
