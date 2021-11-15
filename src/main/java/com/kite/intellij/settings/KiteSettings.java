package com.kite.intellij.settings;

/**
 * Defines the model of Kite settings. The assigned default values are
 * treated as default state of the component.
 *
  */
public class KiteSettings {
    public volatile boolean paramInfoDelayEnabled = true;
    public volatile int paramInfoDelayMillis = 100;

    public volatile boolean paramInfoFontSizeEnabled = false;
    public volatile int paramInfoFontSize = 14;

    public volatile boolean startKiteAtStartup = true;

    public volatile boolean codeCompletionEnabled = true;

    public volatile boolean goEnabled = true;

    public volatile boolean showWelcomeNotification = true;

    // this controls the notification which appears when the first
    // .go file is opened in the current application
    public volatile boolean showGoWelcomeNotification = true;

    // this controls the notification which appears when the first
    // .js,.jsx,.vue file is opened in the current application
    public volatile boolean showJavaScriptWelcomeNotification = true;

    public volatile boolean showPopularPatterns = false;

    public volatile boolean showKwargs = false;

    public volatile boolean useNewCompletions = true;

    public volatile Boolean autocorrectRunValidationEnabled = null;

    public volatile boolean kiteUnavailableShown = false;

    public KiteSettings() {
    }

    @Override
    public int hashCode() {
        int result = (paramInfoDelayEnabled ? 1 : 0);
        result = 31 * result + paramInfoDelayMillis;
        result = 31 * result + (paramInfoFontSizeEnabled ? 1 : 0);
        result = 31 * result + paramInfoFontSize;
        result = 31 * result + (codeCompletionEnabled ? 1 : 0);
        result = 31 * result + (goEnabled ? 1 : 0);
        result = 31 * result + (showWelcomeNotification ? 1 : 0);
        result = 31 * result + (showGoWelcomeNotification ? 1 : 0);
        result = 31 * result + (showJavaScriptWelcomeNotification ? 1 : 0);
        result = 31 * result + (autocorrectRunValidationEnabled ? 1 : 0);
        result = 31 * result + (showPopularPatterns ? 1 : 0);
        result = 31 * result + (showKwargs ? 1 : 0);
        result = 31 * result + (useNewCompletions ? 1 : 0);
        result = 31 * result + (startKiteAtStartup ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        KiteSettings that = (KiteSettings) o;

        if (paramInfoDelayEnabled != that.paramInfoDelayEnabled) {
            return false;
        }
        if (paramInfoDelayMillis != that.paramInfoDelayMillis) {
            return false;
        }
        if (paramInfoFontSizeEnabled != that.paramInfoFontSizeEnabled) {
            return false;
        }
        if (paramInfoFontSize != that.paramInfoFontSize) {
            return false;
        }
        if (codeCompletionEnabled != that.codeCompletionEnabled) {
            return false;
        }
        if (goEnabled != that.goEnabled) {
            return false;
        }
        if (showWelcomeNotification != that.showWelcomeNotification) {
            return false;
        }
        if (showGoWelcomeNotification != that.showGoWelcomeNotification) {
            return false;
        }
        if (showJavaScriptWelcomeNotification != that.showJavaScriptWelcomeNotification) {
            return false;
        }
        if (autocorrectRunValidationEnabled != that.autocorrectRunValidationEnabled) {
            return false;
        }
        if (showPopularPatterns != that.showPopularPatterns) {
            return false;
        }
        if (showKwargs != that.showKwargs) {
            return false;
        }
        if (useNewCompletions != that.useNewCompletions) {
            return false;
        }
        return startKiteAtStartup == that.startKiteAtStartup;
    }

    @Override
    public String toString() {
        return "KiteSettings{" +
                "paramInfoDelayEnabled=" + paramInfoDelayEnabled +
                ", paramInfoDelayMillis=" + paramInfoDelayMillis +
                ", paramInfoFontSizeEnabled=" + paramInfoFontSizeEnabled +
                ", paramInfoFontSize=" + paramInfoFontSize +
                ", codeCompletionEnabled=" + codeCompletionEnabled +
                ", goEnabled=" + goEnabled +
                ", showWelcomeNotification=" + showWelcomeNotification +
                ", showGoWelcomeNotification=" + showGoWelcomeNotification +
                ", autocorrectRunValidationEnabled" + autocorrectRunValidationEnabled +
                ", showPopularPatterns" + showPopularPatterns +
                ", showKwargs" + showKwargs +
                ", useNewCompletions" + useNewCompletions +
                ", startKitedAtStartup" + startKiteAtStartup +
                '}';
    }
}
