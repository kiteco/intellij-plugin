package com.kite.intellij.backend.http;

/**
 * Subclass of {@link HttpStatusException} for the 403 status. 403 in kite means that a file
 * wasn't whitelisted.
 *
  */
public class HttpUnauthorizedException extends HttpStatusException {
    public HttpUnauthorizedException(HttpStatusException e) {
        super("Unauthorized: " + e.getMessage(), 401, e.getBody(), e);
    }
}
