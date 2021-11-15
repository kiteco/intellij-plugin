package com.kite.intellij.backend.http.test;

import com.intellij.openapi.Disposable;
import com.kite.intellij.backend.http.KiteHttpConnection;
import com.kite.intellij.http.GenericRequestHandler;
import com.kite.intellij.http.GetRequestHandler;
import com.kite.intellij.http.RequestWithBodyHandler;

import javax.annotation.Nullable;
import java.util.List;

public interface MockKiteHttpConnection extends KiteHttpConnection {
    static MockKiteHttpConnection getInstance() {
        return (DelegatingHeadlessKiteHttpConnection) KiteHttpConnection.instance();
    }

    void reset();

    void resetHistory();

    List<String> getHttpRequestStringHistory();

    List<RequestInfo> getHttpRequestHistory();

    MockKiteHttpConnection addGenericRequestHandler(GenericRequestHandler handler, @Nullable Disposable parent);

    MockKiteHttpConnection addGetPathHandler(String pathPrefix, GetRequestHandler handler, @Nullable Disposable parent);

    MockKiteHttpConnection addPostPathHandler(String pathPrefix, RequestWithBodyHandler handler, @Nullable Disposable parent);

    MockKiteHttpConnection addPutPathHandler(String pathPrefix, RequestWithBodyHandler handler, @Nullable Disposable parent);

    MockKiteHttpConnection addDeletePathHandler(String pathPrefix, RequestWithBodyHandler handler, @Nullable Disposable parent);
}
