package com.kite.intellij.lang;

import org.jetbrains.annotations.NotNull;

/**
 * This extension point must not depend on classes provided by the Python plugin.
 * It's enabled in 'kite-python.xml' and only loaded when Python support is available at runtime.
 */
public class KitePythonSupport implements LangSupportEP {
    @Override
    public boolean supportsFileExtension(@NotNull String ext) {
        return KiteLanguage.Python.getExtensions().contains(ext);
    }

    @Override
    public boolean isSupportedKiteOnboardingLanguage(@NotNull KiteLanguage language) {
        return KiteLanguage.Python == language;
    }

    @Override
    public boolean supportsFeature(@NotNull KiteLanguageSupport.Feature feature) {
        // python supports all features
        return true;
    }
}
