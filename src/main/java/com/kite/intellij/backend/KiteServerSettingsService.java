package com.kite.intellij.backend;

import com.intellij.openapi.application.ApplicationManager;

public interface KiteServerSettingsService {
    static KiteServerSettingsService getInstance() {
        return ApplicationManager.getApplication().getService(KiteServerSettingsService.class);
    }

    // resets the stored max file size, useful in tests
    void reset();

    long getMaxFileSizeBytes();
}
