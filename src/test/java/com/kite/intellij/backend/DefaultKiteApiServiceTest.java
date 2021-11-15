package com.kite.intellij.backend;

import com.google.common.collect.Lists;
import com.kite.intellij.backend.http.HttpConnectionUnavailableException;
import com.kite.intellij.backend.http.HttpRequestFailedException;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.backend.model.*;
import com.kite.intellij.backend.response.*;
import com.kite.intellij.lang.KiteLanguage;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import com.kite.intellij.platform.fs.UnixCanonicalPath;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultKiteApiServiceTest extends KiteLightFixtureTest {
    @Test
    public void testValueReportResponse() throws Exception {
        MockKiteApiService api = setupHttpApi();

        MockKiteHttpConnection.getInstance().addGetPathHandler("/api/editor/value", (path, queryParams) -> loadFile("valueReport/data.json"), getTestRootDisposable());

        ValueReportResponse report = api.valueReport(Id.of("myId"));
        Assert.assertEquals("valueReport(myId)", api.getCallHistory().get(0));
        Assert.assertEquals("GET /api/editor/value/myId", MockKiteHttpConnection.getInstance().getHttpRequestStringHistory().get(0));

        Assert.assertNotNull(report);
        Assert.assertTrue(report.getReport().getDescriptionText().startsWith("Print objects to the stream file"));
        Assert.assertTrue(report.getReport().getDescriptionHtml().contains("must be strings; they can also be"));
    }

    @Test
    public void testSymbolReportResponse() throws Exception {
        MockKiteApiService api = setupHttpApi();

        MockKiteHttpConnection.getInstance().addGetPathHandler("/api/editor/symbol", (path, queryParams) -> loadFile("symbolReport/data.json"), getTestRootDisposable());

        SymbolReportResponse report = api.symbolReport(Id.of("myId"));
        Assert.assertEquals("symbolReport(myId)", api.getCallHistory().get(0));
        Assert.assertEquals("GET /api/editor/symbol/myId", MockKiteHttpConnection.getInstance().getHttpRequestStringHistory().get(0));

        Assert.assertNotNull("The report must not be null", report);
        Assert.assertTrue(report.getReport().getDescriptionText().startsWith("Print objects to the stream file"));
        Assert.assertTrue(report.getReport().getDescriptionHtml().contains("must be strings; they can also be"));
    }

    @Test
    public void testHoverResponse() throws Exception {
        MockKiteApiService api = setupHttpApi();

        MockKiteHttpConnection.getInstance().addGetPathHandler("/api/buffer/intellij", (path, queryParams) -> loadFile("hover/data.json"), getTestRootDisposable());

        HoverResponse hover = api.hover(new UnixCanonicalPath("test.py"), "content", 0);
        Assert.assertEquals("hover(test.py, 7 chars, 0)", api.getCallHistory().get(0));
        Assert.assertEquals("GET /api/buffer/intellij/test.py/9a0364b9e99bb480dd25e1f0284c8555/hover?cursor_runes=0", MockKiteHttpConnection.getInstance().getHttpRequestStringHistory().get(0));

        Assert.assertNotNull(hover);
        Assert.assertEquals("text report", hover.getReport().getDescriptionText());
        Assert.assertEquals("html report", hover.getReport().getDescriptionHtml());
    }

    @Test
    public void testHoverNotImplemented() {
        MockKiteApiService api = setupHttpApi();

        MockKiteHttpConnection.getInstance().addGetPathHandler("/api/buffer/intellij", (path, queryParams) -> {
            throw new HttpStatusException("Not implemented", 501, "");
        }, getTestRootDisposable());

        try {
            api.hover(new UnixCanonicalPath("test.py"), "content", 0);
            Assert.fail("Expected status exception instead of regular return");
        } catch (KiteHttpException e) {
            //expected
        }
    }

    @Test
    public void testHoverResponseUnicode() throws Exception {
        MockKiteApiService api = setupHttpApi();

        MockKiteHttpConnection.getInstance().addGetPathHandler("/api/buffer/intellij", (path, queryParams) -> loadFile("hover/data.json"), getTestRootDisposable());

        //Unicode chars were not send properly before the fix
        HoverResponse hover = api.hover(new UnixCanonicalPath("test.py"), "类似于微信未读信息数量那种提示效果", 0);
        Assert.assertEquals("hover(test.py, 17 chars, 0)", api.getCallHistory().get(0));
        Assert.assertEquals("GET /api/buffer/intellij/test.py/9a10a18f5d6b502b0143b1aa790c18a5/hover?cursor_runes=0", MockKiteHttpConnection.getInstance().getHttpRequestStringHistory().get(0));

        Assert.assertNotNull(hover);
        Assert.assertTrue(hover.getReport().getDescriptionText().equals("text report"));
        Assert.assertTrue(hover.getReport().getDescriptionHtml().equals("html report"));
    }

    @Test
    public void testCompletionResponse() throws Exception {
        MockKiteApiService api = setupHttpApi();

        MockKiteHttpConnection.getInstance().addPostPathHandler("/clientapi/editor/complete", (path, payload) -> loadFile("completion/data.json"), getTestRootDisposable());

        KiteCompletions completions = api.completions(new UnixCanonicalPath("file.py"), "content", 7);
        Assert.assertEquals("completions(file.py, 7 chars, 7)", api.getCallHistory().get(0));
        String expected = "POST /clientapi/editor/complete\n" +
                "{\"editor\":\"intellij\",\"filename\":\"file.py\",\"text\":\"content\",\"offset_encoding\":\"utf-16\",\"position\":{\"begin\":7,\"end\":7}}";
        Assert.assertEquals(expected, MockKiteHttpConnection.getInstance().getHttpRequestStringHistory().get(0));

        Assert.assertNotNull(completions);
        Assert.assertEquals(19, completions.getItems().length);
        KiteCompletion first = completions.getItems()[0];
        Assert.assertEquals("format(…)", first.getDisplay());
        Assert.assertTrue(first.getDocumentation().startsWith("doc for format()"));
    }

    @Test
    public void testCompletion404Response() throws Exception {
        MockKiteApiService api = setupHttpApi();

        MockKiteHttpConnection.getInstance().addPostPathHandler("/clientapi/editor/complete", (path, payload) -> {
            throw new HttpStatusException("completions not found", 404, "");
        }, getTestRootDisposable());

        KiteCompletions completions = api.completions(new UnixCanonicalPath("file.py"), "content", 7);
        //a 404 must not return null as response, because it indicates an empty set of completions
        Assert.assertNotNull(completions);
        Assert.assertEquals(0, completions.getItems().length);
    }

    @Test
    public void testCompletion403Response() throws Exception {
        MockKiteApiService api = setupHttpApi();

        MockKiteHttpConnection.getInstance().addPostPathHandler("/clientapi/editor/complete", (path, payload) -> {
            throw new HttpStatusException("File not whitelisted", 403, "");
        }, getTestRootDisposable());

        KiteCompletions completions = api.completions(new UnixCanonicalPath("file.py"), "content", 7);
        //a 403 indicates that the file is not whitelisted and that completions are forbidden
        Assert.assertNull(completions);
    }

    @Test
    public void testCompletionUnicodeResponse() throws Exception {
        MockKiteApiService api = setupHttpApi();

        MockKiteHttpConnection.getInstance().addPostPathHandler("/clientapi/editor/complete", (path, payload) -> loadFile("completion/data.json"), getTestRootDisposable());

        KiteCompletions completions = api.completions(new UnixCanonicalPath("file.py"), "类似于微信未读信息数量那种提示效果", 8);
        Assert.assertEquals("completions(file.py, 17 chars, 8)", api.getCallHistory().get(0));
        String expected = "POST /clientapi/editor/complete\n" +
                "{\"editor\":\"intellij\",\"filename\":\"file.py\",\"text\":\"类似于微信未读信息数量那种提示效果\",\"offset_encoding\":\"utf-16\",\"position\":{\"begin\":8,\"end\":8}}";
        Assert.assertEquals(expected, MockKiteHttpConnection.getInstance().getHttpRequestStringHistory().get(0));

        Assert.assertNotNull(completions);
        Assert.assertEquals(19, completions.getItems().length);
        KiteCompletion first = completions.getItems()[0];
        Assert.assertEquals("format(…)", first.getDisplay());
        Assert.assertTrue(first.getDocumentation().startsWith("doc for format()"));
    }

    @Test
    public void testSignaturesResponse() throws Exception {
        MockKiteApiService api = setupHttpApi();

        MockKiteHttpConnection.getInstance().addPostPathHandler("/clientapi/editor/signatures", (path, payload) -> loadFile("signatures/data.json"), getTestRootDisposable());

        Calls response = api.signatures(new UnixCanonicalPath("file.py"), "content", 3);
        Assert.assertEquals("signatures(file.py, 7 chars, 3)", api.getCallHistory().get(0));
        String expected = "POST /clientapi/editor/signatures\n" +
                "{\"editor\":\"intellij\",\"filename\":\"file.py\",\"text\":\"content\",\"cursor_runes\":3,\"offset_encoding\":\"utf-16\"}";
        Assert.assertEquals(expected, MockKiteHttpConnection.getInstance().getHttpRequestStringHistory().get(0));

        Assert.assertNotNull(response);
        Assert.assertEquals(1, response.getCalls().length);
        Assert.assertEquals(5, response.getCalls()[0].getSignatures().length);
    }

    @Test
    public void testSignaturesResponse501() {
        MockKiteApiService api = setupHttpApi();

        MockKiteHttpConnection.getInstance().addPostPathHandler("/clientapi/editor/signatures", (path, payload) -> {
            throw new HttpStatusException("Unsupported", 501, "");
        }, getTestRootDisposable());

        try {
            api.signatures(new UnixCanonicalPath("file.py"), "content", 3);
            Assert.fail("Expected exception instead of regular return.");
        } catch (KiteHttpException e) {
            //expected
        }
    }

    @Test
    public void testFileStatusResponse() {
        MockKiteApiService api = setupHttpApi();

        MockKiteHttpConnection.getInstance().addGetPathHandler("/clientapi/status", (path, queryParams) -> loadFile("fileStatus/fileStatusIndexing.json"), getTestRootDisposable());

        KiteFileStatusResponse response = api.fileStatus(new UnixCanonicalPath("/home/user/test.py"));
        Assert.assertEquals("fileStatus(:home:user:test.py)", api.getCallHistory().get(0));
        Assert.assertEquals("GET /clientapi/status?filename=/home/user/test.py", MockKiteHttpConnection.getInstance().getHttpRequestStringHistory().get(0));

        Assert.assertNotNull(response);
        Assert.assertEquals(KiteFileStatus.Indexing, response.getStatus());
    }

    @Test
    public void testFileStatusReadyResponse() {
        MockKiteApiService api = setupHttpApi();

        MockKiteHttpConnection.getInstance().addGetPathHandler("/clientapi/status", (path, queryParams) -> loadFile("fileStatus/fileStatusReady.json"), getTestRootDisposable());

        KiteFileStatusResponse response = api.fileStatus(new UnixCanonicalPath("/home/user/test.py"));
        Assert.assertEquals("fileStatus(:home:user:test.py)", api.getCallHistory().get(0));
        Assert.assertEquals("GET /clientapi/status?filename=/home/user/test.py", MockKiteHttpConnection.getInstance().getHttpRequestStringHistory().get(0));

        Assert.assertNotNull(response);
        Assert.assertEquals(KiteFileStatus.Ready, response.getStatus());
    }

    @Test
    public void testMembersResponse() throws Exception {
        MockKiteApiService api = setupHttpApi();

        MockKiteHttpConnection.getInstance().addGetPathHandler("/api/editor/value/myId/members", (path, queryParams) -> loadFile("members/data.json"), getTestRootDisposable());

        MembersResponse members = api.members(Id.of("myId"), -1, 10);
        Assert.assertNotNull(members);
        Assert.assertEquals(0, members.getStart());
        Assert.assertEquals(10, members.getEnd());
        Assert.assertEquals(12, members.getTotal());
        Assert.assertEquals(10, members.getMembers().length);
    }

    @Test
    public void testStatusListeners() throws Exception {
        MockKiteApiService.getInstance().enableHttpStatusListeners();

        List<String> listenerEvents = Collections.synchronizedList(Lists.newArrayList());

        MockKiteApiService api = setupHttpApi();

        api.enableHttpCalls();
        HttpStatusListener listener = (status, exception, filePath, requestPath) -> {
            listenerEvents.add(String.format("status: %d, file: %s, message: %s", status, filePath != null ? filePath.asSlashDelimitedPath() : "", (exception != null ? exception.getMessage() : "")));
            return true;
        };
        api.addHttpRequestStatusListener(listener, getTestRootDisposable());

        MockKiteHttpConnection.getInstance().addPostPathHandler("/clientapi/editor/complete", (path, body) -> {
            throw new HttpStatusException("not whitelisted", 403, "note whitelisted");
        }, getTestRootDisposable());

        Assert.assertEquals(0, listenerEvents.size());

        api.completions(new UnixCanonicalPath("/home/test/test.py"), "content", 0);
        Thread.sleep(500); //wait for the background listener processing

        Assert.assertEquals(1, listenerEvents.size());
        Assert.assertEquals("status: 403, file: /home/test/test.py, message: not whitelisted, status: 403, body: note whitelisted", listenerEvents.get(0));

        api.completions(new UnixCanonicalPath("/home/test/test2.py"), "content", 0);
        Thread.sleep(500); //wait for the background listener processing
        Assert.assertEquals(2, listenerEvents.size());
        Assert.assertEquals("status: 403, file: /home/test/test2.py, message: not whitelisted, status: 403, body: note whitelisted", listenerEvents.get(1));

        //remove the listener
        api.removeHttpRequestStatusListener(listener);
        api.completions(new UnixCanonicalPath("/home/test/test2.py"), "content", 0);
        Thread.sleep(500); //wait for the background listener processing
        Assert.assertEquals("No new notifications expected for an empty list of listeners", 2, listenerEvents.size());
    }

    @Test
    public void testLanguages() throws Exception {
        MockKiteApiService api = setupHttpApi();

        MockKiteHttpConnection.getInstance().addGetPathHandler("/clientapi/languages", (path, queryParams) -> "[\"python\"]", getTestRootDisposable());

        Set<KiteLanguage> languages = api.languages();
        Assert.assertEquals(1, languages.size());
        Assert.assertTrue(languages.contains(KiteLanguage.Python));
    }

    @Test
    public void testEmptyLanguages() throws Exception {
        MockKiteApiService api = setupHttpApi();
        MockKiteHttpConnection.getInstance().addGetPathHandler("/clientapi/languages", (path, queryParams) -> "[]", getTestRootDisposable());

        Assert.assertEquals(0, api.languages().size());
    }

    @Test
    public void testOpenCopilot() throws HttpConnectionUnavailableException, HttpStatusException, HttpRequestFailedException {
        MockKiteApiService apiService = setupHttpApi();

        MockKiteHttpConnection.getInstance().addGetPathHandler("/clientapi/sidebar/open", (path, queryParams) -> "", getTestRootDisposable());

        apiService.openKiteCopilot();
        Assert.assertEquals(1, apiService.getCallHistory().size());
        Assert.assertEquals("openKiteCopilot()", apiService.getCallHistory().get(0));
    }

    @Override
    protected String getBasePath() {
        return "api/";
    }

    /**
     * Returns a api service without any registered event listeners.
     */
    @Nonnull
    private MockKiteApiService setupHttpApi() {
        MockKiteApiService api = getKiteApiService();
        api.enableHttpCalls();

        return api;
    }
}