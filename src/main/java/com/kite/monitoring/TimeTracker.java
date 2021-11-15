package com.kite.monitoring;

/**
 * Simple class to track the time an action took.
 *
  */
public interface TimeTracker extends AutoCloseable {
    TimeTracker start();

    void stop();

    /**
     * Overloaded to throw no exception.
     */
    void close();

    boolean isStopped();

    long getNanoDuration();

    double getMillisDuration();
}
