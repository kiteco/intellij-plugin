package com.kite.intellij.ui.notifications;

import com.intellij.notification.*;
import com.intellij.notification.impl.NotificationFullContent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.kite.intellij.Icons;
import com.kite.intellij.backend.model.KiteServiceNotification;
import com.kite.intellij.backend.model.NotificationButton;
import com.kite.intellij.util.KiteBrowserUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class KiteNotifications {
    public static final NotificationGroup KITE_GROUP = new NotificationGroup("Kite", NotificationDisplayType.STICKY_BALLOON, true);

    public static void showServiceNotification(@Nullable Project project, @Nonnull KiteServiceNotification kiteNotification) {
        // don't show more than one notification at a time
        NotificationsManager manager = NotificationsManager.getNotificationsManager();
        KiteServiceUINotification[] current = manager.getNotificationsOfType(KiteServiceUINotification.class, project);
        if (current.length > 0) {
            return;
        }

        Notification notification = new KiteServiceUINotification(NotificationType.WARNING);
        notification.setTitle(kiteNotification.title);
        notification.setContent(kiteNotification.body);
        if (kiteNotification.buttons != null) {
            for (NotificationButton button : kiteNotification.buttons) {
                notification.addAction(createAction(button));
            }
        }

        notification.notify(project);
    }

    private static AnAction createAction(NotificationButton button) {
        if (button.isDismissAction()) {
            return new NotificationAction(button.text) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    notification.expire();
                }
            };
        }

        if (button.isOpenAction()) {
            return new NotificationAction(button.text) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    notification.expire();

                    if (button.link != null) {
                        KiteBrowserUtil.browse(button.link);
                    }
                }
            };
        }

        throw new IllegalStateException("Unsupported button type: " + button.action);
    }

    private static class KiteServiceUINotification extends Notification implements NotificationFullContent, KiteNotification {
        public KiteServiceUINotification(@NotNull NotificationType type) {
            super(KiteNotifications.KITE_GROUP.getDisplayId(), Icons.KiteSmall, type);
        }
    }
}
