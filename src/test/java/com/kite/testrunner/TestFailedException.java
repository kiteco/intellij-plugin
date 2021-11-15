package com.kite.testrunner;

public class TestFailedException extends RuntimeException {
    public TestFailedException(TestContext context, String message) {
        super(msg(context, message));
    }

    public TestFailedException(TestContext context, String message, Throwable cause) {
        super(msg(context, message), cause);
    }

    private static String msg(TestContext context, String message) {
        return String.format("[Step %d] %s. %s", context.getStepIndex(), message, contextString(context
        ));
    }

    private static String contextString(TestContext context) {
        String description = "";
        if (context.getStep() != null) {
            description = context.getStep().description;
        }

        return String.format("{step: %s, file: %s, %s}", context.getStepIndex(), context.getTestFilePath(), description);
    }
}
