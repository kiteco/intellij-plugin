package com.kite.testrunner.expectations;

import com.kite.testrunner.TestContext;
import com.kite.testrunner.TestStepHelper;

public interface TestExpectation extends TestStepHelper {
    String getId();

    void run(TestContext context);
}

