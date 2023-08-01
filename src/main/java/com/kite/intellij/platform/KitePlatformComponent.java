package com.kite.intellij.platform;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.kite.intellij.Icons;
import com.kite.intellij.ui.notifications.KiteNotifications;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * Project component which displays a warning if the current OS or OS version is not supported by Kite.
 */
public class KitePlatformComponent implements ProjectActivity {
    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        if (KitePlatform.isOsVersionNotSupported()) {
            String title = "<b>Kite</b>";
            String subtitle = "<b>Unsupported platform</b>";
            String content = "Kite is currently not yet available for your system. Please check <a href=\"https://kite.com/\">kite.com</a> for the currently supported platforms.";

            Notification notification = new Notification(KiteNotifications.KITE_GROUP.getDisplayId(), title, content, NotificationType.ERROR);
            notification.setSubtitle(subtitle);
            notification.setIcon(Icons.KiteSmall);
            notification.notify(project);
        }
        return null;
    }
}
