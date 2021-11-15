package com.kite.intellij.platform.exec;

import com.google.common.collect.Lists;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class KiteWindowsRegistryExecutableDetectorTest extends KiteLightFixtureTest {
    @Test
    public void testExtractName() throws Exception {
        KiteWindowsRegistryExecutableDetector detector = new KiteWindowsRegistryExecutableDetector();

        List<Path> path = detector.findRegistryInstallLocation(Lists.newArrayList(
                "        HKEY_LOCAL_MACHINE\\Software\\Kite\\AppData",
                "        InstallPath    REG_SZ    C:\\Program Files\\Kite"
        ));

        Assert.assertFalse("The location must be extracted from the 'reg' command's output", path.isEmpty());
        Assert.assertEquals("The location must be extracted from the 'reg' command's output", "C:\\Program Files\\Kite", path.get(0).toString());
    }

    @Test
    public void testFindExeFile() throws Exception {
        KiteWindowsRegistryExecutableDetector detector = new KiteWindowsRegistryExecutableDetector("kited.exe.txt");

        Path installLocation = Paths.get(getTestDataPath(), "c", "program files", "kite");
        Path exeLocation = detector.findReadableProgramFile(installLocation);

        Assert.assertNotNull("The file's location must have been found", exeLocation);
        Assert.assertEquals("The file's location must have been found", installLocation.resolve("kited.exe.txt").toString(), exeLocation.toString());
    }

    @Test
    public void testFindExeFileFailing() throws Exception {
        KiteWindowsRegistryExecutableDetector detector = new KiteWindowsRegistryExecutableDetector("notExisting.txt");

        Path installLocation = Paths.get(getTestDataPath(), "c", "program files", "kite");
        Path exeLocation = detector.findReadableProgramFile(installLocation);

        Assert.assertNull("The file must not have been found", exeLocation);
    }

    @Test
    public void testExtractNameFailing() throws Exception {
        KiteWindowsRegistryExecutableDetector detector = new KiteWindowsRegistryExecutableDetector();

        List<Path> path = detector.findRegistryInstallLocation(Lists.newArrayList(
                "        some output not matching the expected output"
        ));

        Assert.assertNotNull("The location must be extracted from the 'reg' command's output", path);
    }

    @Override
    protected String getBasePath() {
        return "platform/win";
    }
}