package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;

/**
 * Used for all Kite model elements which provide an ID.
 *
  * @see Id
 */
public interface WithId {
    @Nonnull
    Id getId();
}
