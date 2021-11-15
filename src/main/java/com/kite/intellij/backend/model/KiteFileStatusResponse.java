package com.kite.intellij.backend.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class KiteFileStatusResponse {
    @NotNull
    private final KiteFileStatus status;
    @Nullable
    private final String shortStatus;
    @Nullable
    private final String longStatus;
    @Nullable
    private final NotificationButton button;

    public KiteFileStatusResponse(@NotNull KiteFileStatus status, @Nullable String shortStatus, @Nullable String longStatus, @Nullable NotificationButton button) {
        this.status = status;
        this.shortStatus = shortStatus;
        this.longStatus = longStatus;
        this.button = button;
    }

    public KiteFileStatusResponse(KiteFileStatus status) {
        this(status, null, null, null);
    }

    public @NotNull KiteFileStatus getStatus() {
        return status;
    }

    public @Nullable String getShortStatus() {
        return shortStatus;
    }

    public @Nullable String getLongStatus() {
        return longStatus;
    }

    public @Nullable NotificationButton getButton() {
        return button;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, shortStatus, longStatus, button);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KiteFileStatusResponse that = (KiteFileStatusResponse) o;
        return status == that.status && Objects.equals(shortStatus, that.shortStatus) && Objects.equals(longStatus, that.longStatus) && Objects.equals(button, that.button);
    }
}
