package com.kite.monitoring;

import com.intellij.openapi.diagnostic.Logger;

import javax.annotation.Nullable;

/**
 * Default implementation of a time tracker which prints trace information.
 *
  */
public class DefaultTimeTracker implements TimeTracker {
    private final String name;
    @Nullable
    private final Logger log;
    private long start;
    private boolean stopped;
    private long nanoDuration;
    private double millisDuration;

    public DefaultTimeTracker(String name, @Nullable Logger log) {
        this.name = name;
        this.log = log;
    }

    public static boolean isEnabled(Logger log) {
        return log.isTraceEnabled();
    }

    @Override
    public TimeTracker start() {
        start = System.nanoTime();
        return this;
    }

    @Override
    public void stop() {
        this.stopped = true;
        this.nanoDuration = System.nanoTime() - start;
        this.millisDuration = (double) nanoDuration / 1000d / 1000d;

        if (log != null && log.isTraceEnabled()) {
            log.trace(name + ": " + (int) (nanoDuration / 1000d) / 1000d + " ms" + " , thread: " + Thread.currentThread().getName());
        }
    }

    @Override
    public void close() {
        stop();
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public long getNanoDuration() {
        return nanoDuration;
    }

    @Override
    public double getMillisDuration() {
        return millisDuration;
    }
}
