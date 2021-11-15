package com.kite.intellij.platform;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("MissingRecentApi")
public class MockKiteInstallService implements KiteInstallService {
    private volatile boolean isInstalling;

    public static MockKiteInstallService getInstance() {
        return (MockKiteInstallService) ApplicationManager.getApplication().getService(KiteInstallService.class);
    }

    @Override
    public boolean isInstalling() {
        return isInstalling;
    }

    @Override
    public boolean canInstall() {
        return true;
    }

    @TestOnly
    public void setInstalling(boolean installing) {
        this.isInstalling = installing;
    }

    @Override
    public @NotNull File downloadSetup() throws IOException {
        throw new IllegalStateException("unsupported");
    }

    @Override
    public boolean runSetup(@NotNull File setupFile) throws ExecutionException, InterruptedException, IOException {
        throw new IllegalStateException("unsupported");
    }

    @TestOnly
    public void reset() {
        this.isInstalling = false;
    }
}
