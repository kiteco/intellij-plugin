package com.kite.intellij.editor.completion;

import com.intellij.codeInsight.completion.CompletionType;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.editor.events.KiteEventQueue;
import com.kite.intellij.editor.events.KiteEventQueueTest;
import com.kite.intellij.editor.events.TestcaseEditorEventListener;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class KiteCompletionContributorTimeoutTest extends KiteLightFixtureTest {
    @Test
    @Ignore("This currently deadlocks if all tests are run, emptyQueueCondition.awaitNanos(nanos) of KiteEventQueue doesn't return for unknown reasons")
    public void _testCompletionTimeout() throws Exception {
        setupCompletions();
        TestcaseEditorEventListener.sleepForQueueWork(getProject());

        KiteEventQueue queue = KiteEventQueue.getInstance(getProject());

        AtomicBoolean wait = new AtomicBoolean(true);
        queue.addEvent(new KiteEventQueueTest.AbstractTestEvent() {
            @Override
            public boolean doSend() {
                try {
                    while (wait.get()) {
                        Thread.sleep(200);
                    }
                } catch (InterruptedException ignored) {
                }

                return false;
            }
        });

        //unlock the queue in the background
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                latch.await();

                //wait long enough to let the completions call time out
                Thread.sleep(2000);
                wait.set(false);
            } catch (InterruptedException ignored) {
            }
        }).start();

        myFixture.complete(CompletionType.BASIC);
        latch.countDown();

        List<String> items = myFixture.getLookupElementStrings();
        //3 is the no of completions returned by PyCharm
        Assert.assertEquals("If kite's completions timed out then a fallback to PyCharm must be done", 3, items.size());
    }

    @Override
    protected String getBasePath() {
        return "python/editor/codeCompletion/";
    }

    private void setupCompletions() {
        MockKiteHttpConnection httpConnection = MockKiteHttpConnection.getInstance();
        httpConnection.addPostPathHandler("/clientapi/editor/complete", (path, payload) -> loadFile("json/completionTimeout.json"), getTestRootDisposable());
        httpConnection.addPostPathHandler("/clientapi/editor/event", (path, payload) -> "{}", getTestRootDisposable());

        MockKiteApiService api = MockKiteApiService.getInstance();
        api.enableHttpCalls();

        myFixture.configureByFile("completionTimeout.py");
    }
}