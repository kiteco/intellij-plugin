package com.kite.intellij.startup;

import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.progress.ProgressIndicator;
import com.kite.intellij.ui.notifications.KiteNotifications;

import javax.annotation.Nonnull;

/**
 * This preloading activity takes care of the Kite notification group.
 *
  */
public class NotificationRegistration extends PreloadingActivity {
    public NotificationRegistration() {
    }

    @Override
    public void preload(@Nonnull ProgressIndicator indicator) {
        // access the Kite notification group to force it to initialize
        KiteNotifications.KITE_GROUP.getDisplayId();
    }
}
