package com.kite.intellij.platform;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Service which downloads and installs the Kite setup.
 */
@SuppressWarnings("MissingRecentApi")
public interface KiteInstallService {
    static KiteInstallService getInstance() {
        return ApplicationManager.getApplication().getService(KiteInstallService.class);
    }

    /**
     * @return Returns if download or installation are currently in progress.
     */
    boolean isInstalling();

    /**
     * @return Returns if the Kite installer is available for download.
     */
    boolean canInstall();

    /**
     * Download the Kite setup to a temp file.
     * This must be called from a background thread to avoid blocking the UI.
     * The caller is responsible to cleanup the downloaded file.
     *
     * @return File, where the setup file is stored.
     * @throws IOException Thrown if the download failed.
     */
    @NotNull
    File downloadSetup() throws IOException;

    /**
     * Executes the setup file.
     * This must be called from a background thread to avoid blocking the UI.
     * This methods returns when the setup process terminated.
     *
     * @param setupFile Location where the Kite installer was downloaded.
     * @return {@code true} if the setup was successfully executed.
     */
    boolean runSetup(@NotNull File setupFile) throws ExecutionException, InterruptedException, IOException;
}
