package com.kite.intellij.backend.http.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.Disposable;
import com.kite.intellij.backend.http.HttpTimeoutConfig;
import com.kite.intellij.backend.http.JettyAsyncHttpConnection;
import com.kite.intellij.http.GenericRequestHandler;
import com.kite.intellij.http.GetRequestHandler;
import com.kite.intellij.http.RequestWithBodyHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class HistoryItem {
    public String method;
    public String path;
    public String body;
    public Map<String, String> query;
    public int response_status;
    public String response_body;
}

@SuppressWarnings("ComponentNotRegistered")
public class KiteIntegrationTestHttpConnection extends JettyAsyncHttpConnection implements MockKiteHttpConnection {
    public KiteIntegrationTestHttpConnection() {
        super("localhost", kitedPort());
    }

    @Override
    public void reset() {
        try {
            doPost("/testapi/request-history/reset", null, HttpTimeoutConfig.DefaultTimeout);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected exception while calling reset", e);
        }
    }

    @Override
    public void resetHistory() {
        reset();
    }

    @Override
    public List<String> getHttpRequestStringHistory() {
        return getHttpRequestHistory().stream()
                .map(RequestInfo::toString)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestInfo> getHttpRequestHistory() {
        try {
            String json = doGet("/testapi/request-history", Collections.emptyMap(), HttpTimeoutConfig.DefaultTimeout);
            Gson gson = new GsonBuilder().create();

            HistoryItem[] items = gson.fromJson(json, HistoryItem[].class);
            return Arrays.stream(items).map(i -> new RequestInfo(i.method, i.path, i.query, i.body, i.response_status)).collect(Collectors.toList());

        } catch (Exception e) {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }

    @Override
    public MockKiteHttpConnection addGenericRequestHandler(GenericRequestHandler handler, @Nullable Disposable parent) {
        return this;
    }

    @Override
    public MockKiteHttpConnection addGetPathHandler(String pathPrefix, GetRequestHandler handler, @Nullable Disposable parent) {
        return this;
    }

    @Override
    public MockKiteHttpConnection addPostPathHandler(String pathPrefix, RequestWithBodyHandler handler, @Nullable Disposable parent) {
        return this;
    }

    @Override
    public MockKiteHttpConnection addPutPathHandler(String pathPrefix, RequestWithBodyHandler handler, @Nullable Disposable parent) {
        return this;
    }

    @Override
    public MockKiteHttpConnection addDeletePathHandler(String pathPrefix, RequestWithBodyHandler handler, @Nullable Disposable parent) {
        return this;
    }

    protected static int kitedPort() {
        String port = System.getenv("KITED_TEST_PORT");
        if (port != null) {
            return Integer.parseInt(port);
        }
        // default port used by kited-test
        return 56624;
    }
}
