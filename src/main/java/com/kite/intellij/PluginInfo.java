package com.kite.intellij;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.PluginAware;

import javax.annotation.Nullable;

/**
 * Provide a custom extensionw implementing PluginAware to trick IntelliJ into providing the plugin information.
 * The plugin descriptor is only added to extensions implementing PluginAware, not to components or services.
 *
  */
public interface PluginInfo extends PluginAware {
    ExtensionPointName<PluginInfo> EP_NAME = ExtensionPointName.create("com.kite.intellij.pluginInfo");

    @Nullable
    String getVersion();
}
