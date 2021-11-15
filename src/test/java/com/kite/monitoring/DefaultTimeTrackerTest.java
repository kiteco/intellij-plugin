package com.kite.monitoring;

import com.intellij.openapi.diagnostic.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 */
public class DefaultTimeTrackerTest {
    @Test
    public void testTracking() throws InterruptedException {
        TimeTracker tracker;
        try (TimeTracker ignored = new DefaultTimeTracker("test", Logger.getInstance("kite.test")).start()) {
            tracker = ignored;

            Thread.sleep(550);
        }

        Assert.assertTrue(tracker.isStopped());
        Assert.assertTrue("duration must at least be 500ms: " + tracker.getMillisDuration(), tracker.getMillisDuration() >= 500);
        Assert.assertTrue("The duration must be in a reasonable range: " + tracker.getMillisDuration(), tracker.getMillisDuration() <= 5000);

        Assert.assertTrue(tracker.getNanoDuration() >= 500_000_000);
    }
}