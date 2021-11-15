package com.kite.intellij.platform.exec;

import com.google.common.collect.Lists;
import com.intellij.openapi.util.SystemInfo;
import com.kite.intellij.KiteRuntimeInfo;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

public class MacExecutableDetectorTest extends KiteLightFixtureTest {
    @Test
    public void testDetectNotFound() throws Exception {
        if (SystemInfo.isMac) {
            MacExecutableDetector detector = new MacExecutableDetector("com.kite.NotInstalledKite", "enterprise.kite.NotInstalledKite");
            Assert.assertTrue("Cloud application must not be detected", detector.detectKiteExecutableFiles().isEmpty());
        }
    }

    @Test
    public void testMultipleLocations() throws Exception {
        if ("win".equals(KiteRuntimeInfo.osName())) {
            //test isn't compatible with Windows
            return;
        }

        MacExecutableDetector detector = new MacExecutableDetector();
        List<Path> locations = Lists.newArrayList(
                detector.findApplicationPath("/Users/myUser/Dev/Debug/Kite.app"),
                detector.findApplicationPath("/Applications/Kite.app")
        );

        Assert.assertEquals("/Users/myUser/Dev/Debug/Kite.app", locations.get(0).toString());
        Assert.assertEquals("/Applications/Kite.app", locations.get(1).toString());
    }

    @Test
    public void testCommandLine() throws Exception {
        MacExecutableDetector detector = new MacExecutableDetector();

        Assert.assertEquals("mdfind kMDItemCFBundleIdentifier=\\\"com.kite.Kite\\\"", detector.createCommandLine(detector.getApplicationId()).getCommandLineString());

        Assert.assertEquals("mdfind kMDItemCFBundleIdentifier=\\\"enterprise.kite.Kite\\\"", detector.createCommandLine(detector.getApplicationEnterpriseId()).getCommandLineString());
    }

    @Override
    protected String getBasePath() {
        return "platform/mac";
    }
}