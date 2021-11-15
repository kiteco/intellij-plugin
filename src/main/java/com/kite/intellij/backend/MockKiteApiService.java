package com.kite.intellij.backend;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.impl.DebugUtil;
import com.kite.intellij.backend.http.HttpConnectionUnavailableException;
import com.kite.intellij.backend.http.HttpRequestFailedException;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.HttpTimeoutConfig;
import com.kite.intellij.backend.http.KiteHttpConnection;
import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.backend.model.Call;
import com.kite.intellij.backend.model.Calls;
import com.kite.intellij.backend.model.EventType;
import com.kite.intellij.backend.model.Id;
import com.kite.intellij.backend.model.Kind;
import com.kite.intellij.backend.model.KiteCompletion;
import com.kite.intellij.backend.model.KiteFileStatus;
import com.kite.intellij.backend.model.KiteFileStatusResponse;
import com.kite.intellij.backend.model.LicenseInfo;
import com.kite.intellij.backend.model.Report;
import com.kite.intellij.backend.model.SymbolExt;
import com.kite.intellij.backend.model.TextSelection;
import com.kite.intellij.backend.model.UserInfo;
import com.kite.intellij.backend.model.ValueExt;
import com.kite.intellij.backend.response.HoverResponse;
import com.kite.intellij.backend.response.KiteCompletions;
import com.kite.intellij.backend.response.MembersResponse;
import com.kite.intellij.backend.response.SymbolReportResponse;
import com.kite.intellij.backend.response.ValueReportResponse;
import com.kite.intellij.lang.KiteLanguage;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mock api implementation which tracks the method calls to use in the assertions later on.
 */
@TestOnly
public class MockKiteApiService extends DefaultKiteApiService {
    private static final Logger LOG = Logger.getInstance("#kite.test");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
    private final List<CallData> calls = Lists.newCopyOnWriteArrayList();
    private final Set<String> processedEventFiles = Sets.newConcurrentHashSet();
    private volatile boolean callHttp = false;
    private volatile boolean httpStatusListeners = false;//disabled by default in unit tests
    private volatile boolean online = true;

    public MockKiteApiService() {
    }

    private MockKiteApiService(KiteHttpConnection httpConnection) {
        this.httpConnectionOverride = httpConnection;
    }

    public static MockKiteApiService create(KiteHttpConnection connection) {
        return new MockKiteApiService(connection);
    }

    public static MockKiteApiService getInstance() {
        return (MockKiteApiService) KiteApiService.getInstance();
    }

    public synchronized List<String> getCallHistory() {
        return calls.stream().map(e -> e.message).collect(Collectors.toList());
    }

    /**
     * @param excludeSubstring Items will be excluded if they match this
     * @return All call history items which do not contain substring excludeSubstring
     */
    public synchronized List<String> getCallHistoryWithout(String... excludeSubstring) {
        return calls.stream().map(e -> e.message)
                .filter(message -> Arrays.stream(excludeSubstring).noneMatch(message::contains))
                .collect(Collectors.toList());
    }

    public synchronized List<CallData> getCallDataHistory() {
        return calls;
    }

    public synchronized Set<String> getProcessedEventFiles() {
        return processedEventFiles;
    }

    public synchronized List<String> getCounterEventHistory() {
        return getCallHistory().stream().filter(l -> l.startsWith("incrementCounter(")).collect(Collectors.toList());
    }

    public synchronized List<String> getCounterEventHistoryWithoutFocus() {
        return getCounterEventHistory().stream().filter(l -> !l.contains("_focus_")).collect(Collectors.toList());
    }

    /**
     * @return The call history without calls to file status
     */
    public synchronized List<String> getCallHistoryWithoutCountersAndStatus(String... exclude) {
        List<String> allExcluded = Lists.newArrayList("fileStatus(");
        allExcluded.addAll(Lists.newArrayList(exclude));
        return getCallHistoryWithout(allExcluded.toArray(new String[0]));
    }

    public synchronized void clearTestData() {
        calls.clear();
        processedEventFiles.clear();
        online = true;
    }

    public void enableHttpCalls() {
        this.callHttp = true;
    }

    public void disableHttpCalls() {
        this.callHttp = false;
    }

    public void turnOffline() {
        boolean wasOnline = this.online;
        this.online = false;

        if (wasOnline) {
            notifyConnectionStatusListeners(false, null);
        }
    }

