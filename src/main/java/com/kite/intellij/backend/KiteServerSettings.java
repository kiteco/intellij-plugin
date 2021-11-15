package com.kite.intellij.backend;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.kite.intellij.backend.http.KiteHttpException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Our supported set of settings which are managed by the kited server.
 *
  */
public enum KiteServerSettings {
    HasDoneOnboarding("has_done_onboarding"),
    IsJavaScriptEnabled("kite_js_enabled"),
    MaxFileSize("max_file_size_kb");

    private static final Logger LOG = Logger.getInstance("#kite.settings");
    private final String settingName;

    KiteServerSettings(@Nonnull String settingName) {
        this.settingName = settingName;
    }

    @Nullable
    public String get(@Nonnull KiteApiService api) {
        // run under progress if on EDT
        if (ApplicationManager.getApplication().isDispatchThread()) {
            AtomicReference<String> result = new AtomicReference<>(null);
            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                    () -> result.set(api.getSetting(this.settingName)),
                    "Kite Settings", false, null);
            return result.get();
        }

        // sync call in background thread
        return api.getSetting(this.settingName);
    }

    public void set(@Nonnull KiteApiService api, String value) throws KiteHttpException {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            // move to background thread on EDT
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    api.setSetting(settingName, value);
                } catch (KiteHttpException e) {
                    LOG.debug("error setting kite config value " + value, e);
                }
            });
        } else {
            // already a background thread
            api.setSetting(settingName, value);
        }
    }

    @Nullable
    public Boolean getBoolean(KiteApiService api) {
        String v = this.get(api);
        return v == null ? null : Boolean.parseBoolean(v);
    }

    public void setBoolean(@Nonnull KiteApiService api, boolean value) throws KiteHttpException {
        this.set(api, Boolean.toString(value));
    }

    @Nullable
    public Long getLong(KiteApiService api) {
        String v = this.get(api);
        if (v == null) {
            return null;
        }

        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
