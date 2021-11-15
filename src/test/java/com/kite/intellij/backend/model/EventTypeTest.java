package com.kite.intellij.backend.model;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class EventTypeTest {
    @Test
    public void testIds() throws Exception {
        Assert.assertEquals("edit", EventType.EDIT.asKiteId());
        Assert.assertEquals("focus", EventType.FOCUS.asKiteId());
        Assert.assertEquals("selection", EventType.SELECTION.asKiteId());
    }
}