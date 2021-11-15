package com.kite.intellij.http;

import com.kite.intellij.backend.http.HttpConnectionUnavailableException;
import com.kite.intellij.backend.http.HttpRequestFailedException;
import com.kite.intellij.backend.http.HttpStatusException;

import java.util.Map;

@FunctionalInterface
public interface GenericRequestHandler {
    String handleRequest(String method, String path, Map<String, String> queryParams, String body) throws HttpStatusException, HttpConnectionUnavailableException, HttpRequestFailedException;
}
