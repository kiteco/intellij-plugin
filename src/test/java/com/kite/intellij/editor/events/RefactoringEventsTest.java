package com.kite.intellij.editor.events;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.psi.PsiDocumentManager;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Tests event handling for refactorings.
 *
  */
public class RefactoringEventsTest extends KiteLightFixtureTest {

    /**
     * This test renames a function in start.py
     * <p>
     * Events must only be generated for the file of the current editor.
     * Changes in other files, e.g. caused by a refactoring, should be ignored
     * Kite will read the changes when the other files are synced to disk and only supports
     * content tracking for the current file which has the focus
     * (conclusion of a talk with Juan at 2017-06-15)
     */
    @Test
    public void testRenameRefactoring() throws Exception {
        // branch idea-193+ is fixing this in a better way
        int ideMajorBuild = ApplicationInfo.getInstance().getBuild().getBaselineVersion();
        String sourceFile = ideMajorBuild >= 193 ? "src/start.py" : "src/start181.py";
        String expectedFile = ideMajorBuild >= 193 ? "expected/start.py" : "expected/start181.py";

        Assert.assertTrue(getKiteApiService().getCallHistory().isEmpty());

        myFixture.configureByFiles(sourceFile, "src/definition.py");

        myFixture.renameElementAtCaret("NewFunctionName");
        PsiDocumentManager.getInstance(getProject()).commitAllDocuments();

        myFixture.checkResultByFile(expectedFile);

        TestcaseEditorEventListener.sleepForQueueWork(getProject());

        List<String> callHistory = getKiteApiService().getCallHistory();
        Assert.assertFalse("Calls should not be empty: " + callHistory, callHistory.isEmpty());
        Assert.assertTrue("Expected start.py to be causing events", getKiteApiService().getProcessedEventFiles().stream().anyMatch(f -> f.contains(sourceFile)));
        Assert.assertFalse("Expected definition.py to generate no events", getKiteApiService().getProcessedEventFiles().contains("/src/src/definition.py"));
    }

    @Override
    protected String getBasePath() {
        return "python/editor/refactoringEvents";
    }
}
