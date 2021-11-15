package com.kite.intellij.backend.http;

import com.kite.http.KiteTestHttpdServer;
import com.kite.http.LoggingHttpServer;
import com.kite.intellij.backend.DefaultKiteApiService;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.model.Id;
import com.kite.intellij.backend.response.HoverResponse;
import com.kite.intellij.backend.response.MembersResponse;
import com.kite.intellij.platform.fs.UnixCanonicalPath;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class JettyAsyncHttpConnectionTest extends KiteLightFixtureTest {
    @Test
    public void testParamsSerializing() throws Exception {
        KiteTestHttpdServer server = new KiteTestHttpdServer();
        server.addGetPathHandler("/api/editor/value/myId/members", (path, queryParams) -> "{\"total\":10, \"start\":0, \"end\":10, \"members\":[]}");
        server.start();

        try {
            JettyAsyncHttpConnection http = new JettyAsyncHttpConnection(server.getHostname(), server.getListeningPort());

            KiteApiService api = DefaultKiteApiService.create(http);
            MembersResponse response = api.members(Id.of("myId"), 0, 10);
            Assert.assertNotNull(response);
        } finally {
            server.stop();
        }
    }

    @Test
    public void testUnicodePathGetRequest() throws Exception {
        try (LoggingHttpServer server = startLoggingServer()) {
            JettyAsyncHttpConnection http = new JettyAsyncHttpConnection(server.getHostname(), server.getListeningPort());

            HoverResponse response = DefaultKiteApiService.create(http).hover(new UnixCanonicalPath("/home/user/OCR合集/dog.py"), "", 0);
            Assert.assertNotNull(response);

            Assert.assertEquals(1, server.getRequests().size());
            Assert.assertEquals("GET /api/buffer/intellij/:home:user:OCR合集:dog.py/d41d8cd98f00b204e9800998ecf8427e/hover?cursor_runes=0", server.getRequests().get(0).asString());
        }
    }

    @Test
    public void testConnectionListenerOffline() throws Exception {
        JettyAsyncHttpConnection http = new JettyAsyncHttpConnection("localhost", 0); //no server started

        try {
            http.doGet("/not/available", Collections.emptyMap(), HttpTimeoutConfig.DefaultTimeout);
            Assert.fail("Expected a connection unavailable exception");
        } catch (HttpConnectionUnavailableException e) {
            //ignore
        }
    }

    @Nonnull
    protected static LoggingHttpServer startLoggingServer() throws IOException {
        LoggingHttpServer server = new LoggingHttpServer(0) {
            @Override
            protected Response responseData(String uri, Method method, Map<String, String> parms) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"part_of_syntax\": \"\", \"symbol\": null, \"report\": null}");
            }
        };
        server.start();
        return server;
    }
}