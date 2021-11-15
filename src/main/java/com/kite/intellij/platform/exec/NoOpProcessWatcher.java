package com.kite.intellij.platform.exec;

import javax.annotation.Nonnull;
import java.util.OptionalInt;

public class NoOpProcessWatcher implements KiteProcessWatcher {
    @Nonnull
    @Override
    public OptionalInt detectKiteProcessId() {
        return OptionalInt.empty();
    }

    @Override
    public boolean wasLaunched() {
        return false;
    }
}
