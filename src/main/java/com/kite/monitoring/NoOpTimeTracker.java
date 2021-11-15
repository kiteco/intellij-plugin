package com.kite.monitoring;

/**
 * A no-op time tracker to be used if not debug output is generated.
 *
  */
public final class NoOpTimeTracker implements TimeTracker {
    @Override
    public TimeTracker start() {
        return this;
    }

    @Override
    public void stop() {

    }

    @Override
    public void close() {

    }

    @Override
    public long getNanoDuration() {
        return 0;
    }

    @Override
    public double getMillisDuration() {
        return 0d;
    }

    @Override
    public boolean isStopped() {
        return true;
    }
}
