package com.kite.intellij.settings;

import com.intellij.openapi.options.ConfigurableProvider;

import javax.annotation.Nonnull;

/**
 * Simple factory class to create the Kite IDE configurable implementation.
 * It allows flexibility to create the settings in different contexts or installations
 * (enterprse vs. non-enterprise, windows/linux/mac etc.).
 *
  */
public class KiteConfigurableFactory extends ConfigurableProvider {
    private final KiteSettingsService kiteSettingsService;

    public KiteConfigurableFactory() {
        this.kiteSettingsService = KiteSettingsService.getInstance();
    }

    @Nonnull
    @Override
    public KiteConfigurable createConfigurable() {
        return new KiteConfigurable(kiteSettingsService.getState());
    }
}
