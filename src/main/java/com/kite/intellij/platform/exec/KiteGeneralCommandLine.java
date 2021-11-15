package com.kite.intellij.platform.exec;

import com.intellij.execution.configurations.GeneralCommandLine;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

/**
 * An extended version of {@link GeneralCommandLine} which supports redirect targets for the created {@link Process}.
 *
  */
public class KiteGeneralCommandLine extends GeneralCommandLine {
    private ProcessBuilder.Redirect stdoutRedirect;
    private ProcessBuilder.Redirect stdinRedirect;
    private ProcessBuilder.Redirect stderrRedirect;

    public KiteGeneralCommandLine() {
    }

    public KiteGeneralCommandLine(@NotNull String... command) {
        super(command);
    }

    public KiteGeneralCommandLine(@NotNull List<String> command) {
        super(command);
    }

    public void setStdinRedirect(ProcessBuilder.Redirect redirect) {
        this.stdinRedirect = redirect;
    }

    public void setStdoutRedirect(ProcessBuilder.Redirect redirect) {
        this.stdoutRedirect = redirect;
    }

    public void setStderrRedirect(ProcessBuilder.Redirect redirect) {
        this.stderrRedirect = redirect;
    }

    @NotNull
    @Override
    protected Process startProcess(@NotNull List<String> commands) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(commands);
        setupEnvironment(builder.environment());
        builder.directory(getWorkDirectory());

        if (stdinRedirect != null) {
            builder.redirectInput(stdinRedirect);
        }

        if (stdoutRedirect != null) {
            builder.redirectOutput(stdoutRedirect);
        }

        builder.redirectErrorStream(isRedirectErrorStream());
        if (!isRedirectErrorStream()) {
            builder.redirectError(stderrRedirect);
        }

        return builder.start();
    }
}
