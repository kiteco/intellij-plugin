package com.kite.testrunner.expectations;

import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.testrunner.TestContext;
import com.kite.testrunner.TestFailedException;
import com.kite.testrunner.TestRunnerUtil;
import com.kite.testrunner.model.TestStep;

/**
 * Checks the count of requests matching method and path.
 *
  */
public class RequestCountExpectation implements TestExpectation, RetryableExpectation {
    @Override
    public String getId() {
        return "request_count";
    }

    @Override
    public void run(TestContext context) {
        TestStep step = context.getStep();
        String method = step.getStringProperty("method", null);
        String path = step.getStringProperty("path", null);
        int expectedCount = step.getIntProperty("count", null);

        String finalPath = TestRunnerUtil.resolvePlaceholders(path, context);

        long actualCount = MockKiteHttpConnection.getInstance().getHttpRequestHistory().stream()
                .filter(r -> method.equals(r.getMethod()) && finalPath.equals(r.getPathWithQuery()))
                .count();

        if (expectedCount != actualCount) {
            throw new TestFailedException(context, String.format("expected count %d != %d (actual count)", expectedCount, actualCount));
        }
    }
}
