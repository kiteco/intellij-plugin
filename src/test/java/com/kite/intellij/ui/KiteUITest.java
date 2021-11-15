package com.kite.intellij.ui;

import com.intellij.util.ui.JBUI;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 */
public class KiteUITest extends KiteLightFixtureTest {

    /**
     * Test the scale factor check on 145.x (which has a "float com.intellij.util.ui.JBUI#SCALE_FACTOR")
     */
    @Test
    public void testFontScaling2016_1() throws Exception {
        runWithFixedIntelliJScaleFactor("SCALE_FACTOR");
    }

    @Test
    public void testFontScaling2017_1() throws Exception {
        runWithFixedIntelliJScaleFactor("userScaleFactor");
    }

    protected void runWithFixedIntelliJScaleFactor(String fieldName) throws IllegalAccessException {
        Field scaleFactor = null;
        float oldValue = 1.0f;

        try {
            scaleFactor = JBUI.class.getDeclaredField(fieldName);
            scaleFactor.setAccessible(true);
            oldValue = scaleFactor.getFloat(JBUI.class);

            //we run this test with a scale factor of 2 to make sure that KiteUI's code works (and not just falls back to 1)
            scaleFactor.setFloat(JBUI.class, 2.0f);

            Assert.assertEquals("", 2.0f, JBUI.scaleFontSize(1), 0.01f);

            //make sure that KiteUI and JBUI ouput the same values
            Assert.assertEquals(KiteUI.scaleFontSize(0), JBUI.scaleFontSize(0));
            Assert.assertEquals(KiteUI.scaleFontSize(1), JBUI.scaleFontSize(1));
            Assert.assertEquals(KiteUI.scaleFontSize(12), JBUI.scaleFontSize(12));
            Assert.assertEquals(KiteUI.scaleFontSize(120), JBUI.scaleFontSize(120));
        } catch (NoSuchFieldException e) {
            //ignored if not on 2017.1.x
        } finally {
            if (scaleFactor != null) {
                scaleFactor.setFloat(JBUI.class, oldValue);
            }
        }
    }
}