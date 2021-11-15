package com.kite.intellij.backend;

import com.intellij.openapi.application.ApplicationManager;

public class MockKiteServerSettingsService extends KiteServerSettingsServiceImpl {
    private volatile boolean isEnabled;

    @Override
    public void reset() {
        super.reset();

        this.setEnabled(false);
    }

    public static MockKiteServerSettingsService getInstance() {
        return (MockKiteServerSettingsService) ApplicationManager.getApplication().getService(KiteServerSettingsService.class);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    protected void updateMaxFileSize() {
        if (isEnabled) {
            super.updateMaxFileSize();
        }
    }
}
