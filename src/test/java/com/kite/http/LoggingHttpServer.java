package com.kite.http;

import com.google.common.collect.Lists;
import fi.iki.elonen.NanoHTTPD;

import javax.annotation.concurrent.GuardedBy;
import java.util.List;
import java.util.Map;

public abstract class LoggingHttpServer extends NanoHTTPD implements AutoCloseable {
    @GuardedBy("requestLock")
    private final List<RequestInfo> requests = Lists.newLinkedList();
    private final Object requestLock = new Object();

    public LoggingHttpServer(int port) {
        super("127.0.0.1", port);
    }

    @Override
    public void close() throws Exception {
        this.stop();
    }

    public String url() {
        return "http://" + getHostname() + ":" + getListeningPort();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parameters, Map<String, String> files) {
        synchronized (requestLock) {
            requests.add(new RequestInfo(uri, method, headers, parameters, files));
        }

        return responseData(uri, method, parameters);
    }

    public List<RequestInfo> getRequests() {
        synchronized (requestLock) {
            return Lists.newArrayList(requests);
        }
    }

    protected abstract Response responseData(String uri, Method method, Map<String, String> parms);

    public class RequestInfo {
        private final String method;
        private final String asString;
        private final String uri;

        public RequestInfo(String uri, Method method, Map<String, String> headers, Map<String, String> params, Map<String, String> files) {
            this.method = method.name();
            this.uri = uri;
            this.asString = String.format("%s %s%s", this.method, this.uri, params.containsKey("NanoHttpd.QUERY_STRING") ? ("?" + params.get("NanoHttpd.QUERY_STRING")) : "");
        }

        public String getUri() {
            return uri;
        }

        public String getMethod() {
            return method;
        }

        public String asString() {
            return asString;
        }
    }
}
