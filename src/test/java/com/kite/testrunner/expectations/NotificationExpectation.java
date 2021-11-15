package com.kite.testrunner.expectations;

import com.kite.testrunner.TestContext;
import com.kite.testrunner.TestFailedException;
import com.kite.testrunner.TestRunnerUtil;
import com.kite.testrunner.model.NotificationInfo;
import com.kite.testrunner.model.TestStep;

import java.util.List;

public class NotificationExpectation implements TestExpectation {
    @Override
    public String getId() {
        return "notification";
    }

    @Override
    public void run(TestContext context) {
        TestStep step = context.getStep();
        String level = step.getStringProperty("level", "");
        String message = TestRunnerUtil.resolvePlaceholders(step.getStringProperty("message", ""), context);

        List<NotificationInfo> notifications = context.getOpenNotifications();
        for (NotificationInfo n: notifications) {
            if (level.equals(n.getLevel()) /*&& message.equalsIgnoreCase(n.getMessage())*/) {
                return;
            }
        }

        throw new TestFailedException(context, String.format("Notification not found. Expected: level=%s, message=%s", level, message));
    }
}
