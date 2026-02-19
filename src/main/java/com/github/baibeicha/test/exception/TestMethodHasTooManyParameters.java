package com.github.baibeicha.test.exception;

public class TestMethodHasTooManyParameters extends RuntimeException {
    public TestMethodHasTooManyParameters(String message) {
        super(message);
    }
}
