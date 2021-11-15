package com.kite.intellij.lang.psi;

import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.util.ThrowableRunnable;
import com.jetbrains.python.PythonFileType;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.backend.http.test.RequestInfo;
import com.kite.intellij.test.KiteLightFixtureTest;
import com.kite.intellij.ui.KiteTestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This test is not using a default write action and is not run on the EDT.
 *
  */
public class PsiChangeListenerComponentTest extends KiteLightFixtureTest {
    @Test
    public void testNonPythonFile() throws Throwable {
        MockKiteHttpConnection httpConnection = MockKiteHttpConnection.getInstance();
        Assert.assertTrue(httpConnection.getHttpRequestStringHistory().isEmpty());

        myFixture.configureByText("file.txt", "def foo():\n    pass\n<caret>");
        myFixture.type("x = foo");

        flush();

        List<RequestInfo> history = httpConnection.getHttpRequestHistory()
                .stream()
                .collect(Collectors.toList());

        Assert.assertTrue("The PSI change listener must reject non-python files", history.isEmpty());
    }

    @Test
    public void testEvents() throws Throwable {
        MockKiteHttpConnection httpConnection = MockKiteHttpConnection.getInstance();
        Assert.assertTrue(httpConnection.getHttpRequestStringHistory().isEmpty());

        myFixture.configureByText(PythonFileType.INSTANCE, "def foo():\n    pass\n<caret>");
        myFixture.type("x = foo");

        flush();

        List<RequestInfo> history = httpConnection.getHttpRequestHistory()
                .stream()
                .collect(Collectors.toList());

        Assert.assertEquals("expected a PSI change event and a corresponding HTTP request after an edit", 1, history.size());
    }

    @Test
    public void testEventsTooLargeFile() throws Throwable {
        MockKiteHttpConnection httpConnection = MockKiteHttpConnection.getInstance();
        Assert.assertTrue(httpConnection.getHttpRequestStringHistory().isEmpty());

        myFixture.configureByFile("test-data/python/editor/events/fileSizeExceeded.py");
        myFixture.type("\nimport");

        flush();

        List<RequestInfo> history = httpConnection.getHttpRequestHistory();

        Assert.assertEquals("References must not be analyzed for files exceeding the maximum file size", 0, history.size());
    }

    @Override
    protected void invokeTestRunnable(@NotNull Runnable runnable) throws Exception {
        runnable.run();
    }

    @Override
    protected boolean runInDispatchThread() {
        return false;
    }

    @Override
    protected String getBasePath() {
        return "";
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    private void flush() throws Throwable {
        // commit the PSI to trigger our change listener
        EdtTestUtil.runInEdtAndWait(() -> PsiDocumentManager.getInstance(getProject()).commitAllDocuments());

        // wait until our highlighting pass was executed
        myFixture.checkHighlighting();
        KiteTestUtil.runInEdtAndWait(EmptyRunnable.getInstance());
    }
}