package com.kite.intellij.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public final class KiteSwingUtilities {
    private KiteSwingUtilities() {
    }

    /**
     * @param child      Starting point
     * @param parentType The expected type
     * @param <T>
     * @return The first parent in the hierachy which has the given type. {@code null} if no suitable parent was found or child is null.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends Component> T findParentOfType(@Nullable Component child, @Nonnull Class<T> parentType) {
        if (child == null) {
            return null;
        }

        Component parent = child.getParent();
        while (parent != null && !(parentType.isInstance(parent))) {
            parent = parent.getParent();
        }

        return parent == null ? null : (T) parent;
    }
}
