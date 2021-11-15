package com.kite.intellij.http;

import com.kite.intellij.backend.http.HttpConnectionUnavailableException;
import com.kite.intellij.backend.http.HttpRequestFailedException;
import com.kite.intellij.backend.http.HttpStatusException;

@FunctionalInterface
public interface RequestWithBodyHandler {
    String handleRequest(String path, String payload) throws HttpStatusException, HttpConnectionUnavailableException, HttpRequestFailedException;
}