    public void turnOnline() {
        boolean wasOnline = this.online;
        this.online = true;

        if (!wasOnline) {
            notifyConnectionStatusListeners(true, null);
        }
    }

    public void enableHttpStatusListeners() {
        this.httpStatusListeners = true;
    }

    public void disableHttpStatusListeners() {
        this.httpStatusListeners = false;
    }

    @Override
    public boolean checkOnlineStatus() {
        return online && (!callHttp || super.checkOnlineStatus());
    }

    @Nullable
    @Override
    public KiteCompletions completions(CanonicalFilePath filePath, String fileContent, @NotNull Integer cursorOffset, @Nullable Integer cursorEndOffset, boolean disableSnippets, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        recordCall(String.format("completions(%s, %d chars, %d)", filePath.asSlashDelimitedPath(),
                fileContent.length(), cursorOffset), fileContent, cursorOffset, filePath);

        return callHttp
                ? super.completions(filePath, fileContent, cursorOffset, cursorEndOffset, disableSnippets, timeout)
                : KiteCompletions.EMPTY;
    }

    @Nullable
    @Override
    public void completionSelected(CanonicalFilePath filePath, KiteCompletion completion, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        recordCall(String.format("completionSelected(%s, %s)", filePath.asSlashDelimitedPath(),
                completion.getInsert()));

        if (callHttp) {
            super.completionSelected(filePath, completion, timeout);
        }
    }

    @Override
    public HoverResponse hover(CanonicalFilePath filePath, String fileContent, int offset, HttpTimeoutConfig timeout) throws KiteHttpException {
        recordCall(String.format("hover(%s, %d chars, %d)", filePath.asSlashDelimitedPath(), fileContent.length(), offset), fileContent, offset, filePath);

        return callHttp ? super.hover(filePath, fileContent, offset, timeout) : new HoverResponse("dummy", (SymbolExt[]) null, null);
    }

    @Nullable
    @Override
    public ValueReportResponse valueReport(@Nonnull Id id, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        recordCall(String.format("valueReport(%s)", id.getValue()));

        return callHttp
                ? super.valueReport(id, timeout)
                : new ValueReportResponse(new ValueExt(Id.of("dummy id"), Kind.Module, "", "module", "module", null, null, null, null), new Report(null, "text", "text", null, null, 0));
    }

    @Nullable
    @Override
    public SymbolReportResponse symbolReport(@Nonnull Id id, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        recordCall(String.format("symbolReport(%s)", id.getValue()));

        return callHttp
                ? super.symbolReport(id, timeout)
                : new SymbolReportResponse(new SymbolExt(Id.of("dummy id"), "name", "qname", null, null, ""), new Report(null, "text", "text", null, null, 0));
    }

    @Nullable
    @Override
    public MembersResponse members(@Nonnull Id id, int offset, int limit, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        recordCall(String.format("members(%s, %d @ %s)", id.getValue(), limit, offset), "", offset, null);

        return callHttp ? super.members(id, offset, limit, timeout) : new MembersResponse(0, 0, 0, null);
    }

    @Nullable
    @Override
    public Calls signatures(@Nonnull CanonicalFilePath canonicalFilePath, String fileContent, int offset, HttpTimeoutConfig timeout) throws KiteHttpException {
        recordCall(String.format("signatures(%s, %d chars, %d)", canonicalFilePath.asSlashDelimitedPath(), fileContent.length(), offset), fileContent, offset, canonicalFilePath);

        return callHttp ? super.signatures(canonicalFilePath, fileContent, offset, timeout) : new Calls(Call.EMPTY_ARRAY);
    }

    @Nonnull
    @Override
    public KiteFileStatusResponse fileStatus(@Nonnull CanonicalFilePath canonicalFilePath, HttpTimeoutConfig timeout) {
        recordCall(String.format("fileStatus(%s)", canonicalFilePath.asKiteEncodedPath()));
        return callHttp ? super.fileStatus(canonicalFilePath, timeout) : new KiteFileStatusResponse(KiteFileStatus.Ready);
    }

    @Nullable
    @Override
    public UserInfo userInfo(HttpTimeoutConfig timeout) {
        recordCall("userInfo()");

        return callHttp ? super.userInfo(timeout) : new UserInfo("42", "Dummy User", "mail@example.com", null, true, false, false);
    }

