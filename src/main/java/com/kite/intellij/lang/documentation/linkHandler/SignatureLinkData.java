package com.kite.intellij.lang.documentation.linkHandler;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * Defines the context information passed in a link to a signature info.
 *
  */
@Immutable
@ThreadSafe
public class SignatureLinkData implements KiteLinkData {
    @Nonnull
    private final OptionalInt argIndex;
    private final boolean inKwargs;
    private final boolean expandKwargs;
    private final boolean expandPopularPatterns;
    private final boolean automaticPopupMode;

    public SignatureLinkData(@Nonnull OptionalInt argIndex, boolean inKwargs, boolean expandKwargs, boolean expandPopularPatterns, boolean automaticPopupMode) {
        this.argIndex = argIndex;

        this.inKwargs = inKwargs;
        this.expandKwargs = expandKwargs;
        this.expandPopularPatterns = expandPopularPatterns;
        this.automaticPopupMode = automaticPopupMode;
    }

    public boolean isAutomaticPopupMode() {
        return automaticPopupMode;
    }

    public boolean isExpandPopularPatterns() {
        return expandPopularPatterns;
    }

    @Nonnull
    public OptionalInt getArgIndex() {
        return argIndex;
    }

    public boolean isInKwargs() {
        return inKwargs;
    }

    public boolean isExpandKwargs() {
        return expandKwargs;
    }

    public SignatureLinkData withExpandKwargs(@Nonnull Boolean expandKwargs) {
        return new SignatureLinkData(argIndex, inKwargs, expandKwargs, expandPopularPatterns, automaticPopupMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(argIndex, inKwargs, expandKwargs, expandPopularPatterns);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SignatureLinkData linkData = (SignatureLinkData) o;
        return inKwargs == linkData.inKwargs &&
                expandKwargs == linkData.expandKwargs &&
                expandPopularPatterns == linkData.expandPopularPatterns &&
                automaticPopupMode == linkData.automaticPopupMode &&
                Objects.equals(argIndex, linkData.argIndex);
    }

    @Override
    public String toString() {
        return "SignatureLinkData{" +
                "argIndex=" + argIndex +
                ", inKwargs=" + inKwargs +
                ", expandKwargs=" + expandKwargs +
                ", expandPopularPatterns=" + expandPopularPatterns +
                ", automaticPopup=" + automaticPopupMode +
                '}';
    }
}
