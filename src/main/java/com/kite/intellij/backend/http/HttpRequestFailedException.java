package com.kite.intellij.backend.http;

public class HttpRequestFailedException extends KiteHttpException {
    public HttpRequestFailedException() {
    }

    public HttpRequestFailedException(String message) {
        super(message);
    }

    public HttpRequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
