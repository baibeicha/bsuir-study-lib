package com.github.baibeicha.ioc.context.exception;

public class FailedToDestroyTeaLeafException extends RuntimeException {
    public FailedToDestroyTeaLeafException(String message) {
        super(message);
    }
    public FailedToDestroyTeaLeafException(Throwable cause) {
        super(cause);
    }
}
