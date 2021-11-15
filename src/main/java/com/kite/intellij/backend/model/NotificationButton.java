package com.kite.intellij.backend.model;

import org.jetbrains.annotations.Nullable;

// kited's Button in kite-golib/presentation/notification.go
public class NotificationButton {
    public String text;
    public String action;
    @Nullable
    public String link;

    public boolean isOpenAction() {
        return "open".equals(action);
    }

    public boolean isDismissAction() {
        return "dismiss".equals(action);
    }
}
