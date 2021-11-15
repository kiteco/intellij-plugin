package com.kite.intellij.platform.exec;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fallback implementation which looks at the current path setting to locate an executable named "kited".
 * This implementation supports Windows and unix style path variable configurations.
 *
  */
public class PathFallbackExecutableDetector implements ExecutableDetector {
    private static final Logger LOG = Logger.getInstance("#kite.platform.pathLauncher");

    private final String cloudExecutableName;
    private final String enterpriseExecutableName;

    public PathFallbackExecutableDetector(String cloudExecutableName, String enterpriseExecutableName) {
        this.cloudExecutableName = cloudExecutableName;
        this.enterpriseExecutableName = enterpriseExecutableName;
    }

    @Override
    public List<Path> detectKiteExecutableFiles() {
        return detectExecutables(cloudExecutableName);
    }

    @Nonnull
    protected List<Path> detectExecutables(String executableName) {
        String systemPath = System.getenv("PATH");

        List<Path> executables = StringUtil.split(systemPath, File.pathSeparator)
                .stream()
                .map(path -> Paths.get(path, executableName))
                .filter((Path potentialFile) -> Files.isReadable(potentialFile) && Files.isRegularFile(potentialFile) && Files.isExecutable(potentialFile))
                .collect(Collectors.toList());

        LOG.debug("Detected kite executable: " + executables);

        return executables;
    }
}
