package com.kite.intellij.backend;

import javax.annotation.Nullable;

/**
 * Listener to react on connection status changes to the Kite backend.
 *
  */
public interface ConnectionStatusListener {
    void connectionStatusChanged(boolean connectionAvailable, @Nullable Exception error);
}
