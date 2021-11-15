package com.kite.intellij.platform.exec;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.util.ExecUtil;
import com.intellij.openapi.diagnostic.Logger;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KiteWindowsRegistryExecutableDetector implements ExecutableDetector {
    private static final Logger LOG = Logger.getInstance("#kite.platform.exeDetector");

    /**
     * This pattern matches the output of the relevant line which contains the InstallPath value.
     * <p>
     * Sample output of the reg command on windows:
     * <pre>InstallPath    REG_SZ    C:\Program Files\Kite</pre>
     */
    private static final Pattern PATTERN = Pattern.compile("^\\s*InstallPath\\s+REG_SZ\\s+(.+)\\s*$");

    //this key contains a value named "InstallPath" which defines Kite's current install location
    private static final String REGISTRY_KEY = "HKEY_LOCAL_MACHINE\\Software\\Kite\\AppData";
    //the name of the reg command on windows
    private static final String REG_COMMAND_NAME = "reg";

    private final String executableName;

    public KiteWindowsRegistryExecutableDetector() {
        this("kited.exe");
    }

    public KiteWindowsRegistryExecutableDetector(String executableName) {
        this.executableName = executableName;
    }

    @Override
    public List<Path> detectKiteExecutableFiles() {
        try {
            List<String> lines = ExecUtil.execAndGetOutput(new GeneralCommandLine(REG_COMMAND_NAME, "query", REGISTRY_KEY)).getStdoutLines();
            if (LOG.isTraceEnabled()) {
                LOG.trace("Windows registry values " + lines);
            }

            return findRegistryInstallLocation(lines)
                    .stream()
                    .map(this::findReadableProgramFile)
                    .filter(file -> file != null && Files.isExecutable(file))
                    .collect(Collectors.toList());
        } catch (ExecutionException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Detects the executable file and checks for readability. This method exists to simplify cross-platform test cases.
     *
     * @param installLocation The location to check
     * @return A filled optional if a readable file with the given file was found.
     */
    protected Path findReadableProgramFile(@Nonnull Path installLocation) {

        Path path = installLocation.resolve(executableName);

        Path result = Files.isRegularFile(path) && Files.isReadable(path) ? path : null;
        LOG.debug("Checked location " + installLocation.toAbsolutePath() + ": " + result);

        return result;
    }

    @Nonnull
    protected List<Path> findRegistryInstallLocation(List<String> stdoutLines) {
        List<Path> detectedRegistryPath = stdoutLines.stream()
                .map(line -> {
                    Matcher matcher = PATTERN.matcher(line);
                    if (matcher.matches()) {
                        return matcher.group(1);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .map(Paths::get)
                .collect(Collectors.toList());

        LOG.debug("Detected windows kite location " + detectedRegistryPath);

        return detectedRegistryPath;
    }
}
