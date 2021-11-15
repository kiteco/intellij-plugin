package com.kite.intellij.ui;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.util.concurrency.SwingWorker;

import java.util.function.Consumer;

public class SwingWorkerUtil {
    /**
     * Runs {@code backgroundRunnable} in the background to compute a value of type {@link T} and then calls {@code swingCallback}
     * with this value in the Swing EDT. The implementation uses a {@link com.intellij.util.concurrency.SwingWorker} to perform the execution.
     */
    public static <T> void compute(Computable<T> backgroundRunnable, Consumer<T> swingCallback) {
        //don't do background processing in test cases, this would be badly testable
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            try {
                T result = backgroundRunnable.compute();
                swingCallback.accept(result);
            } catch (Exception e) {
                swingCallback.accept(null);
            }

            return;
        }

        new SwingWorker<T>() {
            @Override
            public T construct() {
                return backgroundRunnable.compute();
            }

            @Override
            public void finished() {
                swingCallback.accept(getValue());
            }

            @Override
            public void onThrowable() {
                Application application = ApplicationManager.getApplication();

                if (application.isDispatchThread()) {
                    swingCallback.accept(null);
                } else {
                    application.invokeLater(() -> swingCallback.accept(null));
                }
            }
        }.start();
    }
}
