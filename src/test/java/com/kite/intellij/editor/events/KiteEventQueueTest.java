package com.kite.intellij.editor.events;

import com.intellij.mock.MockDocument;
import com.intellij.openapi.editor.Document;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.model.EventType;
import com.kite.intellij.backend.model.TextSelection;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import com.kite.intellij.platform.fs.UnixCanonicalPath;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class KiteEventQueueTest extends KiteLightFixtureTest {
    @Test
    public void testBasics() throws Exception {
        MockKiteApiService api = MockKiteApiService.getInstance();

        TestEventDelegate focus1 = delegate(new FocusEvent(null, new UnixCanonicalPath("file.py"), "content", 1, true), api);
        TestEventDelegate focus2 = delegate(new FocusEvent(null, new UnixCanonicalPath("file.py"), "content", 1, true), api);

        TestEventDelegate selection1 = delegate(new SelectionEvent(null, new UnixCanonicalPath("file.py"), "content", TextSelection.create(1), true), api);
        TestEventDelegate selection2 = delegate(new SelectionEvent(null, new UnixCanonicalPath("file.py"), "content", TextSelection.create(1), true), api);

        TestEventDelegate edit1 = delegate(new EditEvent(null, new UnixCanonicalPath("file.py"), "content", 1, true), api);
        TestEventDelegate edit2 = delegate(new EditEvent(null, new UnixCanonicalPath("file.py"), "content", 1, true), api);

        KiteEventQueue q = new AsyncKiteEventQueue();
        try {
            q.addEvent(focus1);
            q.addEvent(selection1);
            q.addEvent(edit1);//suppress selection1
            q.addEvent(focus2); //do not suppress focus1, as focus events are imported to be in order
            q.addEvent(selection2);
            q.addEvent(edit2); //suppress selection1, edit1 and selection2

            q.start();
            q.runWhenEmpty(5, TimeUnit.SECONDS, () -> null);

            Assert.assertTrue("focus1 must be send", focus1.isSend());

            Assert.assertFalse("selection1 must be suppressed", selection1.isSend());

            Assert.assertFalse("edit1 must be suppressed", edit1.isSend());

            Assert.assertTrue("focus2 must be send", focus2.isSend());

            Assert.assertFalse("selection2 must be suppressed", selection2.isSend());

            Assert.assertTrue("edit2 must be send", edit2.isSend());
        } finally {
            q.stop();
        }
    }

    @Test
    public void testEditReplacement() throws Exception {
        MockKiteApiService api = MockKiteApiService.getInstance();
        KiteEventQueue q = new AsyncKiteEventQueue();

        Document document = new MockDocument();

        TestEventDelegate edit1 = delegate(new EditEvent(document, new UnixCanonicalPath("file.py"), "content", 1, true), api);
        TestEventDelegate edit2 = delegate(new EditEvent(document, new UnixCanonicalPath("file.py"), "content", 1, true), api);
        TestEventDelegate selection1 = delegate(new SelectionEvent(document, new UnixCanonicalPath("file.py"), "content selection", TextSelection.create(1), true), api);
        TestEventDelegate selection2 = delegate(new SelectionEvent(document, new UnixCanonicalPath("file.py"), "content selection other", TextSelection.create(2), true), api);

        q.addEvent(edit1);
        q.addEvent(edit2);
        q.addEvent(selection1);
        q.addEvent(selection2);

        try {
            q.start();
            q.runWhenEmpty(5, TimeUnit.SECONDS, () -> null);
        } finally {
            q.stop();
        }

        Assert.assertFalse("edit1 must not be send", edit1.isSend());
        Assert.assertFalse("edit2 must not be send", edit2.isSend());
        Assert.assertFalse("selection1 must not be send", selection1.isSend());

        Assert.assertTrue("selection2 must have been sent", selection2.isSend());

        //a replacement event must be send
        Assert.assertEquals("A replacement event must be send: " + api.getCallHistory(), 1, api.getCallHistory().size());
        Assert.assertEquals("Expected an edit event as replacement", "sendEvent(selection, file.py, content selection other, [2,2])", api.getCallHistory().get(0));
    }

    @Test
    public void testRunWhenEmpty() throws Exception {
        KiteEventQueue q = new AsyncKiteEventQueue();

        CountDownLatch startLatch = new CountDownLatch(1);

        //processing these three takes at least 2.25s
        q.addEvent(new DelayingEvent(startLatch, 750, "file.py", "content", TextSelection.create(0), null));
        q.addEvent(new DelayingEvent(startLatch, 750, "file.py", "content", TextSelection.create(0), null));
        q.addEvent(new DelayingEvent(startLatch, 750, "file.py", "content", TextSelection.create(0), null));

        long start = System.currentTimeMillis();

        try {
            q.start();
            startLatch.countDown();

            AtomicLong runTime = new AtomicLong(0);
            boolean successfullyRun = q.runWhenEmpty(5, TimeUnit.SECONDS, () -> {
                runTime.set(System.currentTimeMillis() - start);
                return true;
            });

            Assert.assertTrue("The computable must have been run!", successfullyRun);
            Assert.assertTrue("The waiting time until execution must be less than the timeout: " + runTime.get(), runTime.get() > 0 && runTime.get() < 4000);
        } finally {
            q.stop();
        }
    }

    @Test
    public void testRunWhenEmptyTimeout() throws Exception {
        KiteEventQueue q = new AsyncKiteEventQueue();

        CountDownLatch startLatch = new CountDownLatch(1);

        //processing these three takes at least 6000ms
        q.addEvent(new DelayingEvent(startLatch, 2000, "file1.py", "content", TextSelection.create(0), null));
        q.addEvent(new DelayingEvent(startLatch, 2000, "file2.py", "content", TextSelection.create(0), null));
        q.addEvent(new DelayingEvent(startLatch, 2000, "file3.py", "content", TextSelection.create(0), null));

        try {
            //the processing starts immediately
            q.start();
            startLatch.countDown();

            boolean timeout = false;
            try {
                q.runWhenEmpty(750, TimeUnit.MILLISECONDS, () -> false);
            } catch (TimeoutException e) {
                timeout = true;
            }

            Assert.assertTrue("The computable must not run, runWhenEmpty has to timeout!", timeout);
        } finally {
            q.stop();
        }
    }

    @Override
    protected String getBasePath() {
        return "python/editor/eventQueue";
    }

    private TestEventDelegate delegate(KiteEvent event, KiteApiService api) {
        return new TestEventDelegate(event, api);
    }

    private static class DelayingEvent extends AbstractKiteEvent {
        private final CountDownLatch startLatch;
        private final long timeoutMillis;

        public DelayingEvent(CountDownLatch startLatch, long timeoutMillis, String filePath, String content, TextSelection selection, Document document) {
            super(EventType.EDIT, new UnixCanonicalPath(filePath), content, selection, document, true);
            this.startLatch = startLatch;
            this.timeoutMillis = timeoutMillis;
        }

        @Override
        public boolean send(@NotNull KiteApiService api) {
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException("failed to wait for test start", e);
            }

            try {
                Thread.sleep(timeoutMillis);
                return true;
            } catch (InterruptedException e) {
                return false;
            }
        }

        @Override
        public boolean isOverriding(KiteEvent previous) {
            return false;
        }

    }

    public static abstract class AbstractTestEvent extends AbstractKiteEvent {
        public AbstractTestEvent() {
            super(EventType.EDIT, new UnixCanonicalPath("/home/user/test.py"), "", null, null, true);
        }

        public abstract boolean doSend();

        @Override
        public boolean send(@NotNull KiteApiService api) {
            return doSend();
        }

        @Override
        public boolean isOverriding(KiteEvent previous) {
            return false;
        }
    }

    private class TestEventDelegate implements KiteEvent {
        private final KiteApiService api;
        KiteEvent delegate;
        AtomicBoolean send = new AtomicBoolean(false);

        public TestEventDelegate(KiteEvent event, KiteApiService api) {
            this.delegate = event;
            this.api = api;
        }

        public boolean isSend() {
            return send.get();
        }

        @Override
        public EventType getType() {
            return delegate.getType();
        }

        @Override
        public boolean send(@NotNull KiteApiService api) {
            send.set(true);
            delegate.send(api);

            return true;
        }

        @Override
        public boolean isOverriding(KiteEvent previous) {
            return delegate.isOverriding(previous);
        }

        @Override
        @Nonnull
        public CanonicalFilePath getFilePath() {
            return delegate.getFilePath();
        }

        @Nonnull
        @Override
        public String getContent() {
            return delegate.getContent();
        }

        @Nullable
        public Document getDocument() {
            return delegate.getDocument();
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || delegate.equals(obj);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }
}