package com.kite.intellij.editor.events;

import com.intellij.mock.MockDocument;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationsManager;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.util.Alarm;
import com.kite.intellij.KiteRuntimeInfo;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.backend.http.test.RequestInfo;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory;
import com.kite.intellij.test.KiteLightFixtureTest;
import com.kite.intellij.test.KiteTestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests the event queue handling and posting.
 *
  */
public class DefaultEditorEventListenerTest extends KiteLightFixtureTest {
    @Test
    public void testFocusEvent() throws Exception {
        MockKiteApiService api = MockKiteApiService.getInstance();
        Assert.assertTrue("No api calls expected before the edit events", api.getCallHistory().isEmpty());

        //opens the file and set the caret position
        KiteTestUtils.configureByFileAndFocus("empty.py", myFixture);
    }

    @Test
    public void testEventProperties() throws Exception {
        MockKiteApiService api = MockKiteApiService.getInstance();
        api.enableHttpCalls();
        api.turnOnline();
        Assert.assertTrue("No api calls expected before the edit events", api.getCallHistory().isEmpty());

        //opens the file and set the caret position
        KiteTestUtils.configureByFileAndFocus("empty.py", myFixture);
        TestcaseEditorEventListener.sleepForQueueWork(getProject());

        List<RequestInfo> history = MockKiteHttpConnection.getInstance().getHttpRequestHistory();
        Optional<RequestInfo> requestInfo = history.stream().filter(i -> i.getPath().equals("/clientapi/editor/event")).findFirst();
        Assert.assertTrue("request info not found in ", requestInfo.isPresent());

        String version = KiteRuntimeInfo.getInstance().getVersion();
        String body = requestInfo.get().getBody();
        Assert.assertTrue("expecting plugin version in POST body send to kited. Body:" + body + ", version: " + version, body.contains(version));
        Assert.assertTrue("expecting editor version in POST body send to kited. Body: " + body, body.contains(ApplicationInfo.getInstance().getBuild().asString()));
    }

    @Test
    public void testFocusEventUnsupportedFile() throws Exception {
        MockKiteApiService api = MockKiteApiService.getInstance();
        Assert.assertTrue("No api calls expected before the edit events", api.getCallHistory().isEmpty());

        //opens the file and set the caret position
        KiteTestUtils.configureByFileAndFocus("plainText.txt", myFixture);

        TestcaseEditorEventListener.sleepForQueueWork(getProject());
        Assert.assertEquals("Expected no event: " + api.getCallHistory(), 0, api.getCallHistoryWithoutCountersAndStatus().size());
    }

    @Test
    public void testFrameActivationUnsupportedFile() throws Exception {
        MockKiteApiService api = MockKiteApiService.getInstance();
        Assert.assertTrue("No api calls expected before the edit events", api.getCallHistory().isEmpty());

        //opens the file and set the caret position
        KiteTestUtils.configureByFileAndFocus("plainText.txt", myFixture);
        KiteTestUtils.emulatedFrameActivation(getProject());

        TestcaseEditorEventListener.sleepForQueueWork(getProject());
        Assert.assertEquals("Expected two event: " + api.getCallHistory(), 0, api.getCallHistoryWithoutCountersAndStatus().size());
    }

    @Test
    public void testEditEvent() throws Exception {
        MockKiteApiService api = MockKiteApiService.getInstance();
        Assert.assertTrue("No api calls expected before the edit events", api.getCallHistory().isEmpty());

        //opens the file and set the caret position
        KiteTestUtils.configureByFileAndFocus("test.py", myFixture);

        //in test case the focus event comes last (a limitation of IntelliJ which disables focus event emitting in test mode)
        TestcaseEditorEventListener.sleepForQueueWork(getProject());
        Assert.assertEquals("Expected two events (focus + selection): " + api.getCallHistoryWithoutCountersAndStatus(), 2, api.getCallHistoryWithoutCountersAndStatus().size());
        Assert.assertEquals("Expected 2nd event to be focus", "sendEvent(focus, /src/test.py, print;\n, [7,7])", api.getCallHistoryWithoutCountersAndStatus().get(0));
        Assert.assertEquals("Expected 1st event to be selection", "sendEvent(selection, /src/test.py, print;\n, [7,7])", api.getCallHistoryWithoutCountersAndStatus().get(1));

        api.clearTestData();
        myFixture.type("m");

        //only the last character of 'myFunction()' should trigger an edit event
        //fast typing must not trigger an event for each character
        TestcaseEditorEventListener.sleepForQueueWork(getProject());
        Assert.assertEquals("Expected just one event: " + api.getCallHistory().toString(), 1, api.getCallHistoryWithoutCountersAndStatus().size());
        Assert.assertEquals("Expected selection event", "sendEvent(selection, /src/test.py, print;\nm, [8,8])", api.getCallHistoryWithoutCountersAndStatus().get(0));
    }

