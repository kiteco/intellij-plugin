package com.kite.intellij.welcome;

import com.kite.intellij.lang.KiteLanguage;

public class KiteOnboardingLanguageUnsupportedError extends KiteOnboardingError {
    public KiteOnboardingLanguageUnsupportedError(KiteLanguage language) {
        super("Unsupported language " + language.asKiteName());
    }
}
