package com.kite.intellij.settings;

import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Test;

public class KiteConfigurableTest extends KiteLightFixtureTest {
    @Test
    public void testReset() {
        KiteConfigurable configurable = new KiteConfigurableFactory().createConfigurable();
        configurable.createComponent();

        Assert.assertTrue("In default state this must be enabled", configurable.getSettingsForm().getParamInfoDelayMillis().isPresent());
        Assert.assertFalse("In default state this must be disabled", configurable.getSettingsForm().getParamInfoFontSize().isPresent());

        KiteSettings state = KiteSettingsService.getInstance().getState();

        state.paramInfoDelayEnabled = true;
        state.paramInfoDelayMillis = 400;
        configurable.reset();
        Assert.assertEquals("The UI must reflect the state change", 400, configurable.getSettingsForm().getParamInfoDelayMillis().getAsInt());

        state.paramInfoFontSizeEnabled = true;
        state.paramInfoFontSize = 15;
        configurable.reset();
        Assert.assertEquals("The UI must reflect the state change", 15, configurable.getSettingsForm().getParamInfoFontSize().getAsInt());
    }

    @Test
    public void testApply() {
        //create the UI with the default state
        KiteConfigurable configurable = new KiteConfigurableFactory().createConfigurable();
        configurable.createComponent();

        KiteSettings state = KiteSettingsService.getInstance().getState();
        Assert.assertEquals("The default UI state must represent the default state", state, configurable.applyTo(new KiteSettings()));

        KiteSettingsForm form = configurable.getSettingsForm();

        state.paramInfoDelayEnabled = true;
        state.paramInfoDelayMillis = 300;
        form.setParamInfoDelay(true, 300);
        Assert.assertEquals("The UI state must represent the modified state", state, configurable.applyTo(new KiteSettings()));

        state.paramInfoFontSizeEnabled = true;
        state.paramInfoFontSize = 16;
        form.setParamInfoFontSize(true, 16);
        Assert.assertEquals("The UI state must represent the modified state", state, configurable.applyTo(new KiteSettings()));

    }

    @Test
    public void testParamInfoFontSizeUI() {
        KiteConfigurable configurable = new KiteConfigurable(new KiteSettings());
        configurable.createComponent();

        KiteSettings kiteSettings = new KiteSettings();

        KiteSettingsForm form = configurable.getSettingsForm();
        Assert.assertFalse("Default state must be unselected", form.paramInfoFontSizeCheckbox.isSelected());
        Assert.assertFalse("If checkbox is not selected the value has to be disabled", form.paramInfoFontSize.isEnabled());
        Assert.assertFalse("If checkbox is not selected the value has to be disabled", form.paramInfoFontSizeLabel.isEnabled());

        kiteSettings.paramInfoFontSizeEnabled = true;
        configurable.resetTo(kiteSettings);
        Assert.assertTrue("A state change must set the selection state", form.paramInfoFontSizeCheckbox.isSelected());
        Assert.assertTrue("If checkbox is selected then component must be enabled", form.paramInfoFontSize.isEnabled());
        Assert.assertTrue("If checkbox is selected then the component must be enabled", form.paramInfoFontSizeLabel.isEnabled());
    }

    @Test
    public void testParamInfoDelayUI() {
        KiteConfigurable configurable = new KiteConfigurable(new KiteSettings());
        configurable.createComponent();

        KiteSettings kiteSettings = new KiteSettings();

        KiteSettingsForm form = configurable.getSettingsForm();
        Assert.assertTrue("Default state must be selected", form.paramInfoDelayCheckbox.isSelected());
        Assert.assertTrue("If checkbox is selected the value has to be enabled", form.paramInfoDelayMillis.isEnabled());
        Assert.assertTrue("If checkbox is  selected the value has to be enabled", form.paramInfoDelayMillisLabel.isEnabled());

        kiteSettings.paramInfoDelayEnabled = false;
        configurable.resetTo(kiteSettings);
        Assert.assertFalse("A state change must set the selection state", form.paramInfoDelayCheckbox.isSelected());
        Assert.assertFalse("If checkbox is not selected then the component must be disabled", form.paramInfoDelayMillis.isEnabled());
        Assert.assertFalse("If checkbox is not selected then the component must be disabled", form.paramInfoDelayMillisLabel.isEnabled());
    }
}