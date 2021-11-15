package com.kite.testrunner.actions;

import com.kite.testrunner.TestContext;
import com.kite.testrunner.model.TestStep;

public class NewFileAction extends OpenFileAction {
    @Override
    public String getId() {
        return "new_file";
    }

    @Override
    protected String fileContent(TestContext context, TestStep step) {
        return step.getStringProperty("content", "");
    }
}
