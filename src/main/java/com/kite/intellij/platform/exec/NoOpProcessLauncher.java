package com.kite.intellij.platform.exec;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public class NoOpProcessLauncher implements KiteProcessLauncher {
    @Override
    public boolean launch(@Nonnull Path executable, boolean withCopilot) {
        return false;
    }
}
