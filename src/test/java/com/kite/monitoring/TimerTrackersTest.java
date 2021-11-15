package com.kite.monitoring;

import org.junit.Assert;
import org.junit.Test;

public class TimerTrackersTest {
    @Test
    public void testBasics() throws Exception {
        Assert.assertFalse("Must not be enabled in unit tests", TimerTrackers.isEnabled());
    }
}