    @Test
    public void testUnsupportedFiles() throws Exception {
        MockKiteApiService api = MockKiteApiService.getInstance();
        Assert.assertTrue("No api calls expected before the edit events", api.getCallHistory().isEmpty());

        //open the file and set the caret position
        KiteTestUtils.configureByFileAndFocus("plainText.txt", myFixture);

        TestcaseEditorEventListener.sleepForQueueWork(getProject());
        Assert.assertEquals("Expected no events for plain text files", 0, api.getCallHistoryWithoutCountersAndStatus().size());

        //edit operations
        myFixture.type("my()");
        TestcaseEditorEventListener.sleepForQueueWork(getProject());
        Assert.assertTrue("Expected no events in plain text files for content changes because languages of unsupported files are not tracked anymore", api.getCallHistoryWithoutCountersAndStatus().isEmpty());

        //caret movements
        myFixture.getEditor().getCaretModel().moveToOffset(10);
        TestcaseEditorEventListener.sleepForQueueWork(getProject());
        Assert.assertTrue("Expected no caret events in plain text files for cursor changes", api.getCallHistoryWithoutCountersAndStatus().isEmpty());

        Assert.assertFalse("Expected no edit events for edits in unsupported files, this is enough to track the language",
                api.getCallHistory().stream().anyMatch(event -> event.contains("edit")));
    }

    @Test
    public void testUnsupportedFilesWhitelisting() throws Exception {
        MockKiteApiService api = MockKiteApiService.getInstance();
        Assert.assertTrue("No api calls expected before the edit events", api.getCallHistory().isEmpty());

        api.enableHttpCalls();
        MockKiteHttpConnection.getInstance().addPostPathHandler("/clientapi/editor/event", (path, payload) -> {
            throw new HttpStatusException("not whitelisted", 403, "");
        }, getTestRootDisposable());

        AtomicInteger notifications = new AtomicInteger(0);
        api.addHttpRequestStatusListener((statusCode, e, path, requestPath) -> {
            notifications.incrementAndGet();
            return false;
        }, getTestRootDisposable());

        //expire the welcome notification
        NotificationsManager notificationsManager = NotificationsManager.getNotificationsManager();
        for (Notification notification : notificationsManager.getNotificationsOfType(Notification.class, getProject())) {
            notificationsManager.expire(notification);
        }

        //open the file and set the caret position
        KiteTestUtils.configureByFileAndFocus("plainText.txt", myFixture);

        TestcaseEditorEventListener.sleepForQueueWork(getProject());
        Assert.assertEquals("Expected no focus event for plain text files: " + api.getCallHistory(), 0, api.getCallHistoryWithoutCountersAndStatus().size());

        //edit operations
        myFixture.type("my()");
        TestcaseEditorEventListener.sleepForQueueWork(getProject());
        Assert.assertTrue("Expected no events in plain text files for content changes because languages of unsupported files are tracked", api.getCallHistoryWithoutCountersAndStatus().isEmpty());

        Assert.assertEquals("No notifications must be shown for unsupported files", 0, notificationsManager.getNotificationsOfType(Notification.class, getProject()).length);
        Assert.assertEquals("No notification listener calls expected", 0, notifications.get());
    }

