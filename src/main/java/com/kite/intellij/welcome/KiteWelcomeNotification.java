package com.kite.intellij.welcome;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.impl.NotificationFullContent;
import com.kite.intellij.Icons;
import com.kite.intellij.ui.notifications.KiteNotification;
import com.kite.intellij.ui.notifications.KiteNotifications;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * Notification used to display at first startup.
 *
  */
class KiteWelcomeNotification extends Notification implements KiteNotification, NotificationFullContent {
    public KiteWelcomeNotification(@Nullable String title, @Nullable String content, @Nonnull NotificationType type) {
        super(KiteNotifications.KITE_GROUP.getDisplayId(), title == null ? "Kite" : title, content == null ? "" : content, type);
        this.setIcon(Icons.KiteSmall);
    }
}
