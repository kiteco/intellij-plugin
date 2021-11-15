package com.kite.intellij.backend;

import com.kite.http.KiteTestHttpdServer;
import com.kite.intellij.backend.http.*;
import com.kite.intellij.backend.model.CompletionRange;
import com.kite.intellij.backend.model.CompletionSnippet;
import com.kite.intellij.backend.model.KiteCompletion;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultKiteApiServiceOnlineTest extends KiteLightFixtureTest {
    private KiteTestHttpdServer httpd;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        httpd = new KiteTestHttpdServer();
        httpd.start();
    }

    @Override
    protected void tearDown() throws Exception {
        httpd.reset();
        httpd.closeAllConnections();

        super.tearDown();
    }

    public void testCompletionRequest() throws HttpConnectionUnavailableException, HttpRequestFailedException {
        AtomicReference<String> body = new AtomicReference<>("");
        httpd.addPostPathHandler("/clientapi/editor/complete", (path, payload) -> {
            body.set(payload);
            return "";
        });

        KiteApiService api = setupApi();
        api.completions(CanonicalFilePathFactory.getInstance().forNativePath("test.py"), "", 0, null, false, HttpTimeoutConfig.ShortTimeout);

        String expectedPayload = "{\"editor\":\"intellij\",\"filename\":\"test.py\",\"text\":\"\",\"offset_encoding\":\"utf-16\",\"position\":{\"begin\":0,\"end\":0}}";
        Assert.assertEquals("The payload must include the editor property", expectedPayload, body.get());
    }

    public void testRelatedCode() throws HttpConnectionUnavailableException, HttpRequestFailedException {
        AtomicReference<String> body = new AtomicReference<>("");
        httpd.addPostPathHandler("/codenav/editor/related", (path, payload) -> {
            body.set(payload);
            return "";
        });

        KiteApiService api = setupApi();
        api.relatedCode(CanonicalFilePathFactory.getInstance().forNativePath("/Users/Johnny/go/test.py"), 20, HttpTimeoutConfig.ShortTimeout);

        // Broken up into three paths because "install_path" will differ between local and CI environment
        Assert.assertTrue(body.get().contains("\"editor\":\"intellij\""));
        Assert.assertTrue(body.get().contains("\"editor_install_path\":"));
        // Expected one-based line-number in json from zero-based
        Assert.assertTrue(body.get().contains("\"location\":{\"filename\":\"/Users/Johnny/go/test.py\",\"line\":21"));
    }

    @Test
    public void testCheckOnlineStatusOffline() throws Exception {
        httpd.addGetPathHandler("/clientapi/ping", (path, queryParams) -> {
            throw new HttpConnectionUnavailableException("Kite if offline");
        });

        //use an unbound port to force a connection failed exception
        KiteApiService api = setupApi(httpd.getHostname(), 1234);

        boolean online = api.checkOnlineStatus();
        Assert.assertFalse("Kite must not be online if the connection failed!", online);
    }

    @Test
    public void testCheckOnlineStatusTimeout() throws Exception {
        //requests on windows to an unbound port take a long time and are cancelled with a timeout
        //this is different behaviour than on Unix systems (which fail quickly)
        httpd.addGetPathHandler("/clientapi/ping", (path, queryParams) -> {
            try {
                Thread.sleep(HttpTimeoutConfig.MinimalTimeout.timeoutMillis() + 400);
            } catch (InterruptedException e) {
                //ignore
            }
            return "";
        });

        //use an unbound port to force a connection failed exception
        KiteApiService api = setupApi();

        boolean online = api.checkOnlineStatus();
        Assert.assertFalse("Kite must not be online if the connection failed with a timeout!", online);
    }

    @Test
    public void testCheckOnlineStatusOnline() throws Exception {
        httpd.addGetPathHandler("/clientapi/ping", (path, queryParams) -> {
            throw new HttpStatusException("unauthorized", 401, "");
        });

        KiteApiService api = setupApi();

        boolean online = api.checkOnlineStatus();
        Assert.assertTrue("Kite must be online if the connection was successful and the request wasn't successful (status != 200)", online);
    }

    @Test
    public void testConnectionListener() throws Exception {
        JettyAsyncHttpConnection http = new JettyAsyncHttpConnection("localhost", 9999); //no server started

        AtomicBoolean hasConnection = new AtomicBoolean(true);

        CountDownLatch latch = new CountDownLatch(1);

        KiteApiService api = DefaultKiteApiService.create(http);
        api.addConnectionStatusListener((connectionAvailable, error) -> {
            hasConnection.set(connectionAvailable);
            latch.countDown();
        }, getTestRootDisposable());

        api.licenseInfo();
        latch.await(500, TimeUnit.MILLISECONDS);

        Assert.assertFalse("The connection status must have been set to 'unavailable'", hasConnection.get());
    }

    private KiteApiService setupApi() {
        return setupApi(httpd.getHostname(), httpd.getListeningPort());
    }

    private KiteApiService setupApi(String hostname, int listeningPort) {
        JettyAsyncHttpConnection httpConnection = new JettyAsyncHttpConnection(hostname, listeningPort);

        return DefaultKiteApiService.create(httpConnection);
    }
}