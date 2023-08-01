package com.kite.intellij.startup;

import com.intellij.ide.ApplicationInitializedListener;
import com.kite.intellij.ui.notifications.KiteNotifications;
import kotlinx.coroutines.CoroutineScope;

import javax.annotation.Nonnull;

/**
 * This preloading activity takes care of the Kite notification group.
 *
  */
public class NotificationRegistration implements ApplicationInitializedListener {
    public NotificationRegistration() {
    }


    public void execute(CoroutineScope asyncScope) {
        // access the Kite notification group to force it to initialize
        KiteNotifications.KITE_GROUP.getDisplayId();
    }
}
