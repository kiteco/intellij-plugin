package com.kite.intellij.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.swing.*;
import java.util.OptionalInt;

/**
 * IDE settings panel definition for Kite.
 *
  */
public class KiteConfigurable implements Configurable {
    private final KiteSettings initialState;
    private KiteSettingsForm settingsForm;

    public KiteConfigurable(KiteSettings state) {
        initialState = state;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Kite";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsForm = new KiteSettingsForm();
        settingsForm.updateUI();

        resetTo(initialState);

        return settingsForm.mainPanel;
    }

    @Override
    public boolean isModified() {
        KiteSettings newState = new KiteSettings();
        applyTo(newState);

        return !newState.equals(KiteSettingsService.getInstance().getState());
    }

    /**
     * Updates the persistent settings with the current state of the UI.
     *
     * @throws ConfigurationException
     */
    @Override
    public void apply() throws ConfigurationException {
        boolean isModified = isModified();

        KiteSettings updatedSettings = applyTo(KiteSettingsService.getInstance().getState());
        if (isModified) {
            KiteSettingsService.getInstance().triggerSettingsChange(updatedSettings);
        }
    }

    /**
     * Reset the UI state to display the currently stored state of the settings.
     */
    @Override
    public void reset() {
        resetTo(KiteSettingsService.getInstance().getState());
    }

    @Override
    public void disposeUIResources() {
        this.settingsForm = null;
    }

    @TestOnly
    KiteSettingsForm getSettingsForm() {
        return settingsForm;
    }

    protected void resetTo(KiteSettings state) {
        settingsForm.setKiteIntroductionEnabled(state.showWelcomeNotification);

        settingsForm.setParamInfoDelay(state.paramInfoDelayEnabled, state.paramInfoDelayMillis);
        settingsForm.setParamInfoFontSize(state.paramInfoFontSizeEnabled, state.paramInfoFontSize);

        settingsForm.setCodeCompletionEnabled(state.codeCompletionEnabled);

        settingsForm.setShowPopularPatterns(state.showPopularPatterns);

        settingsForm.setShowKwargs(state.showKwargs);

        settingsForm.setStartKitedAtStartup(state.startKiteAtStartup);

        settingsForm.setUseNewCompletions(state.useNewCompletions);
    }

    protected KiteSettings applyTo(KiteSettings settings) {
        settings.showWelcomeNotification = settingsForm.isKiteIntroductionEnabled();

        OptionalInt paramInfoFontSize = settingsForm.getParamInfoFontSize();
        settings.paramInfoFontSizeEnabled = paramInfoFontSize.isPresent();
        if (paramInfoFontSize.isPresent()) {
            settings.paramInfoFontSize = paramInfoFontSize.getAsInt();
        }

        OptionalInt paramInfoDelay = settingsForm.getParamInfoDelayMillis();
        settings.paramInfoDelayEnabled = paramInfoDelay.isPresent();
        if (paramInfoDelay.isPresent()) {
            settings.paramInfoDelayMillis = paramInfoDelay.getAsInt();
        }

        settings.codeCompletionEnabled = settingsForm.isCodeCompletionEnabled();

        settings.showPopularPatterns = settingsForm.isShowPopularPatterns();

        settings.showKwargs = settingsForm.isShowKwargs();

        settings.startKiteAtStartup = settingsForm.isStartKiteAtStartup();

        settings.useNewCompletions = settingsForm.isUseNewCompletions();

        return settings;
    }
}
