package com.kite.intellij.settings;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.util.OptionalInt;

/**
 * Form definition of the Kite settings.
 *
  */
public class KiteSettingsForm {
    JPanel mainPanel;
    JBCheckBox paramInfoDelayCheckbox;
    JBCheckBox paramInfoFontSizeCheckbox;
    JSpinner paramInfoDelayMillis;
    JSpinner paramInfoFontSize;
    JBLabel paramInfoDelayMillisLabel;
    JBLabel paramInfoFontSizeLabel;
    private JBCheckBox kiteCodeCompletions;
    private JCheckBox showKiteIntro;

    private JBCheckBox showCommonMethods;
    private JBCheckBox showKwargs;
    private JBCheckBox startKited;
    private JBCheckBox multiLineCompletionsCheckbox;

    public boolean isKiteIntroductionEnabled() {
        return showKiteIntro.isSelected();
    }

    public void setKiteIntroductionEnabled(boolean enabled) {
        showKiteIntro.setSelected(enabled);
    }

    public boolean isCodeCompletionEnabled() {
        return kiteCodeCompletions.isSelected();
    }

    public void setCodeCompletionEnabled(boolean enabled) {
        kiteCodeCompletions.setSelected(enabled);
    }

    @Nonnull
    public OptionalInt getParamInfoFontSize() {
        return paramInfoFontSizeCheckbox.isSelected() ? OptionalInt.of((Integer) paramInfoFontSize.getValue()) : OptionalInt.empty();
    }

    @Nonnull
    public OptionalInt getParamInfoDelayMillis() {
        return paramInfoDelayCheckbox.isSelected() ? OptionalInt.of((Integer) paramInfoDelayMillis.getValue()) : OptionalInt.empty();
    }

    public void setParamInfoFontSize(boolean enabled, int fontSizePoints) {
        paramInfoFontSizeCheckbox.setSelected(enabled);
        paramInfoFontSize.setEnabled(enabled);
        paramInfoFontSize.setValue(fontSizePoints);
        paramInfoFontSizeLabel.setEnabled(enabled);
    }

    public void setParamInfoDelay(boolean enabled, int delayMillis) {
        paramInfoDelayCheckbox.setSelected(enabled);
        paramInfoDelayMillis.setEnabled(enabled);
        paramInfoDelayMillis.setValue(delayMillis);
        paramInfoDelayMillisLabel.setEnabled(enabled);
    }

    public boolean isShowPopularPatterns() {
        return showCommonMethods.isSelected();
    }

    public void setShowPopularPatterns(boolean enabled) {
        showCommonMethods.setSelected(enabled);
    }

    public boolean isShowKwargs() {
        return showKwargs.isSelected();
    }

    public void setShowKwargs(boolean enabled) {
        showKwargs.setSelected(enabled);
    }

    public boolean isStartKiteAtStartup() {
        return startKited.isSelected();
    }

    public void setStartKitedAtStartup(boolean enabled) {
        startKited.setSelected(enabled);
    }

    public boolean isUseNewCompletions(){
        return multiLineCompletionsCheckbox.isSelected();
    }

    public void setUseNewCompletions(boolean enabled){
        multiLineCompletionsCheckbox.setSelected(enabled);
    }

    void updateUI() {
        registerCheckboxDependents(paramInfoDelayCheckbox, paramInfoDelayMillis, paramInfoDelayMillisLabel);
        paramInfoDelayMillis.setModel(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

        registerCheckboxDependents(paramInfoFontSizeCheckbox, paramInfoFontSize, paramInfoFontSizeLabel);
        paramInfoFontSize.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));

        kiteCodeCompletions.setVisible(false);
    }

    private void registerCheckboxDependents(JBCheckBox checkbox, Component... dependendents) {
        checkbox.addChangeListener((ChangeEvent e) -> {
            for (Component dependendent : dependendents) {
                dependendent.setEnabled(checkbox.isSelected());
            }
        });
    }

    private void createUIComponents() {
    }
}
