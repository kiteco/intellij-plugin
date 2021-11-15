package com.kite.intellij.backend.http;

public abstract class KiteHttpException extends Exception {
    protected KiteHttpException() {
    }

    protected KiteHttpException(String message) {
        super(message);
    }

    protected KiteHttpException(String message, Throwable cause) {
        super(message, cause);
    }
}
