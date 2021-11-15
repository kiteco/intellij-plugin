package com.kite.intellij.startup;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.impl.NotificationFullContent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.kite.intellij.backend.WebappLinks;
import com.kite.intellij.ui.notifications.KiteNotifications;
import com.kite.intellij.util.KiteBrowserUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KiteInstallationFailedNotification {
    public static void showNotification(@Nullable Project project) {
        ApplicationManager.getApplication().assertIsDispatchThread();

        new FailedNotification().notify(project);
    }

    private static class FailedNotification extends Notification implements NotificationFullContent {
        private FailedNotification() {
            super(KiteNotifications.KITE_GROUP.getDisplayId(),
                    "Kite",
                    "There was an error installing the Kite Engine, which is required for Kite to provide completions and documentation. " +
                            "Please install it to use Kite.",
                    NotificationType.ERROR);

            addAction(new NotificationAction("Install") {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    notification.expire();

                    KiteBrowserUtil.browse(WebappLinks.getInstance().downloadPage(true));
                }
            });
        }
    }
}
