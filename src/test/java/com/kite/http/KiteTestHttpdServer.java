package com.kite.http;

import com.google.common.collect.Maps;
import com.kite.intellij.backend.http.HttpConnectionUnavailableException;
import com.kite.intellij.backend.http.HttpRequestFailedException;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.http.GetRequestHandler;
import com.kite.intellij.http.RequestWithBodyHandler;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

public class KiteTestHttpdServer extends NanoHTTPD {

    private static final String MIME_JSON = "text/json";
    private final TreeMap<String, GetRequestHandler> getHandlers = Maps.newTreeMap();
    private final TreeMap<String, RequestWithBodyHandler> postHandlers = Maps.newTreeMap();
    private final TreeMap<String, RequestWithBodyHandler> putHandlers = Maps.newTreeMap();
    private final TreeMap<String, RequestWithBodyHandler> deleteHandlers = Maps.newTreeMap();

    public KiteTestHttpdServer() {
        this(0);
    }

    public KiteTestHttpdServer(int port) {
        super("127.0.0.1", port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            switch (session.getMethod()) {
                case GET:
                    return goGetRequest(session);
                case POST:
                    return doRequest(session, postHandlers);
                case PUT:
                    return doRequest(session, putHandlers);
                case DELETE:
                    return doRequest(session, deleteHandlers);
            }
        } catch (HttpStatusException e) {
            return newFixedLengthResponse(new Response.IStatus() {
                @Override
                public String getDescription() {
                    return e.getMessage();
                }

                @Override
                public int getRequestStatus() {
                    return e.getStatusCode();
                }
            }, "text/plain", "Error while serving " + session.getUri());
        } catch (Exception e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Exception while serving " + session.getUri() + ": " + e.getMessage());
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "No request available at " + session.getUri());
    }

    public synchronized KiteTestHttpdServer addGetPathHandler(String pathPrefix, GetRequestHandler handler) {
        this.getHandlers.put(pathPrefix, handler);
        return this;
    }

    public synchronized KiteTestHttpdServer addPostPathHandler(String pathPrefix, RequestWithBodyHandler handler) {
        this.postHandlers.put(pathPrefix, handler);
        return this;
    }

    public synchronized KiteTestHttpdServer addPutPathHandler(String pathPrefix, RequestWithBodyHandler handler) {
        this.putHandlers.put(pathPrefix, handler);
        return this;
    }

    public synchronized KiteTestHttpdServer addDeletePathHandler(String pathPrefix, RequestWithBodyHandler handler) {
        this.deleteHandlers.put(pathPrefix, handler);
        return this;
    }

    public synchronized void reset() {
        getHandlers.clear();
        postHandlers.clear();
        putHandlers.clear();
        deleteHandlers.clear();
    }

    protected Response goGetRequest(IHTTPSession session) throws IOException, InterruptedException, HttpConnectionUnavailableException, HttpStatusException, HttpRequestFailedException {
        GetRequestHandler handler;
        synchronized (this) {
            handler = getHandlers.get(session.getUri());
        }

        if (handler != null) {
            String response = handler.handleRequest(session.getUri(), Collections.emptyMap());
            return newFixedLengthResponse(Response.Status.OK, MIME_JSON, response);
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "No GET request at " + session.getUri());
    }

    private Response doRequest(IHTTPSession session, TreeMap<String, RequestWithBodyHandler> handlers) throws IOException, InterruptedException, HttpConnectionUnavailableException, HttpStatusException, HttpRequestFailedException {
        RequestWithBodyHandler handler;
        synchronized (this) {
            handler = handlers.get(session.getUri());
        }

        String payload;
        HashMap<String, String> map = Maps.newHashMap();
        try {
            session.parseBody(map);
            payload = map.getOrDefault("postData", "");
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }

        Response response;
        if (handler != null) {
            //reading the request body is currently not possible with NanoHTTPD
            response = newFixedLengthResponse(Response.Status.OK, MIME_JSON, handler.handleRequest(session.getUri(), payload));
        } else {
            response = newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "No POST request at " + session.getUri());
        }

        response.setGzipEncoding(false);
        return response;
    }

}
