package com.kite.intellij.backend.http;

/**
 * A exception to signal an unavailable HTTP exception.
 *
  */
public class HttpConnectionUnavailableException extends KiteHttpException {
    public HttpConnectionUnavailableException() {
    }

    public HttpConnectionUnavailableException(String message) {
        super(message);
    }

    public HttpConnectionUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
