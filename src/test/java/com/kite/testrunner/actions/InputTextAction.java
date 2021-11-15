package com.kite.testrunner.actions;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.lookup.LookupEx;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CompletionAutoPopupTester;
import com.intellij.util.ThrowableRunnable;
import com.kite.testrunner.TestContext;
import com.kite.testrunner.TestRunnerUtil;
import com.kite.testrunner.model.TestStep;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;

public class InputTextAction implements TestAction {
    private static final Logger LOG = Logger.getInstance("#kite.testRunner");

    @Override
    public String getId() {
        return "input_text";
    }

    @Override
    public void run(TestContext context) throws Throwable {
        LookupEx activeLookup = LookupManager.getActiveLookup(context.getFixture().getEditor());
        if (activeLookup != null) {
            LOG.debug("Hiding active lookup before text is entered");
            EdtTestUtil.runInEdtAndWait((ThrowableRunnable<Throwable>) () -> {
                LookupManager.getInstance(context.getProject()).hideActiveLookup();
            });
        }

        TestStep step = context.getStep();
        String input = step.getStringProperty("text", "");
        if (input.isEmpty()) {
            return;
        }

        Application app = ApplicationManager.getApplication();

        CompletionAutoPopupTester tester = new CompletionAutoPopupTester(context.getFixture());
        tester.runWithAutoPopupEnabled(() -> {
            boolean prevAutoInsertSetting = CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET;
            try {
                CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET = false;
                context.getFixture().type(input);

                // taken from test.typeWithPauses
                tester.joinAutopopup();
                tester.joinCompletion();
            } finally {
                CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET = prevAutoInsertSetting;
            }
        });

        app.runReadAction(() -> {
            String relPath = TestRunnerUtil.relativePathCurrentFile(context);
            Document document = context.getFixture().getEditor().getDocument();
            String contentHash = DigestUtils.md5Hex(document.getText().getBytes(StandardCharsets.UTF_8));

            context.putContextProperty(String.format("editors.%s.hash", relPath), contentHash);
        });

        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public boolean runInEventDispatchThread() {
        // we wait after typing, this must not block the EDT
        return false;
    }
}
