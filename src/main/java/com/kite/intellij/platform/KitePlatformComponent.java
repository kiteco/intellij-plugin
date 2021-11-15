package com.kite.intellij.platform;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.kite.intellij.Icons;
import com.kite.intellij.ui.notifications.KiteNotifications;

import javax.annotation.Nonnull;

/**
 * Project component which displays a warning if the current OS or OS version is not supported by Kite.
 */
public class KitePlatformComponent implements com.intellij.openapi.components.ProjectComponent {
    private final Project project;

    public KitePlatformComponent(Project project) {
        this.project = project;
    }

    @Nonnull
    public static KitePlatformComponent getInstance(Project project) {
        return project.getComponent(KitePlatformComponent.class);
    }

    @Nonnull
    @Override
    public String getComponentName() {
        return "kite.PlatformComponent";
    }

    @Override
    public void projectOpened() {
        if (KitePlatform.isOsVersionNotSupported()) {
            String title = "<b>Kite</b>";
            String subtitle = "<b>Unsupported platform</b>";
            String content = "Kite is currently not yet available for your system. Please check <a href=\"https://kite.com/\">kite.com</a> for the currently supported platforms.";

            Notification notification = new Notification(KiteNotifications.KITE_GROUP.getDisplayId(), Icons.KiteSmall, title, subtitle, content, NotificationType.ERROR, new NotificationListener.UrlOpeningListener(false));
            notification.notify(project);
        }
    }

    @Override
    public void projectClosed() {

    }
}
