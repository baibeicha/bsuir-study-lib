package com.github.baibeicha.ioc.context.exception;

public class FailedToInitTeaLeafException extends RuntimeException {
    public FailedToInitTeaLeafException(String message) {
        super(message);
    }
    public FailedToInitTeaLeafException(Throwable cause) {
        super(cause);
    }
}
