package com.kite.intellij.startup;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.impl.NotificationFullContent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.kite.intellij.Icons;
import com.kite.intellij.backend.WebappLinks;
import com.kite.intellij.ui.notifications.KiteNotifications;
import com.kite.intellij.util.KiteBrowserUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KiteDownloadUnpausedNotification {
    /**
     * @param project           Current project
     * @param onInstallCallback Called when the "Install" button was clicked by the user. The callback is called in a background thread.
     */
    public static void showNotification(@Nullable Project project, @NotNull Runnable onInstallCallback) {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            new UnpausedNotification(onInstallCallback).notify(project);
        } else {
            ApplicationManager.getApplication().invokeLater(() -> {
                new UnpausedNotification(onInstallCallback).notify(project);
            });
        }
    }

    // we need our own class because it has to be a NotificationFullContent
    private static class UnpausedNotification extends Notification implements NotificationFullContent {
        private UnpausedNotification(@NotNull Runnable onInstallCallback) {
            super(KiteNotifications.KITE_GROUP.getDisplayId(),
                    "Kite",
                    "The Kite Engine application is installable again. " +
                            "Kite requires the Kite Engine desktop application to provide completions and documentation. " +
                            "Please install it to use Kite.",
                    NotificationType.INFORMATION);
            this.setIcon(Icons.KiteSmall);

            addAction(new NotificationAction("Install") {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    notification.expire();

                    ApplicationManager.getApplication().executeOnPooledThread(onInstallCallback);
                }
            });

            addAction(new NotificationAction("Learn More") {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    notification.expire();
                    KiteBrowserUtil.browse(WebappLinks.getInstance().copilotURL());
                }
            });
        }
    }
}
