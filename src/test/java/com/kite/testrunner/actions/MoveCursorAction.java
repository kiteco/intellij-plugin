package com.kite.testrunner.actions;

import com.kite.testrunner.TestContext;
import com.kite.testrunner.TestRunnerUtil;

public class MoveCursorAction implements TestAction {
    @Override
    public String getId() {
        return "move_cursor";
    }

    @Override
    public void run(TestContext context) throws Throwable {
        int offset = context.getStep().getIntProperty("offset", null);
        context.getFixture().getEditor().getCaretModel().moveToOffset(offset);

        String relPath = TestRunnerUtil.relativePathCurrentFile(context);
        context.putContextProperty(String.format("editors.%s.offset", relPath), String.valueOf(offset));
    }

}
