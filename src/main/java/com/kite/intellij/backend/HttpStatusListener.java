package com.kite.intellij.backend;

import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.platform.fs.CanonicalFilePath;

import javax.annotation.Nullable;

/**
 */
@FunctionalInterface
public interface HttpStatusListener {
    /**
     * Invoked if a non-200 http status was returned.
     * /clientapi/editor/event also notifies of HTTP success (i.e. 200) response code.
     *
     * @param statusCode  The status code of the response
     * @param e           The http status exception used to transmit the status code
     * @param path        The path of the file the request was for, if there is any
     * @param requestPath The path of of the HTTP request sent for the HTTP response
     * @return true if the exception could be handled, false otherwise.
     */
    boolean notify(int statusCode, @Nullable HttpStatusException e, @Nullable CanonicalFilePath path, @Nullable String requestPath);
}
