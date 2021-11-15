package com.kite.intellij.platform.exec;

import com.intellij.openapi.util.SystemInfoRt;
import org.junit.Assert;
import org.junit.Test;

public class PathFallbackExecutableDetectorTest {
    @Test
    public void testDetectBaseExecutable() {
        String executableName = SystemInfoRt.isWindows ? "cmd.exe" : "ls";

        PathFallbackExecutableDetector detector = new PathFallbackExecutableDetector(executableName, executableName);

        Assert.assertFalse("The base executable should be found on any OS", detector.detectKiteExecutableFiles().isEmpty());
    }
}