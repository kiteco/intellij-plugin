package com.kite.intellij.platform.exec;

import com.intellij.openapi.util.SystemInfoRt;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

public class GenericProcessLauncherTest extends KiteLightFixtureTest {
    @Test
    public void testUnixBaseExecutable() throws Exception {
        if (SystemInfoRt.isUnix) {
            ExecutableDetector detector = new PathFallbackExecutableDetector("ls", "echo");
            List<Path> cloudExecutables = detector.detectKiteExecutableFiles();

            GenericProcessLauncher launcher = new GenericProcessLauncher();

            Assert.assertFalse(cloudExecutables.isEmpty());
            Assert.assertTrue("The 'ls' process must be launched on unix systems", launcher.launch(cloudExecutables.get(0), false));
        }
    }
}