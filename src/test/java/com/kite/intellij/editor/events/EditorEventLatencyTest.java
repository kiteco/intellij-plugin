package com.kite.intellij.editor.events;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.CodeInsightSettings;
import com.jetbrains.python.PythonFileType;
import com.kite.intellij.backend.MockKiteApiService;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.test.KiteLightFixtureTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class EditorEventLatencyTest extends KiteLightFixtureTest {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");

    @Test
    public void testEditorLatencyValidCodeWithAutoPopup() throws Exception {
        CodeInsightSettings settings = CodeInsightSettings.getInstance();
        try {
            settings.AUTO_POPUP_PARAMETER_INFO = true;

            doEditorLatencyTest(50, 500, 300);
        } finally {
            settings.AUTO_POPUP_PARAMETER_INFO = true;
        }
    }

    @Test
    public void testEditorLatencyValidCodeNoAutoPopup() throws Exception {
        CodeInsightSettings settings = CodeInsightSettings.getInstance();
        try {
            settings.AUTO_POPUP_PARAMETER_INFO = false;

            doEditorLatencyTest(50, 500, 200);
        } finally {
            settings.AUTO_POPUP_PARAMETER_INFO = true;
        }
    }

    @Test
    public void testSlowTypingEditorLatencyValidCodeNoAutoPopup() throws Exception {
        CodeInsightSettings settings = CodeInsightSettings.getInstance();
        try {
            settings.AUTO_POPUP_PARAMETER_INFO = false;

            doEditorLatencyTest(200, 500, 200);
        } finally {
            settings.AUTO_POPUP_PARAMETER_INFO = true;
        }
    }

    //@Test
    @Ignore("Disabled to speed up Travis")
    public void _testVerySlowTypingEditorLatencyValidCodeNoAutoPopup() throws Exception {
        CodeInsightSettings settings = CodeInsightSettings.getInstance();
        try {
            settings.AUTO_POPUP_PARAMETER_INFO = false;

            doEditorLatencyTest(1000, 1000, 200);
        } finally {
            settings.AUTO_POPUP_PARAMETER_INFO = true;
        }
    }

    private void doEditorLatencyTest(int typingDelay, int httpRequestDelay, int assertedMaxDelay) throws InterruptedException {
        MockKiteHttpConnection http = MockKiteHttpConnection.getInstance();
        MockKiteApiService api = getKiteApiService();
        api.enableHttpCalls();

        //delay the response to the events
        http.addPostPathHandler("/clientapi/editor/event", (path, payload) -> {
            try {
                Thread.sleep(httpRequestDelay);
            } catch (InterruptedException e) {
                //ignored
            }
            return "\"ok\"";
        }, getTestRootDisposable());

        //warmup
        myFixture.configureByText("testwarmup.py", "");
        typeWithDelay("def myFunc():\n", 10, Lists.newLinkedList());
        TestcaseEditorEventListener.sleepForQueueWork(getProject());
        api.clearTestData();
        http.reset();

        //our real test
        myFixture.configureByText(PythonFileType.INSTANCE, "");

        List<DelayTimestamp> delays = Lists.newArrayList();
        typeWithDelay("def myFunc():\nprint(\"hello world\");\n\nmy|Func();\n123.|abs();", typingDelay, delays);

        //make sure that the editor latency does not contain unusally large delays
        Assert.assertTrue("Typing latency must be < 200ms (2 exceptions permitted): " + delays.stream().map(e -> escapeNewline(e.toString())).collect(Collectors.joining("\n")),
                delays.stream().filter(e -> e.duration() >= assertedMaxDelay).count() <= 2);

        //signature requests may be out-of-order because they are invoked with a delay triggered while the user types
        List<MockKiteApiService.CallData> nonSignatureHistory = api.getCallDataHistory().stream().filter(e -> !e.message.startsWith("signature") && !e.message.startsWith("incrementCounter")).collect(Collectors.toList());
        //make sure that no event has an offset larger than the following
        for (int i = 0; i < nonSignatureHistory.size(); i++) {
            MockKiteApiService.CallData call = nonSignatureHistory.get(i);
            if (nonSignatureHistory.size() > i + 1) {
                MockKiteApiService.CallData next = nonSignatureHistory.get(i + 1);

                if (call.offset > next.offset) {
                    Assert.fail(String.format("A call must not have an offset larger than the next.\n\tCurrent: %s,\n\tnext: %s,\n\tindex: %s", escapeNewline(call.toString()), escapeNewline(next.toString()), i));
                }
            }
        }

    }

    @Nullable
    private String escapeNewline(String data) {
        return StringUtils.replace(data, "\n", "\\n");
    }

    /**
     * Type text into the editor. A | character indicates that code completion must be called at the given offset in the editor.
     *
     * @param typedText
     * @param delayMillis
     * @param typingDelay
     * @throws InterruptedException
     */
    private void typeWithDelay(String typedText, int delayMillis, List<DelayTimestamp> typingDelay) throws InterruptedException {
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < typedText.length(); i++) {
            char c = typedText.charAt(i);
            if (c == '|') {
                myFixture.completeBasic();
                continue;
            }

            current.append(c);

            long start = System.currentTimeMillis();
            myFixture.type(c);
            typingDelay.add(new DelayTimestamp(start, System.currentTimeMillis(), current.toString()));

            Thread.sleep(delayMillis);
        }
    }

    private class DelayTimestamp {
        long start;
        long end;
        String text;

        private DelayTimestamp(long start, long end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }

        @Override
        public String toString() {
            return "DelayTimestamp{" +
                    "duration='" + duration() + "', " +
                    "start='" + DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneOffset.UTC)) + "', " +
                    "end='" + DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(end), ZoneOffset.UTC)) + "', " +
                    "text='" + text + "'" +
                    '}';
        }

        long duration() {
            return end - start;
        }
    }
}
