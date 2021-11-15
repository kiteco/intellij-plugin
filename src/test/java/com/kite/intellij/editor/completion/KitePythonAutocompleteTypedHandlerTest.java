package com.kite.intellij.editor.completion;

import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.jetbrains.annotations.NotNull;

public class KitePythonAutocompleteTypedHandlerTest extends KiteLightFixtureTest {
    public void testDoubleQuote() throws Throwable {
        runWithAutocompletionEnabled((tester) -> {
            MockKiteHttpConnection httpConnection = MockKiteHttpConnection.getInstance();
            httpConnection.addPostPathHandler("/clientapi/editor/complete", (path, payload) -> loadFile("json/doubleQuoteCompletion.json"), getTestRootDisposable());
            httpConnection.addPostPathHandler("/clientapi/editor/event", (path, payload) -> "{}", getTestRootDisposable());
            MockKiteApiService.getInstance().enableHttpCalls();

            myFixture.configureByText("file.py", "print(<caret>)");

            tester.typeWithPauses("\"");
            assertNotNull("Popup expected for opening quote", tester.getLookup());

            tester.typeWithPauses("string content");

            tester.typeWithPauses("\"");
            assertNull("No Popup expected for closing quote", tester.getLookup());
        });
    }

    public void testEscapedDoubleQuote() throws Throwable {
        runWithAutocompletionEnabled((tester) -> {
            MockKiteHttpConnection httpConnection = MockKiteHttpConnection.getInstance();
            httpConnection.addPostPathHandler("/clientapi/editor/complete", (path, payload) -> loadFile("json/doubleQuoteCompletion_escaped.json"), getTestRootDisposable());
            httpConnection.addPostPathHandler("/clientapi/editor/event", (path, payload) -> "{}", getTestRootDisposable());
            MockKiteApiService.getInstance().enableHttpCalls();

            myFixture.configureByText("file.py", "print(\"string content\\<caret>)");

            tester.typeWithPauses("\"");
            assertNotNull("Popup expected for escaped closing quote", tester.getLookup());
        });
    }

    public void testSingleQuote() throws Throwable {
        runWithAutocompletionEnabled((tester) -> {
            MockKiteHttpConnection httpConnection = MockKiteHttpConnection.getInstance();
            httpConnection.addPostPathHandler("/clientapi/editor/complete", (path, payload) -> loadFile("json/singleQuoteCompletion.json"), getTestRootDisposable());
            httpConnection.addPostPathHandler("/clientapi/editor/event", (path, payload) -> "{}", getTestRootDisposable());
            MockKiteApiService.getInstance().enableHttpCalls();

            myFixture.configureByText("file.py", "print(<caret>)");

            tester.typeWithPauses("'");
            assertNotNull("Popup expected for opening quote", tester.getLookup());

            tester.typeWithPauses("string content");

            tester.typeWithPauses("'");
            assertNull("No Popup expected for closing quote", tester.getLookup());
        });
    }

    //@Override // unavailable in super in 2020.3+
    protected void invokeTestRunnable(@NotNull Runnable runnable) {
        runnable.run();
    }

    @Override
    protected boolean runInDispatchThread() {
        return false;
    }

    @Override
    protected String getBasePath() {
        return "python/editor/codeCompletion/";
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }
}