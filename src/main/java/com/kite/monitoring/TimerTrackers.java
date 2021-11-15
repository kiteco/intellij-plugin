package com.kite.monitoring;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;

/**
 */
public class TimerTrackers {
    private static final TimeTracker NO_OP_TRACKER = new NoOpTimeTracker();
    private static final Logger LOG = Logger.getInstance("#kite.timing");
    private static final boolean enabled = LOG.isDebugEnabled()
            && DefaultTimeTracker.isEnabled(LOG)
            && ApplicationManager.getApplication() != null
            && !ApplicationManager.getApplication().isUnitTestMode();

    public static boolean isEnabled() {
        return enabled;
    }

    public static TimeTracker start(String name) {
        return create(name).start();
    }

    public static TimeTracker create(String name) {
        if (!enabled) {
            return NO_OP_TRACKER;
        }

        return new DefaultTimeTracker(name, LOG);
    }
}
