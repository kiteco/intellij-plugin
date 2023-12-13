package com.kite.intellij.platform;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.SystemInfo;
import com.kite.intellij.platform.exec.ExecutableDetector;
import com.kite.intellij.platform.exec.GenericProcessLauncher;
import com.kite.intellij.platform.exec.GenericProcessWatcher;
import com.kite.intellij.platform.exec.KiteProcessLauncher;
import com.kite.intellij.platform.exec.KiteProcessWatcher;
import com.kite.intellij.platform.exec.KiteWindowsRegistryExecutableDetector;
import com.kite.intellij.platform.exec.LinuxFallbackExecutableDetector;
import com.kite.intellij.platform.exec.MacApplicationBundleLauncher;
import com.kite.intellij.platform.exec.MacExecutableDetector;
import com.kite.intellij.platform.exec.MacProcessBundleWatcher;
import com.kite.intellij.platform.exec.NoOpExecutableDetector;
import com.kite.intellij.platform.exec.NoOpProcessLauncher;
import com.kite.intellij.platform.exec.NoOpProcessWatcher;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalInt;

/**
 * Application service which displays a warning if the current OS or OS version is not supported by Kite.
 */
public class KiteDetector implements ExecutableDetector, KiteProcessLauncher, KiteProcessWatcher {
    private final ExecutableDetector executableDetector;
    private final KiteProcessLauncher processLauncher;
    private final KiteProcessWatcher processWatcher;

    public KiteDetector() {
        if (SystemInfo.isWindows) {
            executableDetector = new KiteWindowsRegistryExecutableDetector("kited.exe");
            processWatcher = new GenericProcessWatcher("kited.exe");
            processLauncher = new GenericProcessLauncher();
        } else if (SystemInfo.isMac) {
            executableDetector = new MacExecutableDetector();
            processWatcher = new MacProcessBundleWatcher("Kite.app");
            processLauncher = new MacApplicationBundleLauncher();
        } else if (SystemInfo.isUnix) {
            executableDetector = new LinuxFallbackExecutableDetector();
            processWatcher = new GenericProcessWatcher("kited");
            processLauncher = new GenericProcessLauncher();
        } else {
            executableDetector = new NoOpExecutableDetector();
            processWatcher = new NoOpProcessWatcher();
            processLauncher = new NoOpProcessLauncher();
        }
    }

    @Nonnull
    public static KiteDetector getInstance() {
        return ApplicationManager.getApplication().getService(KiteDetector.class);
    }

    @Override
    public List<Path> detectKiteExecutableFiles() {
        return executableDetector.detectKiteExecutableFiles();
    }

    @Override
    public boolean launch(@Nonnull Path executable, boolean withCopilot) {
        return processLauncher.launch(executable, withCopilot);
    }

    @Nonnull
    @Override
    public OptionalInt detectKiteProcessId() {
        return processWatcher.detectKiteProcessId();
    }

    @Override
    public boolean wasLaunched() {
        return processWatcher.wasLaunched();
    }
}
