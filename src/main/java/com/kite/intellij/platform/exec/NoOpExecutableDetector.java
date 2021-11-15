package com.kite.intellij.platform.exec;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public final class NoOpExecutableDetector implements ExecutableDetector {
    @Override
    public List<Path> detectKiteExecutableFiles() {
        return Collections.emptyList();
    }
}
