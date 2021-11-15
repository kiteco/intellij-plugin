package com.kite.intellij.http;

import com.kite.intellij.backend.http.HttpConnectionUnavailableException;
import com.kite.intellij.backend.http.HttpRequestFailedException;
import com.kite.intellij.backend.http.HttpStatusException;

import java.util.Map;

@FunctionalInterface
public interface GetRequestHandler {
    String handleRequest(String path, Map<String, String> queryParams) throws HttpStatusException, HttpConnectionUnavailableException, HttpRequestFailedException;
}
