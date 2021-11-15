package com.kite.intellij.platform;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.util.ExecUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.DumbProgressIndicator;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.io.HttpRequests;
import com.kite.intellij.platform.exec.KiteGeneralCommandLine;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * The result of "canInstall" is cached until the next restart because it's expensive to retrieve.
 */
public class DefaultKiteInstallService implements KiteInstallService {
    private static final Logger LOG = Logger.getInstance("#kite.installer");

    @Nullable
    private final String setupURL;
    @Nullable
    private final String setupFilename;
    private volatile boolean isInstalling;
    @Nullable
    private volatile Boolean canInstallCached;

    public DefaultKiteInstallService() {
        if (SystemInfo.isMac) {
            setupURL = "https://release.kite.com/dls/mac/current";
            setupFilename = "Kite.dmg";
        } else if (SystemInfo.isWindows) {
            setupURL = "https://release.kite.com/dls/windows/current";
            setupFilename = "KiteSetup.exe";
        } else if (SystemInfo.isLinux) {
            setupURL = "https://release.kite.com/dls/linux/current";
            setupFilename = "kite_installer.sh";
        } else {
            setupURL = null;
            setupFilename = null;
        }
    }

    @Override
    public boolean isInstalling() {
        return isInstalling;
    }

    @Override
    public boolean canInstall() {
        if (setupURL == null || setupFilename == null) {
            throw new IllegalStateException("unsupported OS");
        }

        assertNotDispatchThread();

        Boolean cachedValue = canInstallCached;
        if (cachedValue != null) {
            return cachedValue;
        }

        LOG.debug("Testing availability of Kite installer: " + setupURL);
        try {
            int statusCode = HttpRequests.head(setupURL)
                    .productNameAsUserAgent()
                    .tryConnect();
            boolean canInstall = statusCode == HttpStatus.SC_OK;
            canInstallCached = canInstall;
            return canInstall;
        } catch (IOException e) {
            canInstallCached = false;
            return false;
        }
    }

    @Override
    public @NotNull File downloadSetup() throws IOException {
        if (setupURL == null || setupFilename == null) {
            throw new IllegalStateException("unsupported OS");
        }

        assertNotDispatchThread();

        try {
            isInstalling = true;

            File tempFile = FileUtil.createTempFile(
                    new File(PathManager.getTempPath()),
                    FileUtilRt.getNameWithoutExtension(setupFilename),
                    "." + FileUtilRt.getExtension(setupFilename));

            LOG.debug("Downloading Kite installer to temp file " + tempFile.getAbsolutePath());
            HttpRequests.request(setupURL)
                    .productNameAsUserAgent()
                    .saveToFile(tempFile, DumbProgressIndicator.INSTANCE);
            return tempFile;
        } finally {
            isInstalling = false;
        }
    }

    @Override
    public boolean runSetup(@NotNull File setupFile) throws ExecutionException, InterruptedException, IOException {
        assertNotDispatchThread();

        try {
            isInstalling = true;

            if (SystemInfo.isMac) {
                return runSetupMacOS(setupFile);
            }
            if (SystemInfo.isWindows) {
                return runSetupWindows(setupFile);
            }
            if (SystemInfo.isLinux) {
                return runSetupLinux(setupFile);
            }
            throw new IllegalStateException("unsupported OS");
        } finally {
            isInstalling = false;
        }
    }

    private static void assertNotDispatchThread() {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            throw new IllegalStateException("Must not be called from EDT");
        }
    }

    private boolean runSetupMacOS(@NotNull File setupFile) throws ExecutionException {
        try {
            runCommand("hdiutil", "attach", "-nobrowse", setupFile.getAbsolutePath());
            runCommand("cp", "-r", "/Volumes/Kite/Kite.app", "/Applications/");
            runCommand("hdiutil", "detach", "/Volumes/Kite/");
            runCommand("open", "-a", "/Applications/Kite.app", "--args", "--plugin-launch-with-copilot", "--channel=jetbrains");
            return true;
        } catch (ExecutionException e) {
            LOG.warn("Execution of Kite setup failed", e);
            return false;
        }
    }

    /**
     * Helper method to simplify execution of a command, which is expected to terminate with exit code 0.
     *
     * @param command Command and arguments
     * @throws ExecutionException Thrown if execution failed or if it terminated with exit code != 0
     */
    private void runCommand(String... command) throws ExecutionException {
        ProcessOutput output = ExecUtil.execAndGetOutput(new GeneralCommandLine(command));
        if (output.getExitCode() != 0) {
            throw new ExecutionException(String.format("Command returned with exit code %d: %s",
                    output.getExitCode(), StringUtil.join(command)));
        }
    }

    private boolean runSetupWindows(@NotNull File setupFile) throws ExecutionException, InterruptedException {
        // using "cmd /c ..." to allow the prompt about elevation
        // without it this will break with exception "CreateProcess error=740, The requested operation requires elevation"
        GeneralCommandLine cmd = new GeneralCommandLine("cmd", "/c",
                setupFile.getAbsolutePath(), "--plugin-launch-with-copilot", "--channel=jetbrains");
        cmd.setWorkDirectory(setupFile.getParentFile());

        int exitCode = cmd.createProcess().waitFor();
        LOG.debug("Kite installer exit code: " + exitCode);
        return exitCode == 0;
    }

    private boolean runSetupLinux(@NotNull File setupFile) throws IOException, ExecutionException, InterruptedException {
        FileUtil.setExecutable(setupFile);

        File tempDir = new File(setupFile.getParentFile(), "kite-setup");
        if (!tempDir.exists() && !tempDir.mkdir()) {
            LOG.warn("Failed to create temporary directory: " + tempDir.getAbsolutePath());
            return false;
        }

        try {
            if (!downloadLinuxInstallerData(setupFile, tempDir)) {
                return false;
            }

            // install, redirect progress output to /dev/null
            LOG.debug("Executing Kite Linux installer");
            KiteGeneralCommandLine installCmd = new KiteGeneralCommandLine(setupFile.getAbsolutePath(), "--install");
            installCmd.setWorkDirectory(setupFile.getParentFile());
            installCmd.setRedirectErrorStream(true);
            installCmd.setStdoutRedirect(ProcessBuilder.Redirect.to(new File("/dev/null")));

            int exitCode = installCmd.createProcess().waitFor();
            LOG.debug("Kite installer exit code: " + exitCode);
            return exitCode == 0;
        } finally {
            FileUtil.delete(tempDir);
        }
    }

    private boolean downloadLinuxInstallerData(@NotNull File setupFile, @NotNull File tempDir) throws ExecutionException, InterruptedException {
        LOG.debug(String.format("Downloading Kite Linux installer using %s, temp dir: %s",
                setupFile.getAbsolutePath(), tempDir.getAbsolutePath()));

        KiteGeneralCommandLine downloadCmd = new KiteGeneralCommandLine(setupFile.getAbsolutePath(), "--download");
        downloadCmd.setWorkDirectory(tempDir);
        downloadCmd.setRedirectErrorStream(true);
        downloadCmd.setStdoutRedirect(ProcessBuilder.Redirect.to(new File("/dev/null")));

        Process process = downloadCmd.createProcess();
        int exitCode = process.waitFor();
        LOG.debug("Downloading Linux installer exit code: " + exitCode);
        return exitCode == 0;
    }
}
