package com.kite.intellij.startup;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.impl.NotificationFullContent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.kite.intellij.Icons;
import com.kite.intellij.ui.notifications.KiteNotifications;
import com.kite.intellij.util.KiteBrowserUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KiteDownloadPausedNotification {
    /**
     * @param project Current project
     */
    public static void showNotification(@Nullable Project project) {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            new PausedNotification().notify(project);
        } else {
            ApplicationManager.getApplication().invokeLater(() -> {
                new PausedNotification().notify(project);
            });
        }
    }

    // we need our own class because it has to be a NotificationFullContent
    private static class PausedNotification extends Notification implements NotificationFullContent {
        private PausedNotification() {
            super(KiteNotifications.KITE_GROUP.getDisplayId(),
                    Icons.KiteSmall,
                    "Temporarily unable to install", "",
                    "Kite requires the Kite Copilot to function. However, it cannot be downloaded for the next few weeks. This plugin will notify you when it's available again.",
                    NotificationType.INFORMATION, null);

            addAction(new NotificationAction("Close") {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    notification.expire();
                }
            });
            addAction(new NotificationAction("Learn More") {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    notification.expire();
                    KiteBrowserUtil.browse("https://kite.com/kite-is-temporarily-unavailable/?source=intellij");
                }
            });
        }
    }
}
