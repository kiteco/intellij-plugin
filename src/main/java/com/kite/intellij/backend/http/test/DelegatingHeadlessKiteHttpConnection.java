package com.kite.intellij.backend.http.test;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.kite.intellij.backend.http.*;
import com.kite.intellij.http.GenericRequestHandler;
import com.kite.intellij.http.GetRequestHandler;
import com.kite.intellij.http.RequestWithBodyHandler;
import com.kite.intellij.ui.KiteTestUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * This component either delegates to different implementations, depending on the test environment.
 * The mock component is used except when a Kite integration test is executed. This requires a live HTTP
 * connection.
 * <p>
 * The IntelliJ SDK doesn't offer a factory to create an application component. Therefore we're using a delegate pattern
 * to switch between implementations for our different test modes.
 *
  */
@SuppressWarnings("ComponentNotRegistered")
public class DelegatingHeadlessKiteHttpConnection implements KiteHttpConnection, MockKiteHttpConnection, Disposable {
    private final KiteIntegrationTestHttpConnection live;
    private final MockKiteHttpConnectionImpl mock;

    public DelegatingHeadlessKiteHttpConnection() {
        live = new KiteIntegrationTestHttpConnection();
        mock = new MockKiteHttpConnectionImpl();

        Disposer.register(this, live);
        Disposer.register(this, mock);
    }

    @Override
    public void dispose() {
        reset();
    }

    @Override
    public void reset() {
        try {
            mock.reset();
        } catch (Exception ignored) {
        }

        try {
            live.reset();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void resetHistory() {
        try {
            mock.resetHistory();
        } catch (Exception ignored) {
        }

        try {
            live.resetHistory();
        } catch (Exception ignored) {
        }
    }

    @Override
    public List<String> getHttpRequestStringHistory() {
        return getDelegate().getHttpRequestStringHistory();
    }

    @Override
    public List<RequestInfo> getHttpRequestHistory() {
        return getDelegate().getHttpRequestHistory();
    }

    @Override
    public MockKiteHttpConnection addGenericRequestHandler(GenericRequestHandler handler, @org.jetbrains.annotations.Nullable Disposable parent) {
        return getDelegate().addGenericRequestHandler(handler, parent);
    }

    @Override
    public MockKiteHttpConnection addGetPathHandler(String pathPrefix, GetRequestHandler handler, @org.jetbrains.annotations.Nullable Disposable parent) {
        return getDelegate().addGetPathHandler(pathPrefix, handler, parent);
    }

    @Override
    public MockKiteHttpConnection addPostPathHandler(String pathPrefix, RequestWithBodyHandler handler, @org.jetbrains.annotations.Nullable Disposable parent) {
        return getDelegate().addPostPathHandler(pathPrefix, handler, parent);
    }

    @Override
    public MockKiteHttpConnection addPutPathHandler(String pathPrefix, RequestWithBodyHandler handler, @org.jetbrains.annotations.Nullable Disposable parent) {
        return getDelegate().addPutPathHandler(pathPrefix, handler, parent);
    }

    @Override
    public MockKiteHttpConnection addDeletePathHandler(String pathPrefix, RequestWithBodyHandler handler, @org.jetbrains.annotations.Nullable Disposable parent) {
        return getDelegate().addDeletePathHandler(pathPrefix, handler, parent);
    }

    @Override
    @Nonnull
    public String doGet(@Nonnull String path, @Nonnull Map<String, String> parameters, HttpTimeoutConfig timeoutConfig) throws HttpStatusException, HttpRequestFailedException, HttpConnectionUnavailableException {
        return getDelegate().doGet(path, parameters, timeoutConfig);
    }

    @Override
    @Nonnull
    public String doPost(@Nonnull String path, @Nullable String payload, HttpTimeoutConfig timeoutConfig) throws HttpStatusException, HttpRequestFailedException, HttpConnectionUnavailableException {
        return getDelegate().doPost(path, payload, timeoutConfig);
    }

    @Override
    @Nonnull
    public String doPut(@Nonnull String path, @Nonnull Map<String, String> parameters, @Nullable String payload, HttpTimeoutConfig timeoutConfig) throws HttpStatusException, HttpRequestFailedException, HttpConnectionUnavailableException {
        return getDelegate().doPut(path, parameters, payload, timeoutConfig);
    }

    @Override
    @Nonnull
    public String doDelete(@Nonnull String path, @Nonnull Map<String, String> parameters, @Nullable String payload, HttpTimeoutConfig timeoutConfig) throws HttpStatusException, HttpRequestFailedException, HttpConnectionUnavailableException {
        return getDelegate().doDelete(path, parameters, payload, timeoutConfig);
    }

    private MockKiteHttpConnection getDelegate() {
        return KiteTestUtil.isIntegrationTesting() ? live : mock;
    }
}
