package com.kite.intellij.codeFinder;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.impl.NotificationFullContent;
import com.kite.intellij.Icons;
import com.kite.intellij.ui.notifications.KiteNotification;
import com.kite.intellij.ui.notifications.KiteNotifications;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * Notification used to display codenav errors.
 *
 * @author tonycheang
 */
class KiteCodeFinderNotification extends Notification implements KiteNotification, NotificationFullContent {
    public KiteCodeFinderNotification(@Nullable String title, @Nullable String content, @Nonnull NotificationType type) {
        super(KiteNotifications.KITE_GROUP.getDisplayId(), Icons.KiteSmall, title, null, content, type, null);
    }
}
