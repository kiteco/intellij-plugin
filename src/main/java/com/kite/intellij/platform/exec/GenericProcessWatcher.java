package com.kite.intellij.platform.exec;

import com.intellij.execution.process.ProcessInfo;
import com.intellij.execution.process.impl.ProcessListUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.SystemProperties;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.OptionalInt;

/**
 * Generic process watcher which takes an executable name and searches for its PID in the current list of running processes.
 *
  */
public class GenericProcessWatcher implements KiteProcessWatcher {
    private static final Logger LOG = Logger.getInstance("#kite.process");
    private final String executableName;

    public GenericProcessWatcher(String executableName) {
        this.executableName = executableName;
    }

    @Nonnull
    @Override
    public OptionalInt detectKiteProcessId() {
        try {
            ProcessInfo[] processList = ProcessListUtil.getProcessList();
            return Arrays.stream(processList)
                    .filter(processInfo -> executableName.equalsIgnoreCase(processInfo.getExecutableName()))
                    .mapToInt(ProcessInfo::getPid)
                    .findFirst();
        } catch (Throwable e) {
            LOG.warn("error retrieving process list", e);
            return OptionalInt.empty();
        }
    }

    @Override
    public boolean wasLaunched() {
        String home = SystemProperties.getUserHome();
        if (SystemInfo.isWindows) {
            String localAppData = StringUtils.defaultIfEmpty(System.getenv("LOCALAPPDATA"), home);
            return Files.isReadable(Paths.get(localAppData, "Kite", "kited_has_run"));
        }

        // Linux
        return Files.isReadable(Paths.get(home, ".kite", "kited_has_run"));
    }
}
