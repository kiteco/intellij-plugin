package com.kite.intellij.backend.http;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Thrown by {@link KiteHttpConnection} if a HTTP response was returned with a status != 200.
 *
  */
public class HttpStatusException extends KiteHttpException {
    private final int statusCode;
    private final boolean isFallback;
    @Nonnull
    private final String body;

    public HttpStatusException(@Nonnull HttpStatusException e) {
        this(e.getMessage(), e.getStatusCode(), e.getBody(), e);
    }

    public HttpStatusException(@NonNls String message, int statusCode, @Nullable String body) {
        this(message, statusCode, body, null);
    }

    public HttpStatusException(@NonNls String message, int statusCode, @Nullable String body, Throwable cause) {
        this(message, statusCode, body, cause, false);
    }

    public HttpStatusException(@NonNls String message, int statusCode, @Nullable String body, Throwable cause, boolean isFallback) {
        super(message, cause);

        this.statusCode = statusCode;
        this.body = StringUtils.trimToEmpty(body);
        this.isFallback = isFallback;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ", status: " + statusCode + ", body: " + body.subSequence(0, Math.min(50, body.length()));
    }

    @Nonnull
    public String getBody() {
        return body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isUnauthorizedError401() {
        return statusCode == 401;
    }

    public boolean isNotFoundError404() {
        return statusCode == 404;
    }

    public boolean isForbidden403() {
        return statusCode == 403;
    }

    public boolean isNotImplemented501() {
        return statusCode == 501;
    }

    public boolean isServiceUnavailable503() {
        return statusCode == 503;
    }

    public boolean isFallback() {
        return isFallback;
    }
}
