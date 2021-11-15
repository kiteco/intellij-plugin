package com.kite.intellij.platform.exec;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public interface KiteProcessLauncher {
    /**
     * @param executable  The executable file which should be launched.
     * @param withCopilot If Copilot should be launched by Kite.
     * @return {@code true} if the new process was successfully launched.
     */
    boolean launch(@Nonnull Path executable, boolean withCopilot);
}
