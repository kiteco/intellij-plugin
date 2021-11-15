package com.kite.testrunner.expectations;

import com.kite.intellij.backend.MockKiteApiService;
import com.kite.testrunner.TestContext;
import com.kite.testrunner.TestFailedException;

/**
 * Checks the count of requests matching method and path.
 *
  */
public class HoverDataExpectation implements TestExpectation {
    @Override
    public String getId() {
        return "hover_data";
    }

    @Override
    public void run(TestContext context) {
        try {
            // the processing of the hover request may take a while
            // the runner flushed the event, now we're waiting that the handling of this event is finished
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        boolean ok = MockKiteApiService.getInstance().getCounterEventHistory().contains("incrementCounter(intellij_hover_fulfilled, 1)");
        if (!ok) {
            throw new TestFailedException(context, "hover_data not found in counter history");
        }
    }
}
