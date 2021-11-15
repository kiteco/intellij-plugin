package com.kite.intellij.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CompletionAutoPopupTester;
import com.jetbrains.python.PythonFileType;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Make sure that livetemplate handling is not broken by Kite.
 */
public class EditorLiveTemplateTest extends KiteLightFixtureTest {
    protected CompletionAutoPopupTester typeTester;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        typeTester = new CompletionAutoPopupTester(myFixture);
    }

    @Override
    protected String getBasePath() {
        return "python/editor/liveTemplates";
    }

    @Test
    public void testReturnTemplate() throws Exception {
        if (ApplicationInfoEx.getInstanceEx().getBuild().getBaselineVersion() >= 203) {
            // fixme so far, this test doesn't work with 2020.3. Revisit with a later eap version.
            return;
        }

        getKiteApiService().enableHttpCalls();
        //we must not return 200 or 404 because empty completions block PyCharms completions
        //this test assumes that PyCharms code is called
        MockKiteHttpConnection.getInstance().addPostPathHandler("/clientapi/editor/complete", (path, payload) -> {
            throw new HttpStatusException("unknown", 500, "");
        }, getTestRootDisposable());

        PsiFile file = myFixture.configureByText(PythonFileType.INSTANCE, "def x:\n\t<caret>");

        //163.x complains about non-open editor
        ApplicationManager.getApplication().invokeLater(() -> myFixture.openFileInEditor(file.getVirtualFile()));

        type("ret");

        List<String> lookupStrings = myFixture.getLookupElementStrings();
        Assert.assertNotNull(lookupStrings);
        Assert.assertTrue(lookupStrings.contains("return"));

        type("\t");
        type("1"); //enter some text after return to make sure that the "remove whitespace at line end doesn't remove trailing whitespace"

        ApplicationManager.getApplication().invokeLater(() -> {
            PsiDocumentManager.getInstance(getProject()).commitAllDocuments();

            Assert.assertEquals("def x:\n\treturn 1", file.getText());
        });
    }

    protected void type(String text) {
        typeTester.typeWithPauses(text);
    }

    //@Override // not in 2020.3+
    protected void invokeTestRunnable(@Nonnull Runnable runnable) {
        try {
            typeTester.runWithAutoPopupEnabled(() -> runnable.run());
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    @Override
    protected boolean runInDispatchThread() {
        return false;
    }
}
