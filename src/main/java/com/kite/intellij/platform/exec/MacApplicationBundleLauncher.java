package com.kite.intellij.platform.exec;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Launches mac application bundles.
 *
  */
public class MacApplicationBundleLauncher implements KiteProcessLauncher {
    public MacApplicationBundleLauncher() {
    }

    @Override
    public boolean launch(@Nonnull Path appPackageDir, boolean withCopilot) {
        if (!Files.isDirectory(appPackageDir)) {
            return false;
        }

        GeneralCommandLine commandLine = new GeneralCommandLine("open", "-a", appPackageDir.toAbsolutePath().toString())
                .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.SYSTEM)
                .withWorkDirectory(System.getProperty("user.home"));

        if (withCopilot) {
            commandLine.addParameters("--args", "--plugin-launch-with-copilot", "--channel=jetbrains");
        } else {
            commandLine.addParameters("--args", "--plugin-launch");
        }

        try {
            KiteDetachedProcessHandler processHandler = new KiteDetachedProcessHandler(commandLine);
            processHandler.startNotify();

            return true;
        } catch (ExecutionException e) {
            return false;
        }
    }
}
