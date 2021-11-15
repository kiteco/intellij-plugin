package com.kite.intellij.lang;

import com.intellij.openapi.extensions.ExtensionPointName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public interface LangSupportEP {
    ExtensionPointName<LangSupportEP> EP = ExtensionPointName.create("com.kite.intellij.kiteLangSupport");

    boolean supportsFileExtension(@NotNull String ext);

    default boolean isSupportedKiteOnboardingLanguage(@NotNull KiteLanguage language) {
        return false;
    }

    /**
     * Make sure that {@code supportsFileExtension} of this extension returns true, before calling this method.
     * Filename patching is needed for .pynb files, for example.
     *
     * @param filename The original filename
     * @return A patched filename, if the extension is supported.
     */
    @Nullable
    default String patchFilename(@NotNull String filename) {
        return null;
    }

    boolean supportsFeature(@Nonnull KiteLanguageSupport.Feature feature);
}
