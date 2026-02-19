package com.github.baibeicha.test.exception;

public class UnableInvokeTestMethodException extends RuntimeException {
    public UnableInvokeTestMethodException(String message) {
        super(message);
    }
    public UnableInvokeTestMethodException(Throwable cause) { super(cause); }
}
