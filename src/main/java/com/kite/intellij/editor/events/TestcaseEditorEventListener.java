package com.kite.intellij.editor.events;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.kite.intellij.KiteConstants;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.model.EventType;
import com.kite.intellij.backend.model.TextSelection;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory;
import com.kite.intellij.platform.fs.UnixCanonicalPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Event listener used in test cases. This has a slower queue rate to work with slowly operating test cases.
 *
  */
@SuppressWarnings("ComponentNotRegistered")
@TestOnly
public class TestcaseEditorEventListener extends DefaultEditorEventListener {
    public TestcaseEditorEventListener(Project project) {
        super(project,
                KiteConstants.DEFAULT_QUEUE_TIMEOUT_MILLIS,
                CanonicalFilePathFactory.getInstance(),
                KiteConstants.ALARM_DELAY_MILLIS);
    }

    public static void sleepForQueueWork(Project project) throws InterruptedException {
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            EditorEventListener.getInstance(project).awaitEvents();
        } else {
            application.invokeAndWait(() -> {
                EditorEventListener.getInstance(project).awaitEvents();
            }, ModalityState.any());
        }

        CountDownLatch latch = new CountDownLatch(1);

        KiteEventQueue.getInstance(project).addEvent(new AbstractKiteEvent(EventType.EDIT, new UnixCanonicalPath("/dummy"), "", TextSelection.create(0, 1), null, false) {
            @Override
            public boolean send(@NotNull KiteApiService api) {
                latch.countDown();
                return true;
            }

            @Override
            public boolean isOverriding(KiteEvent previous) {
                return false;
            }
        });

        latch.await(2000, TimeUnit.MILLISECONDS);
    }
}
