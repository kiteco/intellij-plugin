package com.kite.intellij.backend.http.test;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class RequestInfo {
    private final String method;
    private final String path;
    private final Map<String, String> queryParameters;
    private final String body;
    private final int responseStatus;

    public RequestInfo(String method, String path, Map<String, String> queryParameters, String body, int responseStatus) {
        this.method = method;
        this.path = path;
        this.queryParameters = queryParameters;
        this.body = body;
        this.responseStatus = responseStatus;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getPathWithQuery() {
        if (queryParameters.isEmpty()) {
            return path;
        }
        return path + "?" + MockKiteHttpConnectionImpl.asQueryString(queryParameters);
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public String getBody() {
        return body;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path, queryParameters, body, responseStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestInfo that = (RequestInfo) o;
        return responseStatus == that.responseStatus &&
                Objects.equals(method, that.method) &&
                Objects.equals(path, that.path) &&
                Objects.equals(queryParameters, that.queryParameters) &&
                Objects.equals(body, that.body);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(method).append(" ").append(path);
        if (!queryParameters.isEmpty()) {
            b.append("?").append(queryParameters.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&")));
        }
        if (body != null && !body.isEmpty()) {
            b.append("\n").append(body);
        }
        return b.toString();
    }
}
