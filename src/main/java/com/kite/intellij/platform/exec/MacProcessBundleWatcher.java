package com.kite.intellij.platform.exec;

import com.intellij.execution.process.ProcessInfo;
import com.intellij.execution.process.impl.ProcessListUtil;
import com.intellij.util.SystemProperties;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.OptionalInt;

/**
 * Mac implementation of a process watcher.
 * The executable is an executable in the Kite.app subpath, we return the first process which contains "/Kite.app/" in
 * its command line.
 *
  */
public class MacProcessBundleWatcher implements KiteProcessWatcher {
    private final String appBundleName;

    public MacProcessBundleWatcher(String appBundleName) {
        this.appBundleName = appBundleName;
    }

    @Nonnull
    @Override
    public OptionalInt detectKiteProcessId() {
        ProcessInfo[] processList = ProcessListUtil.getProcessList();
        return Arrays.stream(processList)
                .filter(info -> info.getCommandLine().contains("/" + appBundleName + "/"))
                .mapToInt(ProcessInfo::getPid)
                .findFirst();
    }

    @Override
    public boolean wasLaunched() {
        String home = SystemProperties.getUserHome();
        return Files.isReadable(Paths.get(home, ".kite", "kited_has_run"));
    }
}
