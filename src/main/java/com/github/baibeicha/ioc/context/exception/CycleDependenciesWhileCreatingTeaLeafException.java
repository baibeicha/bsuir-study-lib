package com.github.baibeicha.ioc.context.exception;

public class CycleDependenciesWhileCreatingTeaLeafException extends RuntimeException {
    public CycleDependenciesWhileCreatingTeaLeafException(String message) {
        super(message);
    }
}
