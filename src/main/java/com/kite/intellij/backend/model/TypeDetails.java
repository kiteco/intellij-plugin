package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;

public interface TypeDetails extends Detail {
    @Nonnull
    Symbol[] getMembers();

    boolean hasMembers();

    int getTotalMembers();
}
