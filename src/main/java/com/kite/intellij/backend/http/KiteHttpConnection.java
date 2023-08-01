package com.kite.intellij.backend.http;

import com.intellij.openapi.application.ApplicationManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * The low-level http connection to the kite application.
 *
  */
public interface KiteHttpConnection {
    @Nonnull
    static KiteHttpConnection instance() {
        return ApplicationManager.getApplication().getService(KiteHttpConnection.class);
    }

    /**
     * Executes a GET request to Kite.
     *
     * @param path          The path to build the full URL.
     * @param parameters    The query parameters used for the request
     * @param timeoutConfig
     * @return The response body as string if the response status is 200.
     * @throws IOException         May occur during the http communication
     * @throws HttpStatusException Thrown if the response status is != 200.
     */
    @Nonnull
    String doGet(@Nonnull String path, @Nonnull Map<String, String> parameters, HttpTimeoutConfig timeoutConfig) throws HttpStatusException, HttpRequestFailedException, HttpConnectionUnavailableException;

    /**
     * Executes a POST request to Kite.
     *
     * @param path          The path to build the full URL.
     * @param payload       The body to be send as the request body
     * @param timeoutConfig
     * @return The response body as string if the response status is 200.
     * @throws IOException         May occur during the http communication
     * @throws HttpStatusException Thrown if the response status is != 200.
     */
    @Nonnull
    String doPost(@Nonnull String path, @Nullable String payload, HttpTimeoutConfig timeoutConfig) throws HttpStatusException, HttpRequestFailedException, HttpConnectionUnavailableException;

    /**
     * Performs a PUT operation using the given path and query parameters.
     *
     * @param path          The request url path
     * @param parameters
     * @param payload
     * @param timeoutConfig
     * @return The response as string
     * @throws IOException
     * @throws HttpStatusException
     * @throws URISyntaxException
     */
    @Nonnull
    String doPut(@Nonnull String path, @Nonnull Map<String, String> parameters, @Nullable String payload, HttpTimeoutConfig timeoutConfig) throws HttpStatusException, HttpRequestFailedException, HttpConnectionUnavailableException;

    /**
     * Performs a DELETE request for the given path, query parameters and payload.
     *
     * @param path          The path identifying the resource
     * @param parameters    The query parameters
     * @param payload       The optional payload
     * @param timeoutConfig
     * @return The response body
     * @throws IOException
     * @throws HttpStatusException
     * @throws URISyntaxException
     */
    @Nonnull
    String doDelete(@Nonnull String path, @Nonnull Map<String, String> parameters, @Nullable String payload, HttpTimeoutConfig timeoutConfig) throws HttpStatusException, HttpRequestFailedException, HttpConnectionUnavailableException;
}
