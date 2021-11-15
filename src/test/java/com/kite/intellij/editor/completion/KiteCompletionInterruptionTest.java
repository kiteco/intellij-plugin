package com.kite.intellij.editor.completion;

import com.kite.http.KiteTestHttpdServer;
import com.kite.intellij.backend.DefaultKiteApiService;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.http.HttpRequestFailedException;
import com.kite.intellij.backend.http.HttpTimeoutConfig;
import com.kite.intellij.backend.http.JettyAsyncHttpConnection;
import com.kite.intellij.backend.http.KiteHttpConnection;
import com.kite.intellij.backend.response.KiteCompletions;
import com.kite.intellij.platform.fs.UnixCanonicalPath;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.jetbrains.ide.PooledThreadExecutor;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This test checks whether the completion http call can be properly interruped by the current ProgressIndicator context
 * managed by IntelliJ.
 *
  */
public class KiteCompletionInterruptionTest extends KiteLightFixtureTest {
    @Test
    public void testInterruptionJetty() throws Exception {
        KiteTestHttpdServer server = new KiteTestHttpdServer();
        server.start();

        try {
            //to make sure the request is active when the future is cancelled
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch finishedLatch = new CountDownLatch(1);

            server.addPostPathHandler("/clientapi/editor/event", (path, payload) -> "");
            server.addPostPathHandler("/clientapi/editor/complete", (path, payload) -> {
                try {
                    startLatch.countDown();
                    Thread.sleep(1000);

                    return "{\"completions\":[{\"display\": \"One\", \"insert\": \"One\"}]}";
                } catch (InterruptedException e) {
                    throw new HttpRequestFailedException("Error", e);
                }
            });

            KiteHttpConnection httpConnection = new JettyAsyncHttpConnection(server.getHostname(), server.getListeningPort());
            KiteApiService api = DefaultKiteApiService.create(httpConnection);

            AtomicLong requestDuration = new AtomicLong(Integer.MAX_VALUE);

            Future<KiteCompletions> future = PooledThreadExecutor.INSTANCE.submit(() -> {
                long start = System.currentTimeMillis();
                try {
                    return api.completions(new UnixCanonicalPath("/home/user/test.py"), "", 0, null, false, HttpTimeoutConfig.ShortTimeout);
                } finally {
                    requestDuration.set(System.currentTimeMillis() - start);
                    finishedLatch.countDown();
                }
            });

            try {
                startLatch.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            future.cancel(true);
            finishedLatch.await(2, TimeUnit.SECONDS);

            Assert.assertTrue("The future's http request have been cancelled, it must have taken shorter than the server's response time: " + requestDuration.get() + " ms",
                    requestDuration.get() >= 0 && requestDuration.get() < 1000);
        } finally {
            server.stop();
        }
    }
}