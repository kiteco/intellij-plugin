package com.kite.intellij.ui;

import org.jetbrains.annotations.TestOnly;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class KiteTestUtil {
    public volatile static boolean isIntegrationTesting = false;

    @TestOnly
    public static boolean isIntegrationTesting() {
        return "true".equals(System.getenv("KITED_TEST")) && isIntegrationTesting;
    }

    /**
     * @param runnable
     * @throws Throwable
     * @see com.intellij.testFramework.EdtTestUtil
     */
    @TestOnly
    public static void runInEdtAndWait(Runnable runnable) throws Throwable {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
            return;
        }

        try {
            SwingUtilities.invokeAndWait(runnable);
        } catch (InvocationTargetException | InterruptedException e) {
            if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
    }
}
