package com.kite.intellij.platform.exec;

import java.nio.file.Path;
import java.util.List;

/**
 * Manages the Kite executable on the current system.
 *
  */
public interface ExecutableDetector {
    /**
     * Locates the Kite cloud executable.
     *
     * @return Kite's executable locations, if found and executable.
     */
    List<Path> detectKiteExecutableFiles();
}
