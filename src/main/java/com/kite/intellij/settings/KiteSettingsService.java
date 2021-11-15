package com.kite.intellij.settings;

import com.google.common.collect.Lists;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

/**
 * The central place where {@link KiteSettings} are managed.
 *
  */
@State(name = "kite", storages = @Storage("kite.settings.xml"))
public class KiteSettingsService implements PersistentStateComponent<KiteSettings> {
    @Nonnull
    private volatile KiteSettings settings = new KiteSettings();
    private final List<Consumer<KiteSettings>> settingsListeners = Lists.newLinkedList();

    public KiteSettingsService() {
    }

    public static KiteSettingsService getInstance() {
        return ServiceManager.getService(KiteSettingsService.class);
    }

    @Nonnull
    @Override
    public KiteSettings getState() {
        return settings;
    }

    @Override
    public void loadState(@NotNull KiteSettings state) {
        this.settings = state;
    }

    public <T extends Disposable & Consumer<KiteSettings>> void registerSettingsListener(T listener) {
        this.settingsListeners.add(listener);

        Disposer.register(listener, () -> settingsListeners.remove(listener));
    }

    void triggerSettingsChange(KiteSettings updatedSettings) {
        settingsListeners.forEach(l -> l.accept(updatedSettings));
    }
}
