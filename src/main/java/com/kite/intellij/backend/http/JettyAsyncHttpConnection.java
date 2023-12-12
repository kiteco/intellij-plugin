package com.kite.intellij.backend.http;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.kite.intellij.KiteConstants;
import com.kite.monitoring.TimeTracker;
import com.kite.monitoring.TimerTrackers;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.HttpCookieStore;
import org.eclipse.jetty.util.URIUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The default implementation which is used to communicate with Kite. The interface may be implemented to
 * have a separate implementation for unit testing.
 *
  */
public class JettyAsyncHttpConnection implements KiteHttpConnection, Disposable {
    private static final Logger LOG = Logger.getInstance("#kite.http");
    private static final Logger BASH_LOG = Logger.getInstance("#kite.http.script");

    private final HttpClient httpclient;
    private final String host;
    private final int port;

    private volatile boolean disposed;
    private volatile long lastRequestTimestamp = -1;

    /**
     * Creates a new http connection using the default port and hostname.
     */
    @SuppressWarnings("unused")
    public JettyAsyncHttpConnection() {
        this(KiteConstants.DEFAULT_HOST, KiteConstants.DEFAULT_PORT);
    }

    /**
     * Creates a new instance with the given host and port.
     *
     * @param host The host to use
     * @param port The port to use
     */
    public JettyAsyncHttpConnection(String host, int port) {
        this.host = host;
        this.port = port;

        //rather high number of concurrent connections because things like completion call are called often
        //and left unused if the ui thread cancelled the completion action

        try {
            httpclient = new HttpClient();
            httpclient.setConnectBlocking(false);
            httpclient.setFollowRedirects(false);
            httpclient.setMaxConnectionsPerDestination(20);
            httpclient.setMaxRequestsQueuedPerDestination(5);
            httpclient.setCookieStore(new HttpCookieStore.Empty());
            httpclient.setUserAgentField(new HttpField(HttpHeader.USER_AGENT, "intellij"));
            //disable gzip response compression
            httpclient.getContentDecoderFactories().clear();

            // start all clients
            httpclient.start();
        } catch (Exception e) {
            throw new RuntimeException("Error while initialising JettyAsyncHttpConntection", e);
        }
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;

            try {
                this.httpclient.stop();
            } catch (Exception e) {
                //ignored
            }
        }
    }

    @Nonnull
    @Override
    public String doGet(@Nonnull String path, @Nonnull Map<String, String> parameters, HttpTimeoutConfig timeoutConfig) throws HttpStatusException, HttpRequestFailedException, HttpConnectionUnavailableException {
        Request method = httpclient.newRequest(host, port).method(HttpMethod.GET).path(URIUtil.encodePath(path));
        addParamsToRequest(parameters, method);

        return doHttpRequest(method, timeoutConfig, "");
    }

    @Nonnull
    @Override
    public String doPost(@Nonnull String path, @Nullable String payload, HttpTimeoutConfig timeoutConfig) throws HttpStatusException, HttpRequestFailedException, HttpConnectionUnavailableException {
        Request request = httpclient.newRequest(host, port).method(HttpMethod.POST).path(URIUtil.encodePath(path));
        if (payload != null) {
            request.content(new StringContentProvider(payload, StandardCharsets.UTF_8), "application/json");
        }

        return doHttpRequest(request, timeoutConfig, payload);
    }

    @Nonnull
    @Override
    public String doPut(@Nonnull String path, @Nonnull Map<String, String> parameters, @Nullable String payload, HttpTimeoutConfig timeoutConfig) throws HttpStatusException, HttpRequestFailedException, HttpConnectionUnavailableException {
        Request request = httpclient.newRequest(host, port).method(HttpMethod.PUT).path(URIUtil.encodePath(path));

        if (payload != null) {
            request.content(new StringContentProvider(payload, StandardCharsets.UTF_8), "application/json");
        }

        addParamsToRequest(parameters, request);

        return doHttpRequest(request, timeoutConfig, payload);
    }

    @Nonnull
    @Override
    public String doDelete(@Nonnull String path, @Nonnull Map<String, String> parameters, @Nullable String payload, HttpTimeoutConfig timeoutConfig) throws HttpStatusException, HttpRequestFailedException, HttpConnectionUnavailableException {
        Request request = httpclient.newRequest(host, port).method(HttpMethod.DELETE).path(URIUtil.encodePath(path));

        if (payload != null) {
            request.content(new StringContentProvider(payload, StandardCharsets.UTF_8), "application/json");
        }

        addParamsToRequest(parameters, request);

        return doHttpRequest(request, timeoutConfig, payload);
    }

    protected String doHttpRequest(Request request, HttpTimeoutConfig timeoutConfig, String content) throws HttpRequestFailedException, HttpStatusException, HttpConnectionUnavailableException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("HTTP " + request.getMethod() + " " + request.getURI());

            if (LOG.isTraceEnabled() && ApplicationManager.getApplication().isDispatchThread()) {
                try {
                    throw new IllegalStateException(String.format("HTTP request %s %s executed in UI thread", request.getMethod(), request.getPath()));
                } catch (Exception e) {
                    // use debug because trace doesn't allow to pass an exception
                    LOG.debug("HTTP request executed in UI thread", e);
                }
            }
        }

        if (BASH_LOG.isTraceEnabled()) {
            //print a curl command line to perform the same request
            long sinceLastRequest = lastRequestTimestamp > 0 ? (System.currentTimeMillis() - lastRequestTimestamp) : 0;
            if (BASH_LOG.isTraceEnabled() && sinceLastRequest > 0) {
                BASH_LOG.trace("sleep " + (float) sinceLastRequest / 1000.0);
            }

            String curlCommand;
            if (request.getMethod().equals("GET")) {
                curlCommand = String.format("curl '%s' -f -X%s", StringUtils.replace(request.getURI().toString(), "'", "\\'"), request.getMethod());
            } else {
                String body = StringUtils.replace(content, "'", "\\'");
                String escaped = StringUtils.replace(body, "\\", "\\\\");
                escaped = StringUtils.replace(escaped, "\"", "\\\"");
                curlCommand = String.format("curl '%s' -f -X%s --data \"%s\" -H 'Accept: application/json'", request.getURI(), request.getMethod(), escaped);
            }

            BASH_LOG.trace(curlCommand);

            lastRequestTimestamp = System.currentTimeMillis();
        }

        try (TimeTracker ignored = TimerTrackers.start("HTTP request " + request.getMethod() + " " + request.getPath())) {
            long timeoutMillis = timeoutConfig.timeoutMillis();

            ContentResponse response = request.timeout(timeoutMillis, TimeUnit.MILLISECONDS).send();

            int statusCode = response.getStatus();
            if (statusCode == org.eclipse.jetty.http.HttpStatus.OK_200) {
                return response.getContentAsString();
            }

            throw new HttpStatusException("Unexpected http status", statusCode, response.getContentAsString());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ConnectException) {
                throw new HttpConnectionUnavailableException("Connection failed", e);
            }

            //some http status exceptions are nested in execution exceptions
            for (Throwable cause = e; cause != null; cause = cause.getCause()) {
                if (cause instanceof HttpResponseException) {
                    Response response = ((HttpResponseException) cause).getResponse();
                    throw new HttpStatusException(response.getReason(), response.getStatus(), null, e);
                }
            }

            throw new HttpRequestFailedException("Request failed with an unknown error", e);
        } catch (InterruptedException | TimeoutException e) {
            throw new HttpRequestFailedException("Request was interrupted or cancelled", e);
        }
    }

    private void addParamsToRequest(@Nonnull Map<String, String> parameters, Request method) {
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            method.param(entry.getKey(), entry.getValue());
        }
    }
}
