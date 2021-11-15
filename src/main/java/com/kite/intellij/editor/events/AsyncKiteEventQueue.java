package com.kite.intellij.editor.events;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.kite.intellij.KiteConstants;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.backend.model.EventType;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Queue implementation, which works without a lock in addEvent.
 * It uses a queue, which is shared between producer and consumer thread. The producer adds events without blocking.
 * The consumer blocks while waiting for the next event. As soon as it has received a new event, it's processed.
 *
  */
public class AsyncKiteEventQueue implements KiteEventQueue {
    private static final Logger LOG = Logger.getInstance("#kite.eventQueue");

    private final TransferQueue<KiteEvent> eventQueue = new LinkedTransferQueue<>();

    @Nullable
    private WorkerThread thread;

    @Override
    public synchronized void start() {
        if (thread != null) {
            throw new IllegalStateException("start may only be called once.");
        }

        thread = new WorkerThread(eventQueue);
        thread.start();
    }

    @Override
    public synchronized void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    @Override
    public <T> T runWhenEmpty(KiteQueueComputable<T> computable) throws TimeoutException, InterruptedException, KiteHttpException {
        return runWhenEmpty(KiteConstants.EMPTY_QUEUE_TIMEOUT, TimeUnit.MILLISECONDS, computable);
    }

    @Override
    public <T> T runWhenEmpty(long timeout, TimeUnit unit, KiteQueueComputable<T> computable) throws TimeoutException, InterruptedException, KiteHttpException {
        // using a latch here, because eventQueue.tryTransfer returns immediately when a take() is in progress
        // but that could happen before the taken event has been processed
        CountDownLatch latch = new CountDownLatch(1);
        eventQueue.add(new KiteLatchCountdownEvent(latch));

        boolean success = latch.await(timeout, unit);
        if (!success) {
            LOG.warn("timeout while waiting queue to become empty");
            throw new TimeoutException("timeout waiting for empty queue");
        }
        return computable.compute();
    }

    @Override
    public void addEvent(KiteEvent newEvent) {
        try {
            boolean success = eventQueue.add(newEvent);
            if (!success) {
                LOG.debug("failed to add new event to event queue");
            }
        } catch (IllegalStateException e) {
            throw new RuntimeException("unable to add the event to the queue", e);
        }
    }

    private static final class WorkerThread extends Thread {
        private static final Logger LOG = Logger.getInstance("#kite.eventQueue");
        private final TransferQueue<KiteEvent> eventQueue;

        private WorkerThread(TransferQueue<KiteEvent> eventQueue) {
            super("kite-event-queue");
            setDaemon(true);

            this.eventQueue = eventQueue;
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    KiteEvent next = eventQueue.take();

                    // fetch all other queued events, if there are any
                    // then deduplicate the events and send the remaining to kited
                    ArrayList<KiteEvent> queuedEvents = new ArrayList<>();
                    queuedEvents.add(next);
                    eventQueue.drainTo(queuedEvents);

                    try {
                        shrinkQueue(queuedEvents);
                        for (KiteEvent event : queuedEvents) {
                            event.send(KiteApiService.getInstance());
                        }
                    } catch (Exception e) {
                        LOG.debug("exception while sending kite event", e);
                    }
                }
            } catch (InterruptedException e) {
                LOG.debug("kite event queue worked stopped by InterruptedException");
            }

            // make sure to remove any pending event when stopped
            eventQueue.clear();
        }

        /**
         * Removes all events which are not needed any more. Events which have follow-up events of the same type
         * will be removed from the queue.
         * The queue lock must be acquired by the current thread before calling this method. The events to send are returned
         * by this method, the state of the original queue is not modified.
         *
         * @param events The list of events to deduplicate. This list is modified in-place.
         */
        private void shrinkQueue(ArrayList<KiteEvent> events) {
            int size = events.size();
            if (size == 0 || size == 1) {
                return;
            }

            //set the overridden events to null
            for (int i = events.size() - 1; i >= 1; i--) {
                KiteEvent master = events.get(i);
                if (master == null) {
                    continue;
                }

                //all events send before the master event may be overridden by it
                //all overridden events are set to null and won't be in the result returned by this method
                for (int compareIndex = i - 1; compareIndex >= 0; compareIndex--) {
                    KiteEvent current = events.get(compareIndex);
                    if (current != null && master.isOverriding(current)) {
                        events.set(compareIndex, null);
                    }
                }
            }

            // drop all non-null elements
            events.removeIf(Objects::isNull);
        }
    }

    private static class KiteLatchCountdownEvent implements KiteEvent {
        private final CountDownLatch latch;

        public KiteLatchCountdownEvent(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public EventType getType() {
            return EventType.SKIP;
        }

        @Override
        public boolean send(@NotNull KiteApiService api) {
            latch.countDown();
            return true;
        }

        @Override
        public boolean isOverriding(KiteEvent previous) {
            return false;
        }

        @NotNull
        @Override
        public CanonicalFilePath getFilePath() {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public String getContent() {
            throw new UnsupportedOperationException();
        }

        @Nullable
        @Override
        public Document getDocument() {
            throw new UnsupportedOperationException();
        }
    }
}
