package com.kite.intellij.platform.exec;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.util.io.BaseOutputReader;
import org.jetbrains.annotations.NotNull;

class KiteDetachedProcessHandler extends OSProcessHandler {
    public KiteDetachedProcessHandler(GeneralCommandLine commandLine) throws ExecutionException {
        super(commandLine);
    }

    @Override
    public boolean detachIsDefault() {
        return true;
    }

    @NotNull
    @Override
    protected BaseOutputReader.Options readerOptions() {
        return BaseOutputReader.Options.NON_BLOCKING;
    }
}
