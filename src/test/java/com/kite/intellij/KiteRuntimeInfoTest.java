package com.kite.intellij;

import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 */
public class KiteRuntimeInfoTest extends KiteLightFixtureTest {
    @Test
    public void testProperties() throws Exception {
        Map<String, Object> properties = KiteRuntimeInfo.reportPluginProperties("1.0.0");

        Assert.assertTrue(properties.containsKey("os"));
        Assert.assertTrue(properties.containsKey("os_version"));
        Assert.assertTrue(properties.containsKey("os_arch"));

        Assert.assertTrue(properties.containsKey("editor_platform"));
        Assert.assertTrue(properties.containsKey("editor_productCode"));
        Assert.assertTrue(properties.containsKey("editor_version"));

        Assert.assertTrue(properties.containsKey("java_version"));
        Assert.assertTrue(properties.containsKey("java_vendor"));
    }
}