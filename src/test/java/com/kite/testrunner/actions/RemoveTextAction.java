package com.kite.testrunner.actions;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.CompletionAutoPopupTester;
import com.kite.intellij.ui.KiteTestUtil;
import com.kite.testrunner.TestContext;
import com.kite.testrunner.TestRunnerUtil;
import com.kite.testrunner.model.TestStep;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.StandardCharsets;

public class RemoveTextAction implements TestAction {
    @Override
    public String getId() {
        return "remove_text";
    }

    @Override
    public void run(TestContext context) throws Throwable {
        TestStep step = context.getStep();
        int fromOffset = step.getIntProperty("from_offset", null);
        int toOffset = step.getIntProperty("to_offset", null);

        Application app = ApplicationManager.getApplication();
        CodeInsightTestFixture fixture = context.getFixture();

        try {
            KiteTestUtil.runInEdtAndWait(() -> fixture.getEditor().getCaretModel().moveToOffset(Math.max(fromOffset, toOffset)));
        } catch (Throwable throwable) {
            //ignore
        }

        CompletionAutoPopupTester tester = new CompletionAutoPopupTester(fixture);
        try {
            tester.runWithAutoPopupEnabled(() -> {
                boolean prevAutoInsertSetting = CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET;
                try {
                    CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET = false;

                    String input = StringUtils.repeat("\b", Math.abs(fromOffset - toOffset));
                    fixture.type(input);

                    // taken from test.typeWithPauses
                    tester.joinAutopopup();
                    tester.joinCompletion();
                } finally {
                    CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET = prevAutoInsertSetting;
                }
            });
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }

        app.runReadAction(() -> {
            String relPath = TestRunnerUtil.relativePathCurrentFile(context);
            Document document = fixture.getEditor().getDocument();
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