    @Override
    public boolean sendEvent(EventType eventType, CanonicalFilePath filePath, String fileContent, List<TextSelection> selections, boolean statusUpdateNotificationsEnabled, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        recordCall(String.format("sendEvent(%s, %s, %s, %s)", eventType.asKiteId(), filePath.asSlashDelimitedPath(), fileContent, selections.stream().map(Object::toString).collect(Collectors.joining(","))), fileContent, selections.get(0).getStartOffset(), filePath);

        processedEventFiles.add(filePath.asSlashDelimitedPath());

        return callHttp && super.sendEvent(eventType, filePath, fileContent, selections, statusUpdateNotificationsEnabled, timeout);
    }

    @Override
    public void openKiteCopilot() throws HttpConnectionUnavailableException, HttpStatusException, HttpRequestFailedException {
        recordCall("openKiteCopilot()");

        if (callHttp) {
            super.openKiteCopilot();
        }
    }

    @Override
    public LicenseInfo licenseInfo() {
        recordCall("licenseInfo()");

        // fixme
        return callHttp ? super.licenseInfo() : null;
    }

    @Override
    protected void notifyHttpStatusListeners(int status, @Nullable HttpStatusException exception, CanonicalFilePath filePath, String requestPath) {
        if (httpStatusListeners) {
            super.notifyHttpStatusListeners(status, exception, filePath, requestPath);
        }
    }

    @Override
    protected void notifyConnectionStatusListeners(boolean connectionAvailable, @org.jetbrains.annotations.Nullable Exception error) {
        // sync invocation to simplify testing
        connectionStatusListeners.forEach(listener -> listener.connectionStatusChanged(connectionAvailable, error));
    }

    @Override
    public Set<KiteLanguage> languages() throws HttpConnectionUnavailableException, HttpRequestFailedException {
        recordCall("languages()");

        return callHttp ? super.languages() : Sets.immutableEnumSet(KiteLanguage.Python);
    }

    private void recordCall(String message) {
        calls.add(new CallData(message, null));
    }

    private void recordCall(String message, String content, int contentOffset, CanonicalFilePath filePath) {
        CallData newCall = new CallData(message, content, contentOffset, filePath);

        if (!calls.isEmpty()) {
            CallData prev = calls.get(calls.size() - 1);
            if (filePath != null && filePath.equals(prev.filePath) && contentOffset < prev.offset && !newCall.message.startsWith("signature") && !prev.message.startsWith("signature")) {
                LOG.warn(String.format("Invalid order of events detected: prev: %s, next: %s", prev, newCall));
            }
        }

        calls.add(newCall);
    }

    public static class CallData {
        public int offset;
        public String contentWithOffset;
        public String message;
        public String content;
        public String stacktrace;
        public long timestamp;
        public CanonicalFilePath filePath;

        private CallData(String message, CanonicalFilePath filePath) {
            this(message, null, -1, filePath);
        }

        public CallData(String message, String content, int contentOffset, CanonicalFilePath filePath) {
            if (content != null && contentOffset >= 0 && contentOffset > content.length()) {
                throw new IllegalStateException(String.format("contentOffset %d is invalid for content length %s", contentOffset, content.length()));
            }

            this.message = message;
            this.content = content;
            this.contentWithOffset = content == null || contentOffset < 0 ? content : content.substring(0, contentOffset) + "|" + content.substring(contentOffset);
            this.timestamp = System.currentTimeMillis();
            this.offset = contentOffset;
            this.filePath = filePath;
            this.stacktrace = DebugUtil.currentStackTrace();
        }

        @Override
        public int hashCode() {
            int result = offset;
            result = 31 * result + message.hashCode();
            result = 31 * result + content.hashCode();
            result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CallData callData = (CallData) o;

            if (offset != callData.offset) {
                return false;
            }
            if (timestamp != callData.timestamp) {
                return false;
            }
            if (!message.equals(callData.message)) {
                return false;
            }
            return content.equals(callData.content);
        }

        @Override
        public String toString() {
            return "CallData{" +
                    "offset='" + offset + '\'' +
                    ", message='" + message + '\'' +
                    ", timestamp=" + DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)) +
                    '}';
        }
    }
}
