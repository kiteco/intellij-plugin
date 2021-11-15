package com.kite.intellij.platform.exec;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Forks a process on unix systems. Please not, that this implementation doesen't handle Mac OS X application bundles,
 * only traditional unix executables.
 * <p>
 * The launches process will receive the same environment as the IntelliJ parent process.
 * The process's working directory is the user's home directory.
 * <p>
 * stdout and stderr will be redirected to /dev/null if the current OS is a UNIX system and /dev/null is writable
 * for the current user (which should be always the case).
 * Both streams will be redirected to NUL if the current OS is windows.
 *
  */
public class GenericProcessLauncher implements KiteProcessLauncher {
    private static final Logger LOG = Logger.getInstance("#kite.launcher.unix");

    public GenericProcessLauncher() {
    }

    @Override
    public boolean launch(@Nonnull Path executable, boolean withCopilot) {
        KiteGeneralCommandLine commandLine = new KiteGeneralCommandLine(executable.toAbsolutePath().toString());
        commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.SYSTEM);
        commandLine.withWorkDirectory(System.getProperty("user.home"));
        if (withCopilot) {
            commandLine.addParameters("--plugin-launch-with-copilot", "--channel=jetbrains");
        } else {
            commandLine.withEnvironment("KITE_SKIP_ONBOARDING", "1");
        }

        if (SystemInfo.isUnix && Files.isWritable(Paths.get("/dev/null"))) {
            commandLine.setStdoutRedirect(ProcessBuilder.Redirect.to(new File("/dev/null")));
        } else if (SystemInfo.isWindows) {
            commandLine.setStdoutRedirect(ProcessBuilder.Redirect.to(new File("NUL")));
        }

        commandLine.withRedirectErrorStream(true);

        try {
            KiteDetachedProcessHandler handler = new KiteDetachedProcessHandler(commandLine);
            handler.startNotify();

            return true;
        } catch (ExecutionException e) {
            LOG.warn("Error launching kite", e);
            return false;
        }
    }
}
