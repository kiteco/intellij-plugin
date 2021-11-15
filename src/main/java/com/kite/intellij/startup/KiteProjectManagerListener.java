package com.kite.intellij.startup;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtilRt;
import com.kite.intellij.platform.KiteDetector;
import com.kite.intellij.platform.KiteInstallService;
import com.kite.intellij.settings.KiteSettingsService;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is a project lifecycle listener instead of an ApplicationLifecycleListener, because
 * notifications have to be displayed. The application listener has no project and may be executed before a project
 * window is shown.
 */
public class KiteProjectManagerListener implements ProjectManagerListener {
    private static final Logger LOG = Logger.getInstance("#kite.startup");
    private static volatile boolean firstProjectOpened;

    @Override
    public void projectOpened(@NotNull Project project) {
        if (project.isDefault() || firstProjectOpened) {
            return;
        }

        StartupManager.getInstance(project).runAfterOpened(() -> {
            // only do this once and for just one project
            if (!firstProjectOpened) {
                firstProjectOpened = true;

                // appStarting is executed on the EDT and within a transaction.
                // Execute our logic in the background to not delay the IDE startup
                ApplicationManager.getApplication().executeOnPooledThread(() -> onAppStarting(project));
            }
        });
    }

    private void onAppStarting(@NotNull Project project) {
        if (KiteDetector.getInstance().isRunning()) {
            return;
        }

        List<Path> paths = KiteDetector.getInstance().detectKiteExecutableFiles();
        if (paths.size() == 0) {
            // display notification to ask for confirmation to install,
            // but only if the installer is still available
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                // only notify once that Kite is unavailable
                boolean unavailableShown = KiteSettingsService.getInstance().getState().kiteUnavailableShown;

                if (KiteInstallService.getInstance().canInstall()) {
                    if (unavailableShown) {
                        KiteSettingsService.getInstance().getState().kiteUnavailableShown = false;
                        KiteDownloadUnpausedNotification.showNotification(project, () -> {
                            boolean ok = downloadAndInstallKite();
                            if (!ok) {
                                ApplicationManager.getApplication().invokeLater(() -> {
                                    KiteInstallationFailedNotification.showNotification(project);
                                });
                            }
                        });
                    } else {
                        KiteAutomaticInstallNotification.showNotification(project, () -> {
                            boolean ok = downloadAndInstallKite();
                            if (!ok) {
                                ApplicationManager.getApplication().invokeLater(() -> {
                                    KiteInstallationFailedNotification.showNotification(project);
                                });
                            }
                        });
                    }
                } else if (!unavailableShown) {
                    KiteSettingsService.getInstance().getState().kiteUnavailableShown = true;
                    KiteDownloadPausedNotification.showNotification(project);
                }
            });
        } else if (paths.size() == 1 && KiteSettingsService.getInstance().getState().startKiteAtStartup) {
            if (disallowKitedAutostart()) {
                LOG.debug("Skipping kite autostart, because the current IDE build isn't supporting it.");
                return;
            }

            try {
                KiteDetector.getInstance().launch(paths.get(0), false);

                Thread.sleep(TimeUnit.SECONDS.toMillis(5), 0);
                ApplicationManager.getApplication().getMessageBus().syncPublisher(KiteAutostartListener.TOPIC).onKiteAutostart();
            } catch (Exception e) {
                LOG.debug("Error during automatic startup of kited", e);
            }
        }
    }

    private boolean downloadAndInstallKite() {
        KiteInstallService installService = KiteInstallService.getInstance();
        File setupFile = null;
        try {
            setupFile = installService.downloadSetup();
            boolean ok = installService.runSetup(setupFile);
            if (!ok) {
                return false;
            }

            List<Path> paths = KiteDetector.getInstance().detectKiteExecutableFiles();
            if (paths.size() == 1) {
                KiteDetector.getInstance().launch(paths.get(0), true);
            }
            return true;
        } catch (IOException | InterruptedException | ExecutionException e) {
            LOG.warn("Error while downloading or installing Kite", e);
            return false;
        } finally {
            if (setupFile != null) {
                FileUtilRt.delete(setupFile);
            }
        }
    }

    /**
     * See https://github.com/kiteco/kiteco/issues/9723. Builds 2019.3 ship with a faulty WinProcessListHelper.exe
     * executable file.
     *
     * @return {@code true} if the current IDE is incompatible with the autostart feature.
     */
    private static boolean disallowKitedAutostart() {
        return SystemInfo.isWin7OrNewer
                && !SystemInfo.isWin8OrNewer
                && ApplicationInfo.getInstance().getBuild().getBaselineVersion() == 193;
    }
}
