package com.kite.intellij.platform.exec;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.util.ExecUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.kite.intellij.KiteConstants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Detects an executable application on Mac OS X. It returns the folder locations of
 * the .app application bundle on mac.
 * <p>
 * It runs <code>mdfind kMDItemCFBundleIdentifier="com.kite.Kite"</code> to detect the kite application
 * on all system, even if different application folders are used.
 *
  */
public class MacExecutableDetector implements ExecutableDetector {
    private static final Logger LOG = Logger.getInstance("#kite.platform.macExeDetector");

    @Nonnull
    private final String applicationId;
    @Nonnull
    private final String applicationEnterpriseId;

    public MacExecutableDetector() {
        this(KiteConstants.KITE_MAC_CLOUD_APPLICATION_ID, KiteConstants.KITE_MAC_ENTERPRISE_APPLICATION_ID);
    }

    /**
     * Constructor which takes the base dirs to simplify unit tests.
     *
     * @param applicationId The application id name to detect, e.g. "com.kite.Kite"
     */
    public MacExecutableDetector(@Nonnull String applicationId, @Nonnull String applicationEnterpriseId) {
        this.applicationId = applicationId;
        this.applicationEnterpriseId = applicationEnterpriseId;
    }

    @Override
    public List<Path> detectKiteExecutableFiles() {
        return detectExecutables(applicationId);
    }

    @Nonnull
    public String getApplicationId() {
        return applicationId;
    }

    @Nonnull
    public String getApplicationEnterpriseId() {
        return applicationEnterpriseId;
    }

    /**
     * Takes the stdout lines of the process and returns the application location to use.
     *
     * @param line A line of stdout output of the process. Empty lines are not included.
     * @return The location of the executable to use, if there is any.
     */
    @Nullable
    protected Path findApplicationPath(String line) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("mdfind output for " + applicationId + ": " + line);
        }

        return line != null && !line.isEmpty() && line.startsWith("/") ? Paths.get(line) : null;
    }

    /**
     * Creates the command line to execute to locate the application bundle location on the current system.
     *
     * @param applicationId
     * @return The command line which should be run to locate the application
     */
    @Nonnull
    protected GeneralCommandLine createCommandLine(String applicationId) {
        return new GeneralCommandLine("mdfind", String.format("kMDItemCFBundleIdentifier=\"%s\"", applicationId));
    }

    private List<Path> detectExecutables(String applicationId) {
        try {
            //we expect just a single line which contains the path toe the application bundle
            ProcessOutput out = ExecUtil.execAndGetOutput(createCommandLine(applicationId));

            return out.getStdoutLines(true)
                    .stream()
                    .map(this::findApplicationPath)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (ExecutionException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not detect kite executable on Mac OS X.");
            }
        }

        return Collections.emptyList();
    }
}
