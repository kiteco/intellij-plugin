package com.kite.intellij.editor.events;

import com.intellij.openapi.project.Project;
import com.kite.intellij.backend.http.KiteHttpException;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An event queue handling events send to the Kite daemon.
 *
  */
public interface KiteEventQueue {
    /**
     * Central place to retrieve the project's event queue.
     *
     * @param project The project to use
     * @return The event queue instance used in the given project
     */
    @Nonnull
    static KiteEventQueue getInstance(Project project) {
        return EditorEventListener.getInstance(project).getEventQueue();
    }

    /**
     * Starts the queue processing. If start() is not called not event processing will be done.
     */
    void start();

    /**
     * Stops the queue processing. Any pending events will be cancelled. The queue can not be started again after
     * {@code stop()} has been called.
     */
    void stop();

    /**
     * Runs the given computable on the current thread as soon as the queue finished processing all requests.
     * A default timeout is applied.
     *
     * @param computable
     * @param <T>
     * @return
     * @throws TimeoutException
     * @throws InterruptedException
     * @throws KiteHttpException
     */
    <T> T runWhenEmpty(KiteQueueComputable<T> computable) throws TimeoutException, InterruptedException, KiteHttpException;

    /**
     * Runs the given computable on the current thread as soon as the queue finished processing all pending requests.
     * If new requests are added while the queue is not empty these will be processed before the computable is run.
     *
     * @param timeout    The timeout value to wait at most for an empty queue
     * @param unit       The unit of the {@code timeout} value
     * @param computable The computable to run as soon as the queue is empty.
     * @param <T>        The type of the value computed by the computable.
     * @return The value computed by the computable
     * @throws TimeoutException     Thrown if the max wait time for an empty queue was reached
     * @throws InterruptedException Thrown if the computable was interrupted while waiting
     * @throws KiteHttpException    Http exceptions thrown by the computable are passed on to the callee
     */
    <T> T runWhenEmpty(long timeout, TimeUnit unit, KiteQueueComputable<T> computable) throws TimeoutException, InterruptedException, KiteHttpException;

    /**
     * Appends a new event to the queue. If will be merged with the previously queued event if is able to override it,
     * i.e. if it provides at least the same information and processing the previous is not strictly necesaryy for other reasons.
     *
     * @param newEvent The event to add.
     */
    void addEvent(KiteEvent newEvent);

    @FunctionalInterface
    interface KiteQueueComputable<T> {
        T compute() throws KiteHttpException;
    }
}
