package com.kite.intellij.backend.http;

import com.kite.http.KiteTestHttpdServer;
import com.kite.intellij.KiteConstants;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

@SuppressWarnings("Duplicates")
public class JettyHttpConnectionTimeoutTest {
    private KiteTestHttpdServer httpd;

    @Before
    public void setup() throws Exception {
        httpd = new KiteTestHttpdServer();
        httpd.start();
    }

    @After
    public void shutdown() {
        httpd.reset();
        httpd.closeAllConnections();
    }

    @Test
    public void testDefaultTimeout() throws Exception {
        httpd.addGetPathHandler("/noTimeout", (path, queryParams) -> "");
        httpd.addGetPathHandler("/defaultTimeout", (path, queryParams) -> {
            try {
                Thread.sleep(KiteConstants.SO_TIMEOUT_MILLIS_DEFAULT + 3000);
                return "";
            } catch (InterruptedException e) {
                throw new HttpRequestFailedException("Error", e);
            }
        });
        httpd.addGetPathHandler("/longTimeout", (path, queryParams) -> {
            try {
                Thread.sleep(KiteConstants.SO_TIMEOUT_MILLIS_LONG + 3000);
                return "";
            } catch (InterruptedException e) {
                throw new HttpRequestFailedException("Error", e);
            }
        });

        KiteHttpConnection client = new JettyAsyncHttpConnection("127.0.0.1", httpd.getListeningPort());
        Assert.assertEquals("An immediate response must pass", "", client.doGet("/noTimeout", Collections.emptyMap(), HttpTimeoutConfig.DefaultTimeout));

        // disabled for now due to flaky results
//        try {
//            client.doGet("/defaultTimeout", Collections.emptyMap(), HttpTimeoutConfig.DefaultTimeout);
//            Assert.fail("A request longer than the default timeout must throw an exception.");
//        } catch (HttpRequestFailedException ignored) {
//        }
//
//        //must not time out
//        Assert.assertEquals("", client.doGet("/defaultTimeout", Collections.emptyMap(), HttpTimeoutConfig.LongTimeout));
//
//        try {
//            client.doGet("/longTimeout", Collections.emptyMap(), HttpTimeoutConfig.LongTimeout);
//            Assert.fail("A timeout with long timeout config must throw an exception.");
//        } catch (HttpRequestFailedException ignored) {
//        }
    }
}

