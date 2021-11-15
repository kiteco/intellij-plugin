package com.kite.intellij.status;

import com.kite.intellij.Icons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Defines the status of the Kite icon in the statusbar.
 *
  */
enum IconStatus {
    Ok, InitializingOrIndexing, Unknown, Hidden, Error;

    @Nullable
    public Icon getIcon() {
        switch (this) {
            case Ok:
                return Icons.KiteSmallDisabled;
            case InitializingOrIndexing:
                return Icons.KiteSmallSync;
            case Error:
                return Icons.KiteSmallError;
            case Unknown:
                return Icons.KiteSmallDisabled;
            case Hidden:
                return null;
            default:
                throw new IllegalStateException("Unable to handle " + this);
        }
    }
}
