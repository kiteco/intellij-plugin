package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;

public class ForwardingTypeDetails implements TypeDetails {
    @Nonnull
    protected final TypeDetails base;

    public ForwardingTypeDetails(@Nonnull TypeDetails base) {
        this.base = base;
    }

    @Override
    public String toString() {
        return "ForwardingTypeDetails{" +
                "base=" + base +
                '}';
    }

    @Override
    public DetailType getType() {
        return base.getType();
    }

    @Override
    @Nonnull
    public Symbol[] getMembers() {
        return base.getMembers();
    }

    @Override
    public boolean hasMembers() {
        return base.hasMembers();
    }

    @Override
    public int getTotalMembers() {
        return base.getTotalMembers();
    }
}
