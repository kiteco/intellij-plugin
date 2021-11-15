package com.kite.intellij.platform.exec;

import javax.annotation.Nonnull;
import java.util.OptionalInt;

/**
 * Detects a kite process running in the current system.
 *
  */
public interface KiteProcessWatcher {
    /**
     * @return {@code true} if a kited process was detected on the current system.
     */
    default boolean isRunning() {
        return detectKiteProcessId().isPresent();
    }

    /**
     * @return The PID of the currently running kited process, if available.
     */
    @Nonnull
    OptionalInt detectKiteProcessId();

    /**
     * @return {@code true} if kited has been launched before on the current machine, {@code false} if unknown or if kite was never running before.
     */
    boolean wasLaunched();
}
