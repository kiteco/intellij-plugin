package com.kite.testrunner.actions;

import com.kite.testrunner.TestContext;
import com.kite.testrunner.TestStepHelper;

public interface TestAction extends TestStepHelper {
    String getId();

    void run(TestContext context) throws Throwable;

    default boolean runInEventDispatchThread() {
        return true;
    }
}
