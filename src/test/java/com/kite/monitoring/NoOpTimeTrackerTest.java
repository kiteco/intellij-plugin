package com.kite.monitoring;

import org.junit.Assert;
import org.junit.Test;

public class NoOpTimeTrackerTest {
    @Test
    public void testBasics() throws Exception {
        NoOpTimeTracker t = new NoOpTimeTracker();
        t.start();
        Thread.sleep(100);
        t.stop();

        Assert.assertEquals(0, t.getMillisDuration(), 0.1d);
        Assert.assertEquals(0, t.getNanoDuration());
    }
}