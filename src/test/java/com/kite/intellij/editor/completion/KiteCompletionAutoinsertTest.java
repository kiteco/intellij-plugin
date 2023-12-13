package com.kite.intellij.editor.completion;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.jetbrains.python.PythonFileType;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.editor.events.TestcaseEditorEventListener;
import com.kite.intellij.settings.KiteSettingsService;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class KiteCompletionAutoinsertTest extends KiteLightFixtureTest {
    @Test
    public void testSingleCompletionInserted() throws Throwable {
        runWithAutocompletionEnabled((tester) -> {
            MockKiteHttpConnection httpConnection = MockKiteHttpConnection.getInstance();
            httpConnection.addPostPathHandler("/clientapi/editor/complete", (path, payload) -> loadFile("json/singleCompletion.json"), getTestRootDisposable());
            httpConnection.addPostPathHandler("/clientapi/editor/event", (path, payload) -> "{}", getTestRootDisposable());

            MockKiteApiService.getInstance().enableHttpCalls();

            myFixture.configureByText("file.py", "import j<caret>");

            // make sure that we remove events for focus (182.x seems to trigger focus events in tests, earlier versions don't)
            MockKiteApiService.getInstance().clearTestData();

            //after the initial setup to minimize events
            myFixture.complete(CompletionType.BASIC);

            Assert.assertEquals("Kite's insert value must be used to insert the completion into the document.", "import json as j", myFixture.getEditor().getDocument().getText());
        });
    }

    @Test
    public void testMultilineCompletions() throws Throwable {
        runWithAutocompletionEnabled((tester) -> {
            MockKiteHttpConnection httpConnection = MockKiteHttpConnection.getInstance();
            httpConnection.addPostPathHandler("/clientapi/editor/complete", (path, payload) -> loadFile("json/multilineCompletions.json"), getTestRootDisposable());
            httpConnection.addPostPathHandler("/clientapi/editor/event", (path, payload) -> "{}", getTestRootDisposable());

            MockKiteApiService.getInstance().enableHttpCalls();

            myFixture.configureByText("file.py", "class BookSpider(scrapy.Spider):\n    name = <caret>");

            // make sure that we remove events for focus (182.x seems to trigger focus events in tests, earlier versions don't)
            MockKiteApiService.getInstance().clearTestData();

            //after the initial setup to minimize events
            myFixture.complete(CompletionType.BASIC);

            // confirm the completion
            tester.typeWithPauses("\n");
        });

        Assert.assertEquals("Kite's insert value must be used to insert the completion into the document.", "class BookSpider(scrapy.Spider):\n    name = str\n    def parse()", myFixture.getEditor().getDocument().getText());
    }

    @Test
    public void testMultilineCompletionsPlaceholders() throws Throwable {
        runWithAutocompletionEnabled((tester) -> {
            MockKiteHttpConnection httpConnection = MockKiteHttpConnection.getInstance();
            httpConnection.addPostPathHandler("/clientapi/editor/complete", (path, payload) -> loadFile("json/multilineCompletions_placeholders.json"), getTestRootDisposable());
            httpConnection.addPostPathHandler("/clientapi/editor/event", (path, payload) -> "{}", getTestRootDisposable());

            MockKiteApiService.getInstance().enableHttpCalls();

            myFixture.configureByText("file.py", "class BookSpider(scrapy.Spider):\n    name = <caret>");

            // make sure that we remove events for focus (182.x seems to trigger focus events in tests, earlier versions don't)
            MockKiteApiService.getInstance().clearTestData();

            //after the initial setup to minimize events
            myFixture.complete(CompletionType.BASIC);

            // confirm the first completion, i.e. Kite's completion
            tester.typeWithPauses("\n");
        });

        Assert.assertEquals("Kite's insert value must be used to insert the completion into the document.", "class BookSpider(scrapy.Spider):\n    name = str\n    def parse()", myFixture.getEditor().getDocument().getText());
    }

    @Test
    public void testReplacementRange() throws Throwable {
        runWithAutocompletionEnabled((tester) -> {
            MockKiteHttpConnection httpConnection = MockKiteHttpConnection.getInstance();
            httpConnection.addPostPathHandler("/clientapi/editor/complete", (path, payload) -> loadFile("json/completionReplacementRange.json"), getTestRootDisposable());
            httpConnection.addPostPathHandler("/clientapi/editor/event", (path, payload) -> "{}", getTestRootDisposable());

            MockKiteApiService.getInstance().enableHttpCalls();

            myFixture.configureByFile("completionReplacementRange.py");

            // make sure that we remove events for focus (182.x seems to trigger focus events in tests, earlier versions don't)
            MockKiteApiService.getInstance().clearTestData();

            //after the initial setup to minimize events
            myFixture.complete(CompletionType.BASIC);

            String expected = "data = {\n" +
                    "    \"foo\": \"bar\"\n" +
                    "}\n" +
                    "data[\"foo\"]";
            Assert.assertEquals("Kite's replacement range must be used", expected, myFixture.getEditor().getDocument().getText());
        });
    }

    @Test
    public void testSpaceCompletion() throws Throwable {
        MockKiteHttpConnection httpConnection = MockKiteHttpConnection.getInstance();
        httpConnection.addPostPathHandler("/clientapi/editor/complete", (path, payload) -> loadFile("json/singleCompletion.json"), getTestRootDisposable());
        httpConnection.addPostPathHandler("/clientapi/editor/event", (path, payload) -> "{}", getTestRootDisposable());

        MockKiteApiService api = MockKiteApiService.getInstance();
        api.enableHttpCalls();

        KiteSettingsService.getInstance().getState().useNewCompletions = false;

        runWithAutocompletionEnabled((tester) -> {
            try {
                myFixture.configureByText(PythonFileType.INSTANCE, "");

                myFixture.type("prin");
                TestcaseEditorEventListener.sleepForQueueWork(getProject());
                tester.joinCompletion();
                long count = api.getCallHistory().stream().filter(e -> e.contains("completion")).count();
                Assert.assertTrue(1 <= count);

                myFixture.type(" ");
                TestcaseEditorEventListener.sleepForQueueWork(getProject());
                tester.joinCompletion();
                Assert.assertEquals("No completion after space expected in this context", count, api.getCallHistory().stream().filter(e -> e.contains("completion")).count());

                myFixture.type("\nimport");
                myFixture.type(" ");
                TestcaseEditorEventListener.sleepForQueueWork(getProject());
                tester.joinCompletion();
                Assert.assertTrue("Completion after space expected in import statement", count < api.getCallHistory().stream().filter(e -> e.contains("completion")).count());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testCustomTriggersCompletion() throws Throwable {
        MockKiteHttpConnection httpConnection = MockKiteHttpConnection.getInstance();
        // this completion test needs completion results with replacement ranges,
        // which are compatible with the current editor offset
        // we have to return non-empty completion results, because otherwise 192.x and later
        // will re-request completions with a typoAware prefix matcher
        httpConnection.addPostPathHandler("/clientapi/editor/complete", (path, payload) -> {
            String response = loadFile("json/singleCompletion_template.json");
            int offset = ReadAction.compute((ThrowableComputable<Integer, RuntimeException>) () -> {
                return myFixture.getEditor().getCaretModel().getOffset();
            });
            response = StringUtils.replace(response, "$BEGIN$", String.valueOf(offset));
            response = StringUtils.replace(response, "$END$", String.valueOf(offset));
            return response;
        }, getTestRootDisposable());
        httpConnection.addPostPathHandler("/clientapi/editor/event", (path, payload) -> "{}", getTestRootDisposable());

        MockKiteApiService api = MockKiteApiService.getInstance();
        api.enableHttpCalls();

        KiteSettingsService.getInstance().getState().useNewCompletions = true;

        Project project = getProject();
        runWithAutocompletionEnabled((tester) -> {
            try {
                myFixture.configureByText(PythonFileType.INSTANCE, "");

                tester.typeWithPauses("print");
                TestcaseEditorEventListener.sleepForQueueWork(project);
                tester.joinCompletion();
                long count1 = api.getCallHistory().stream().filter(e -> e.contains("completion")).count();
                Assert.assertTrue(1 <= count1);

                tester.typeWithPauses("(");
                TestcaseEditorEventListener.sleepForQueueWork(project);
                tester.joinCompletion();
                long count2 = api.getCallHistory().stream().filter(e -> e.contains("completion")).count();
                Assert.assertTrue("Completion after open paren expected", count1 < count2);

                tester.typeWithPauses("a");
                TestcaseEditorEventListener.sleepForQueueWork(project);
                Assert.assertEquals("Completion after 'a' expected", count2 + 1, api.getCallHistory().stream().filter(e -> e.contains("completion")).count());

                tester.typeWithPauses(",");
                TestcaseEditorEventListener.sleepForQueueWork(project);
                tester.joinCompletion();
                Assert.assertEquals("No completion after comma expected", count2 + 1, api.getCallHistory().stream().filter(e -> e.contains("completion")).count());

                tester.typeWithPauses(")");
                TestcaseEditorEventListener.sleepForQueueWork(project);
                tester.joinCompletion();
                Assert.assertEquals("no completion expected on closing paren", count2 + 1, api.getCallHistory().stream().filter(e -> e.contains("completion")).count());

                tester.typeWithPauses("\na");
                TestcaseEditorEventListener.sleepForQueueWork(project);
                tester.joinCompletion();
                Assert.assertEquals("completions after expected", count2 + 2, api.getCallHistory().stream().filter(e -> e.contains("completion")).count());

                tester.typeWithPauses("={");
                tester.joinCompletion();
                Assert.assertEquals("no completion expected on closing paren", count2 + 2, api.getCallHistory().stream().filter(e -> e.contains("completion")).count());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // test quotes
        api.clearTestData();
        runWithAutocompletionEnabled((tester) -> {
            try {
                myFixture.configureByText(PythonFileType.INSTANCE, "");

                Assert.assertEquals(0, api.getCallHistory().stream().filter(e -> e.contains("completion")).count());

                tester.typeWithPauses("foo");
                TestcaseEditorEventListener.sleepForQueueWork(project);
                tester.joinCompletion();
                long lastCount = api.getCallHistory().stream().filter(e -> e.contains("completion")).count();

                tester.typeWithPauses("[");
                TestcaseEditorEventListener.sleepForQueueWork(project);
                tester.joinCompletion();
                Assert.assertEquals(lastCount, api.getCallHistory().stream().filter(e -> e.contains("completion")).count());

                tester.typeWithPauses("'");
                TestcaseEditorEventListener.sleepForQueueWork(project);
                tester.joinCompletion();
                Assert.assertEquals(lastCount + 1, api.getCallHistory().stream().filter(e -> e.contains("completion")).count());

                tester.typeWithPauses("']\nfoo");
                TestcaseEditorEventListener.sleepForQueueWork(project);
                tester.joinCompletion();
                lastCount = api.getCallHistory().stream().filter(e -> e.contains("completion")).count();

                tester.typeWithPauses("[\"");
                TestcaseEditorEventListener.sleepForQueueWork(project);
                tester.joinCompletion();
                Assert.assertEquals(lastCount + 1, api.getCallHistory().stream().filter(e -> e.contains("completion")).count());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
