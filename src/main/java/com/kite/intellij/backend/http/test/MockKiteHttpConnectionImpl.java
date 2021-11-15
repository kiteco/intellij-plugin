package com.kite.intellij.backend.http.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.http.HttpConnectionUnavailableException;
import com.kite.intellij.backend.http.HttpRequestFailedException;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.HttpTimeoutConfig;
import com.kite.intellij.http.GenericRequestHandler;
import com.kite.intellij.http.GetRequestHandler;
import com.kite.intellij.http.RequestWithBodyHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation used in test cases.
 * See {@link MockKiteApiService#enableHttpCalls()} how to activate the http connection with the default
 * {@link com.kite.intellij.backend.KiteApiService}.
 *
  */
@SuppressWarnings("ComponentNotRegistered")
public class MockKiteHttpConnectionImpl implements MockKiteHttpConnection, Disposable {
    private static final Logger LOG = Logger.getInstance("#kite.mockHttp");

    private final List<GenericRequestHandler> genericRequestHandlers = Lists.newLinkedList();
    private final TreeMap<String, GetRequestHandler> getHandlers = Maps.newTreeMap();
    private final TreeMap<String, RequestWithBodyHandler> postHandlers = Maps.newTreeMap();
    private final TreeMap<String, RequestWithBodyHandler> putHandlers = Maps.newTreeMap();
    private final TreeMap<String, RequestWithBodyHandler> deleteHandlers = Maps.newTreeMap();
    private final List<RequestInfo> httpRequestHistory = Lists.newLinkedList();

    public MockKiteHttpConnectionImpl() {
    }

    @Override
    public void dispose() {
        reset();
    }

    @Override
    public void resetHistory() {
        httpRequestHistory.clear();
    }

    @Override
    public synchronized void reset() {
        genericRequestHandlers.clear();
        getHandlers.clear();
        postHandlers.clear();
        putHandlers.clear();
        deleteHandlers.clear();

        resetHistory();
    }

    @Override
    public synchronized List<String> getHttpRequestStringHistory() {
        return httpRequestHistory.stream()
                .map(RequestInfo::toString)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<RequestInfo> getHttpRequestHistory() {
        return Lists.newArrayList(httpRequestHistory);
    }

    public synchronized MockKiteHttpConnection addGenericRequestHandler(GenericRequestHandler handler, @Nullable Disposable parent) {
        this.genericRequestHandlers.add(0, handler);
        if (parent != null) {
            Disposer.register(parent, () -> this.genericRequestHandlers.remove(handler));
        }
        return this;
    }

    public synchronized MockKiteHttpConnection addGetPathHandler(String pathPrefix, GetRequestHandler handler, @Nullable Disposable parent) {
        this.getHandlers.put(pathPrefix, handler);
        if (parent != null) {
            Disposer.register(parent, () -> {
                this.getHandlers.remove(pathPrefix, handler);
            });
        }
        return this;
    }

    public synchronized MockKiteHttpConnection addPostPathHandler(String pathPrefix, RequestWithBodyHandler handler, @Nullable Disposable parent) {
        this.postHandlers.put(pathPrefix, handler);
        if (parent != null) {
            Disposer.register(parent, () -> this.postHandlers.remove(pathPrefix, handler));
        }
        return this;
    }

    public synchronized MockKiteHttpConnection addPutPathHandler(String pathPrefix, RequestWithBodyHandler handler, @Nullable Disposable parent) {
        this.putHandlers.put(pathPrefix, handler);
        if (parent != null) {
            Disposer.register(parent, () -> this.putHandlers.remove(pathPrefix, handler));
        }
        return this;
    }

    public synchronized MockKiteHttpConnection addDeletePathHandler(String pathPrefix, RequestWithBodyHandler handler, @Nullable Disposable parent) {
        this.deleteHandlers.put(pathPrefix, handler);
        if (parent != null) {
            Disposer.register(parent, () -> this.deleteHandlers.remove(pathPrefix, handler));
        }
        return this;
    }

    @Nonnull
    @Override
    public String doGet(@Nonnull String path, @Nonnull Map<String, String> parameters, HttpTimeoutConfig timeoutConfig) throws HttpStatusException, HttpRequestFailedException, HttpConnectionUnavailableException {
        assertNotSwingThread();

        int status = 200;

        try {
            String body = callGenericRequestHandler("GET", path, parameters, null);
            if (body != null) {
                return body;
            }

            SortedMap<String, GetRequestHandler> subMap = getHandlers.subMap(path, path + Character.MAX_VALUE);
            if (!subMap.isEmpty()) {
                String key = subMap.lastKey();
                return getHandlers.get(key).handleRequest(path, parameters);
            }

            //try to match by prefix
            String floorKey = getHandlers.floorKey(path);
            if (floorKey != null && path.startsWith(floorKey)) {
                return getHandlers.get(floorKey).handleRequest(path, parameters);
            }

            throw new HttpStatusException("No handler found for " + path, 404, "");
        } catch (NoSuchElementException e) {
            throw new HttpStatusException("Unhandled test request + path or exception", 404, "", null, true);
        } catch (HttpStatusException e) {
            status = e.getStatusCode();
            throw e;
        } finally {
            synchronized (this) {
                httpRequestHistory.add(new RequestInfo("GET", path, parameters, "", status));
            }
        }
    }

    @Nonnull
    @Override
    public String doPost(@Nonnull String path, @Nullable String payload, HttpTimeoutConfig timeoutConfig) throws HttpStatusException, HttpRequestFailedException, HttpConnectionUnavailableException {
        assertNotSwingThread();

        int status = 200;
        try {
            String body = callGenericRequestHandler("POST", path, Collections.emptyMap(), payload);
            if (body != null) {
                return body;
            }

            return postHandlers.get(postHandlers.subMap(path, path + Character.MAX_VALUE).lastKey()).handleRequest(path, payload);
        } catch (NoSuchElementException e) {
            throw new HttpStatusException("Not available", 404, "", null, true);
        } catch (HttpStatusException e) {
            status = e.getStatusCode();
            throw e;
        } finally {
            synchronized (this) {
                httpRequestHistory.add(new RequestInfo("POST", path, Collections.emptyMap(), payload, status));
            }
        }
    }

    @Nonnull
    @Override
    public String doPut(@Nonnull String path, @Nonnull Map<String, String> parameters, @Nullable String payload, HttpTimeoutConfig timeoutConfig) throws HttpStatusException, HttpRequestFailedException, HttpConnectionUnavailableException {
        assertNotSwingThread();

        int status = 200;
        try {
            String body = callGenericRequestHandler("PUT", path, Collections.emptyMap(), null);
            if (body != null) {
                return body;
            }

            return putHandlers.get(putHandlers.subMap(path, path + Character.MAX_VALUE).lastKey()).handleRequest(path, payload);
        } catch (NoSuchElementException e) {
            throw new HttpStatusException("Not available", 404, "", null, true);
        } catch (HttpStatusException e) {
            status = e.getStatusCode();
            throw e;
        } finally {
            synchronized (this) {
                httpRequestHistory.add(new RequestInfo("PUT", path, Collections.emptyMap(), payload, status));
            }
        }
    }

    @Nonnull
    @Override
    public String doDelete(@Nonnull String path, @Nonnull Map<String, String> parameters, @Nullable String payload, HttpTimeoutConfig timeoutConfig) throws HttpStatusException, HttpRequestFailedException, HttpConnectionUnavailableException {
        assertNotSwingThread();

        int status = 200;
        try {
            String body = callGenericRequestHandler("DELETE", path, Collections.emptyMap(), null);
            if (body != null) {
                return body;
            }

            return deleteHandlers.get(deleteHandlers.subMap(path, path + Character.MAX_VALUE).lastKey()).handleRequest(path, payload);
        } catch (NoSuchElementException e) {
            throw new HttpStatusException("Unhandled test request + path or exception: " + path, 404, "", null, true);
        } catch (HttpStatusException e) {
            status = e.getStatusCode();
            throw e;
        } finally {
            synchronized (this) {
                httpRequestHistory.add(new RequestInfo("DELETE", path, Collections.emptyMap(), payload, status));
            }
        }
    }

    static String asQueryString(@Nonnull Map<String, String> parameters) {
        return parameters.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&"));
    }

    @Nullable
    private String callGenericRequestHandler(String method, String path, Map<String, String> queryParams, @Nullable String body) throws HttpConnectionUnavailableException, HttpRequestFailedException, HttpStatusException {
        for (GenericRequestHandler handler : genericRequestHandlers) {
            try {
                return handler.handleRequest(method, path, queryParams, body);
            } catch (HttpStatusException e) {
                if (e.isFallback()) {
                    continue;
                }
                throw e;
            }
        }

        return null;
    }

    private void assertNotSwingThread() {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            if (LOG.isTraceEnabled()) {
                LOG.debug("HTTP request in UI thread", new Throwable());
            }

            // can be enabled to find all invocation in test cases
            // throw new IllegalStateException("HTTP request in UI thread");
        }
    }

}
