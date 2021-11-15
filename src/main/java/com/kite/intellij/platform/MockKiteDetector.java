package com.kite.intellij.platform;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalInt;

@TestOnly
public class MockKiteDetector extends KiteDetector {
    @Nonnull
    private final List<String> launchedCommands = Lists.newLinkedList();
    @Nullable
    private Boolean isRunningOverride;
    @Nullable
    private List<Path> couldExecutablesOverride;

    public MockKiteDetector() {
    }

    @Nonnull
    public static MockKiteDetector getInstance() {
        return (MockKiteDetector) (KiteDetector.getInstance());
    }

    @NotNull
    public synchronized List<String> getLaunchedCommands() {
        return launchedCommands;
    }

    public synchronized void reset() {
        launchedCommands.clear();
        isRunningOverride = null;
        couldExecutablesOverride = null;
    }

    @Override
    public synchronized List<Path> detectKiteExecutableFiles() {
        if (couldExecutablesOverride != null) {
            return couldExecutablesOverride;
        }

        return super.detectKiteExecutableFiles();
    }

    @Override
    public boolean launch(@Nonnull Path executable, boolean withCopilot) {
        launchedCommands.add(executable.toString());
        return true;
    }

    @Nonnull
    @Override
    public OptionalInt detectKiteProcessId() {
        return OptionalInt.empty();
    }

    @Override
    public boolean wasLaunched() {
        return false;
    }

    @Override
    public synchronized boolean isRunning() {
        return isRunningOverride == null ? false : isRunningOverride;
    }
}