    @Test
    public void testOnFrameActivation() throws Exception {
        ProjectEditorEventListener eventListener = (ProjectEditorEventListener) EditorEventListener.getInstance(getProject());

        MockKiteApiService api = MockKiteApiService.getInstance();
        Assert.assertTrue("No api calls expected before the edit events", api.getCallHistory().isEmpty());

        KiteTestUtils.configureByFileAndFocus("empty.py", myFixture);
        Editor editor = myFixture.getEditor();
        Assert.assertNotNull(editor);

        TestcaseEditorEventListener.sleepForQueueWork(getProject());
        Assert.assertEquals("Focus event expected for a newly opened file", 1, api.getCallHistoryWithoutCountersAndStatus().size());
        Assert.assertEquals("Focus event expected for a newly opened file", "sendEvent(focus, /src/empty.py, , [0,0])", api.getCallHistoryWithoutCountersAndStatus().get(0));
        api.clearTestData();

        eventListener.onFrameActivated();

        TestcaseEditorEventListener.sleepForQueueWork(getProject());
        Assert.assertEquals("Focus event expected on frame activation", 1, api.getCallHistoryWithoutCountersAndStatus().size());
        Assert.assertEquals("Focus event expected for a newly activated file", "sendEvent(focus, /src/empty.py, , [0,0])", api.getCallHistoryWithoutCountersAndStatus().get(0));
    }

    @Test
    public void testMaximumFileSize() throws Exception {
        MockKiteApiService api = MockKiteApiService.getInstance();

        KiteTestUtils.configureByFileAndFocus("fileSizeExceeded.py", myFixture);

        TestcaseEditorEventListener.sleepForQueueWork(getProject());
        Assert.assertEquals("Focus event expected on frame activation", 1, api.getCallHistoryWithoutCountersAndStatus().size());
        Assert.assertEquals("Focus event expected for a newly activated file", "sendEvent(skip, /src/fileSizeExceeded.py, , [0,0])", api.getCallHistoryWithoutCountersAndStatus().get(0));

        myFixture.type("print\n");
        TestcaseEditorEventListener.sleepForQueueWork(getProject());
        Assert.assertTrue("Focus event expected on frame activation", api.getCallHistory().size() >= 2);
        Assert.assertEquals("At least one selection event expected for caret change", "sendEvent(skip, /src/fileSizeExceeded.py, , [0,0])", api.getCallHistoryWithoutCountersAndStatus().get(1));
    }

    /**
     * This test makes sure that a flush of the pending editor events returns after all requests have been processed.
     * This has to be possible for calls from the Swing EDT and other threads.
     */
    @Test
    public void testAwaitEventsInSwingThread() throws Exception {
        ApplicationManager.getApplication().assertIsDispatchThread();

        MockKiteApiService api = MockKiteApiService.getInstance();

        AtomicBoolean awaitWasCalled = new AtomicBoolean(false);
        AtomicBoolean inAwaitCall = new AtomicBoolean(false);
        List<String> performedRequests = new CopyOnWriteArrayList<>();

        ProjectEditorEventListener listener = new ProjectEditorEventListener(getProject()) {
            @Override
            public void awaitEvents() {
                awaitWasCalled.set(true);

                inAwaitCall.set(true);
                try {
                    super.awaitEvents();
                } finally {
                    inAwaitCall.set(false);
                }
            }

            @Override
            protected <T> void addOverridingRequest(Map<T, Runnable> requestMap, T key, Alarm alarm, Runnable newRequest) {
                super.addOverridingRequest(requestMap, key, alarm, () -> {
                    //requests must be run in the EDT because the events access the IntelliJ editor and document
                    ApplicationManager.getApplication().assertIsDispatchThread();

                    if (awaitWasCalled.get() && !inAwaitCall.get()) {
                        throw new IllegalStateException("request processed by awaitEvents wasn't executed while awaitEvents() was active");
                    }

                    newRequest.run();
                });
            }
        };

        listener.addOverridingRequest(listener.pendingEditorChangeRequests, new MockDocument(), listener.editAlarm, () -> {
            performedRequests.add("a");
        });

        listener.addOverridingRequest(listener.pendingEditorChangeRequests, new MockDocument(), listener.editAlarm, () -> {
            performedRequests.add("b");
        });

        listener.addOverridingRequest(listener.pendingEditorChangeRequests, new MockDocument(), listener.editAlarm, () -> {
            performedRequests.add("c");
        });

        //this must flush the events synchronously if called on the EDT
        listener.awaitEvents();

        Assert.assertEquals(3, performedRequests.size());
    }

    @Override
    protected String getBasePath() {
        return "python/editor/events";
    }

}