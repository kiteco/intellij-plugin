package com.kite.testrunner.expectations;

import com.kite.testrunner.TestContext;
import com.kite.testrunner.TestFailedException;
import com.kite.testrunner.model.NotificationInfo;
import com.kite.testrunner.model.TestStep;

import java.util.List;

public class NotificationCountExpectation implements TestExpectation {
    @Override
    public String getId() {
        return "notification_count";
    }

    @Override
    public void run(TestContext context) {
        TestStep step = context.getStep();
        String level = step.getStringProperty("level", "");
        int expectedCount = step.getIntProperty("count", null);

        long actualCount = context.getOpenNotifications().stream().filter(n -> n.getLevel().equals(level)).count();
        if (expectedCount != actualCount) {
            throw new TestFailedException(context, String.format("expected notification count %d != %d (actual)", expectedCount, actualCount));
        }
    }
}
