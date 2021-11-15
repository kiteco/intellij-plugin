package com.kite.testrunner.actions;

import com.kite.testrunner.TestContext;

public class RequestCompletionAction implements TestAction {
    @Override
    public String getId() {
        return "request_completion";
    }

    @Override
    public void run(TestContext context) throws Throwable {
        context.getFixture().completeBasic();
    }
}
