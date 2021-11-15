package com.kite.intellij.backend.http;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

public class HttpDeleteWithEntity extends HttpEntityEnclosingRequestBase {
    public HttpDeleteWithEntity(URI uri) {
        this.setURI(uri);
    }

    public String getMethod() {
        return "DELETE";
    }
}
