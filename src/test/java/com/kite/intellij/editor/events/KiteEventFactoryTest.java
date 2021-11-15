package com.kite.intellij.editor.events;

import com.kite.intellij.platform.fs.UnixCanonicalPath;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

public class KiteEventFactoryTest extends KiteLightFixtureTest {
    @Test
    public void testEmptySkipEventContent() {
        KiteEvent event = KiteEventFactory.createSkipEvent(new UnixCanonicalPath("/home/user/test.py"));
        Assert.assertTrue("Content must be empty", event.getContent().isEmpty());
    }
}