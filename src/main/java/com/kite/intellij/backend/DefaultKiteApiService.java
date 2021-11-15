package com.kite.intellij.backend;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.kite.intellij.KiteConstants;
import com.kite.intellij.KiteRuntimeInfo;
import com.kite.intellij.backend.http.HttpConnectionUnavailableException;
import com.kite.intellij.backend.http.HttpRequestFailedException;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.HttpTimeoutConfig;
import com.kite.intellij.backend.http.HttpUnauthorizedException;
import com.kite.intellij.backend.http.KiteHttpConnection;
import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.backend.json.KiteJsonParsing;
import com.kite.intellij.backend.model.Calls;
import com.kite.intellij.backend.model.EventType;
import com.kite.intellij.backend.model.Id;
import com.kite.intellij.backend.model.KiteCompletion;
import com.kite.intellij.backend.model.KiteFileStatus;
import com.kite.intellij.backend.model.KiteFileStatusResponse;
import com.kite.intellij.backend.model.LicenseInfo;
import com.kite.intellij.backend.model.TextSelection;
import com.kite.intellij.backend.model.UserInfo;
import com.kite.intellij.backend.response.CodeFinderResponse;
import com.kite.intellij.backend.response.HoverResponse;
import com.kite.intellij.backend.response.KiteCompletions;
import com.kite.intellij.backend.response.MembersResponse;
import com.kite.intellij.backend.response.SymbolReportResponse;
import com.kite.intellij.backend.response.ValueReportResponse;
import com.kite.intellij.codeFinder.KiteFindRelatedError;
import com.kite.intellij.lang.KiteLanguage;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.intellij.openapi.application.PathManager.getHomePath;
import static com.kite.intellij.KiteConstants.APPLICATION_ID;

/**
 * The online service implementation which communicates with the real-live kite backend.
 * The class {@link MockKiteApiService} is used in test cases (see plugin.xml for the configuration).
 */
public class DefaultKiteApiService implements KiteApiService, Disposable {
    private static final Logger LOG = Logger.getInstance("#kite.api");
    protected final List<HttpStatusListener> httpStatusListeners = Lists.newCopyOnWriteArrayList();
    protected final List<ConnectionStatusListener> connectionStatusListeners = Lists.newCopyOnWriteArrayList();
    private final KiteJsonParsing jsonParsing = new KiteJsonParsing();
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private final String pluginVersion = KiteRuntimeInfo.getInstance().getVersion();
    private final String editorVersion = ApplicationInfo.getInstance().getBuild().asString();
    protected KiteHttpConnection httpConnectionOverride;
    private volatile boolean connectionAvailable = true;
    private volatile boolean disposed;

    public DefaultKiteApiService() {
    }

    // avoid warning on constructor injection on 2020.1+
    public static DefaultKiteApiService create(KiteHttpConnection connection) {
        DefaultKiteApiService api = new DefaultKiteApiService();
        api.httpConnectionOverride = connection;
        return api;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;

            resetStatusListener();
        }
    }

    public boolean isConnectionAvailable() {
        return connectionAvailable;
    }

    @Override
    public boolean checkOnlineStatus() {
        try {
            doGet("/clientapi/ping", Collections.emptyMap(), HttpTimeoutConfig.MinimalTimeout);
            return true;
        } catch (HttpRequestFailedException | HttpStatusException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error while checking online status using '/clientapi/ping': ", e);
            }

            //timeout indicates offline status, easily happens on Windows for unbound ports
            return !(e.getCause() instanceof TimeoutException);
        } catch (HttpConnectionUnavailableException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Offline status detected: ", e);
            }

            return false;
        }
    }

    @Override
    public void addHttpRequestStatusListener(HttpStatusListener listener, @Nullable Disposable parent) {
        synchronized (this) {
            httpStatusListeners.add(listener);

            if (parent != null) {
                Disposer.register(parent, () -> removeHttpRequestStatusListener(listener));
            }
        }
    }

    @Override
    public void removeHttpRequestStatusListener(HttpStatusListener listener) {
        synchronized (this) {
            httpStatusListeners.remove(listener);
        }
    }

    @Override
    public void addConnectionStatusListener(ConnectionStatusListener listener, @Nullable Disposable parent) {
        synchronized (this) {
            connectionStatusListeners.add(listener);

            if (parent != null) {
                Disposer.register(parent, () -> {
                    removeConnectionStatusListener(listener);
                });
            }
        }
    }

    @Override
    public void removeConnectionStatusListener(ConnectionStatusListener listener) {
        synchronized (this) {
            connectionStatusListeners.remove(listener);
        }
    }

    @Override
    public void resetStatusListener() {
        synchronized (this) {
            httpStatusListeners.clear();
            connectionStatusListeners.clear();
        }
    }

    @Nullable
    @Override
    public KiteCompletions completions(CanonicalFilePath filePath, String fileContent, @NotNull Integer cursorOffset, @Nullable Integer cursorEndOffset, boolean disableSnippets, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        JsonObject pos = new JsonObject();
        pos.addProperty("begin", cursorOffset);
        pos.addProperty("end", cursorEndOffset != null ? cursorEndOffset : cursorOffset);

        JsonObject request = new JsonObject();
        request.addProperty("editor", KiteConstants.APPLICATION_ID);
        request.addProperty("filename", filePath.asOSDelimitedPath());
        request.addProperty("text", fileContent);
        if (disableSnippets) {
            request.addProperty("no_snippets", disableSnippets);
        }
        // java is using unicode code units, i.e. 16 bit, and not code points (32 bit)
        request.addProperty("offset_encoding", "utf-16");
        request.add("position", pos);

        String payload = gson.toJson(request);

        String requestPath = "/clientapi/editor/complete";
        try {
            String json = doPost(requestPath, payload, timeout);
            KiteCompletions response = jsonParsing.parseCompletionResponse2(json);
            notifyHttpStatusListenersSuccess(filePath);

            return response;
        } catch (HttpStatusException e) {
            notifyHttpStatusListeners(e, filePath, requestPath);

            //a 404 means that there are no completions available, in that case we return an empty set of completions
            if (e.isNotFoundError404()) {
                return KiteCompletions.EMPTY;
            }

            if (e.isServiceUnavailable503()) {
                // returning null keeps the IDE's own completions enabled
                return null;
            }

            // if the language isn't enabled, then don't display this as an error
            if ("language not supported".equals(e.getBody())) {
                return KiteCompletions.EMPTY;
            }

            return null;
        }
    }

    @Override
    public void completionSelected(CanonicalFilePath filePath, KiteCompletion completion, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException {
    }

    @Override
    public void relatedCode(CanonicalFilePath canonical, @Nullable Integer zeroBasedLineNo, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException, KiteFindRelatedError {
        JsonObject location = new JsonObject();
        location.addProperty("filename", canonical.asOSDelimitedPath());
        if (zeroBasedLineNo != null) {
            location.addProperty("line", zeroBasedLineNo + 1);
        }

        JsonObject request = new JsonObject();
        // Editor here must distinguish IntelliJ, GoLand, WebStorm etc.
        request.addProperty(
                "editor",
                getEditorFromProductCode(ApplicationInfo.getInstance().getBuild().getProductCode())
        );
        request.addProperty("editor_install_path", getHomePath());
        request.add("location", location);

        String requestPath = "/codenav/editor/related";
        String payload = gson.toJson(request);

        try {
            doPost(requestPath, payload, timeout);
            notifyHttpStatusListenersSuccess(canonical);
        } catch (HttpStatusException e) {
            notifyHttpStatusListeners(e, canonical, requestPath);

            try {
                CodeFinderResponse resp = jsonParsing.parseCodeFinderResponse(e.getBody().trim());

                // don't show the code finder warning if the 503 already triggers a paywall notification
                // the call to notifyHttpStatusListeners above takes care of the paywall notification
                if (e.isServiceUnavailable503() && "AllFeaturesProPaywallLocked".equals(resp.reason)) {
                    return;
                }

                if (resp.message != null && !resp.message.isEmpty()) {
                    throw new KiteFindRelatedError(resp.message);
                }
            } catch (JsonSyntaxException jse) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Error parsing code finder response.", jse);
                }
                // continue to raise generic error
            }
            throw new KiteFindRelatedError("Oops! Something went wrong with Code Finder. Please try again later.");
        }
    }

    @Override
    @Nullable
    public HoverResponse hover(CanonicalFilePath filePath, String fileContent, int offset, HttpTimeoutConfig timeout) throws KiteHttpException {
        Map<String, String> params = Maps.newHashMapWithExpectedSize(2);
        params.put("cursor_runes", String.valueOf(offset));

        String md5Hash = DigestUtils.md5Hex(fileContent.getBytes(StandardCharsets.UTF_8));
        String urlPath = String.format("/api/buffer/%s/%s/%s/hover", APPLICATION_ID, filePath.asKiteEncodedPath(), md5Hash);

        try {
            HoverResponse response = jsonParsing.parseHoverResponse(doGet(urlPath, params, timeout));

            notifyHttpStatusListenersSuccess(filePath);

            return response;
        } catch (HttpStatusException e) {
            notifyHttpStatusListeners(e, filePath, urlPath);

            if (e.isNotFoundError404()) {
                return null;
            }

            if (e.isUnauthorizedError401()) {
                throw new HttpUnauthorizedException(e);
            }

            throw new HttpStatusException(e);
        }
    }

    @Nullable
    @Override
    public ValueReportResponse valueReport(@Nonnull Id id, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        String requestPath = String.format("/api/editor/value/%s", id.getValue());
        try {
            ValueReportResponse response = jsonParsing.parseValueReportResponse(doGet(requestPath, Collections.emptyMap(), timeout));

            notifyHttpStatusListenersSuccess(null);

            return response;
        } catch (HttpStatusException e) {
            notifyHttpStatusListeners(e, null, requestPath);

            return null;
        }
    }

    @Nullable
    @Override
    public SymbolReportResponse symbolReport(@Nonnull Id id, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        String requestPath = String.format("/api/editor/symbol/%s", id.getValue());
        try {
            SymbolReportResponse response = jsonParsing.parseSymbolReportResponse(doGet(requestPath, Collections.emptyMap(), timeout));

            notifyHttpStatusListenersSuccess(null);

            return response;
        } catch (HttpStatusException e) {
            notifyHttpStatusListeners(e, null, requestPath);

            return null;
        }
    }

    @Nullable
    @Override
    public MembersResponse members(@Nonnull Id id, int offset, int limit, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        String requestPath = String.format("/api/editor/value/%s/members", id.getValue());
        try {
            Map<String, String> params = Maps.newLinkedHashMap();
            params.put("offset", String.valueOf(offset));
            params.put("limit", String.valueOf(limit));

            MembersResponse response = jsonParsing.parseMembersResponse(doGet(requestPath, params, timeout));

            notifyHttpStatusListenersSuccess(null);

            return response;
        } catch (HttpStatusException e) {
            notifyHttpStatusListeners(e, null, requestPath);

            return null;
        }
    }

    @Nullable
    @Override
    public Calls signatures(@Nonnull CanonicalFilePath canonicalFilePath, String fileContent, int offset, HttpTimeoutConfig timeout) throws KiteHttpException {
        String requestPath = "/clientapi/editor/signatures";
        try {
            Map<String, Object> request = Maps.newLinkedHashMap();
            request.put("editor", KiteConstants.APPLICATION_ID);
            request.put("filename", canonicalFilePath.asOSDelimitedPath());
            request.put("text", fileContent);
            request.put("cursor_runes", offset);
            request.put("offset_encoding", "utf-16");

            String payload = gson.toJson(request);

            Calls response = jsonParsing.parseCalls(doPost(requestPath, payload, timeout));

            notifyHttpStatusListenersSuccess(canonicalFilePath);

            return response;
        } catch (HttpStatusException e) {
            notifyHttpStatusListeners(e, canonicalFilePath, requestPath);

            if (e.isNotFoundError404()) {
                return null;
            }

            throw new HttpStatusException(e);
        }
    }

    @Nonnull
    @Override
    public KiteFileStatusResponse fileStatus(@Nonnull CanonicalFilePath file, HttpTimeoutConfig timeout) {
        String requestPath = "/clientapi/status";
        try {
            KiteFileStatusResponse response = jsonParsing.parseFileStatus(doGet(requestPath, Collections.singletonMap("filename", file.asOSDelimitedPath()), timeout));

            notifyHttpStatusListenersSuccess(file);

            return response;
        } catch (HttpStatusException e) {
            notifyHttpStatusListeners(e, null, requestPath);

            if (e.isUnauthorizedError401()) {
                return new KiteFileStatusResponse(KiteFileStatus.Unauthorized);
            }

            return new KiteFileStatusResponse(KiteFileStatus.Unknown);
        } catch (HttpRequestFailedException | HttpConnectionUnavailableException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error during file status request.", e);
            }
            return new KiteFileStatusResponse(KiteFileStatus.Error);
        }
    }

    @Nullable
    @Override
    public UserInfo userInfo(HttpTimeoutConfig timeout) {
        String requestPath = "/clientapi/user";
        try {
            UserInfo response = jsonParsing.parseUserInfo(doGet(requestPath, Collections.emptyMap(), timeout));

            notifyHttpStatusListenersSuccess(null);

            return response;
        } catch (HttpStatusException e) {
            notifyHttpStatusListeners(e, null, requestPath);
            return null;
        } catch (HttpRequestFailedException | HttpConnectionUnavailableException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error during user info request.", e);
            }
            return null;
        }
    }

    @Override
    public boolean sendEvent(EventType eventType, CanonicalFilePath cononicalFilePath, String fileContent, List<TextSelection> selections, boolean statusUpdateNotificationsEnabled, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        JsonArray selectionArray = new JsonArray();
        selections.forEach(selection -> {
            JsonObject range = new JsonObject();
            range.addProperty("start", selection.getStartOffset());
            range.addProperty("end", selection.getEndOffset());
            range.addProperty("encoding", "utf-16");
            selectionArray.add(range);
        });

        JsonObject request = new JsonObject();
        request.addProperty("source", APPLICATION_ID);
        request.addProperty("action", eventType.asKiteId());
        request.addProperty("filename", cononicalFilePath.asOSDelimitedPath());
        request.addProperty("text", fileContent);
        request.addProperty("editor_version", editorVersion);
        request.addProperty("plugin_version", pluginVersion);
        request.add("selections", selectionArray);

        String payload = gson.toJson(request);

        String requestPath = "/clientapi/editor/event";
        try {
            doPost(requestPath, payload, timeout);

            //in contrast to other request implementations sendEvent also notifies of HTTP success
            if (statusUpdateNotificationsEnabled) {
                notifyHttpStatusListeners(200, null, cononicalFilePath, requestPath);
            }

            return true;
        } catch (HttpStatusException e) {
            if (statusUpdateNotificationsEnabled) {
                notifyHttpStatusListeners(e, cononicalFilePath, requestPath);
            }

            return false;
        }
    }

    @Override
    public Set<KiteLanguage> languages(HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        try {
            KiteLanguage[] languages = jsonParsing.parseLanguages(doGet("/clientapi/languages", Collections.emptyMap(), timeout));
            return Arrays.stream(languages).filter(Objects::nonNull).collect(Collectors.toSet());
        } catch (HttpStatusException e) {
            return Collections.emptySet();
        }
    }

    @Override
    public void openKiteCopilot() throws HttpConnectionUnavailableException, HttpStatusException, HttpRequestFailedException {
        doGet("/clientapi/sidebar/open", Collections.emptyMap(), HttpTimeoutConfig.DefaultTimeout);
    }

    @Override
    public LicenseInfo licenseInfo() {
        String requestPath = "/clientapi/license-info";
        try {
            LicenseInfo response = jsonParsing.parseLicenseInfo(doGet(requestPath, Collections.singletonMap("refresh", "false"), HttpTimeoutConfig.DefaultTimeout));

            notifyHttpStatusListenersSuccess(null);

            return response;
        } catch (HttpStatusException e) {
            notifyHttpStatusListeners(e, null, requestPath);
            return null;
        } catch (HttpRequestFailedException | HttpConnectionUnavailableException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error during license-info request.", e);
            }
            return null;
        }
    }

    @Nullable
    @Override
    public CanonicalFilePath getOnboardingFilePath(@NotNull KiteLanguage language) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("editor", APPLICATION_ID);
            params.put("language", language.asKiteName());

            String body = doGet("/clientapi/plugins/onboarding_file", params, HttpTimeoutConfig.DefaultTimeout);
            String path = gson.fromJson(body, String.class);
            return CanonicalFilePathFactory.getInstance().forNativePath(path);
        } catch (KiteHttpException e) {
            LOG.debug("Error handling request /clientapi/plugins/onboarding_file", e);
            return null;
        }
    }

    @Nullable
    @Override
    public String getSetting(@Nonnull String name) {
        String path = String.format("/clientapi/settings/%s", name);
        try {
            return doGet(path, Collections.emptyMap(), HttpTimeoutConfig.MinimalTimeout);
        } catch (KiteHttpException e) {
            LOG.debug("Error handling request " + path, e);
            return null;
        }
    }

    @Override
    public void setSetting(@Nonnull String name, String value) throws KiteHttpException {
        String path = String.format("/clientapi/settings/%s", name);
        doPost(path, value, HttpTimeoutConfig.DefaultTimeout);
    }

    @NotNull
    @Override
    public KiteJsonParsing getJsonParsing() {
        return jsonParsing;
    }

    /**
     * Notify all registered listeners that a non-200 http response has ocurred.
     *
     * @param status      The status returned by the request
     * @param exception   The exception representing the unexpected http status code, {@code null} if the request finished successfully
     * @param filePath    The file processed in the http request, if available.
     * @param requestPath
     */
    protected void notifyHttpStatusListeners(int status, @Nullable HttpStatusException exception, CanonicalFilePath filePath, String requestPath) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("HTTP status change: " + status);
        }

        // Listeners must be invoked in another thread to avoid dead-locks.
        // The document listener acquires the IntelliJ write lock, processes a modification and then adds an event to the kite
        // event queue and waits to acquire the queue's lock.
        // If the event queue is processed at the same time and if listeners were notified within the calling thread and
        // if a listener wants to acquire IntelliJ's write lock then a dead lock will occur.
        // HTTP status notifiction must be run in another thread to avoid this potential dead-lock.
        Application application = ApplicationManager.getApplication();
        Future<?> future = application.executeOnPooledThread(() -> httpStatusListeners.forEach(listener -> listener.notify(status, exception, filePath, requestPath)));

        //wait in unit test mode
        if (application.isUnitTestMode()) {
            try {
                future.get(2, TimeUnit.SECONDS);
            } catch (Exception e) {
                //ignored
            }
        }
    }

    protected void notifyHttpStatusListenersSuccess(@Nullable CanonicalFilePath filePath) {
        notifyHttpStatusListeners(HttpStatus.SC_OK, null, filePath, null);
    }

    protected void notifyHttpStatusListeners(@Nonnull HttpStatusException exception, @Nullable CanonicalFilePath filePath, String requestPath) {
        notifyHttpStatusListeners(exception.getStatusCode(), exception, filePath, requestPath);
    }

    protected void notifyConnectionStatusListeners(boolean connectionAvailable, @Nullable Exception error) {
        ApplicationManager.getApplication().executeOnPooledThread(() ->
                connectionStatusListeners.forEach(listener -> listener.connectionStatusChanged(connectionAvailable, error))
        );
    }

    private KiteHttpConnection connection() {
        if (httpConnectionOverride != null) {
            return httpConnectionOverride;
        }
        return KiteHttpConnection.instance();
    }

    @Nonnull
    private String doGet(String urlPath, @Nonnull Map<String, String> params, HttpTimeoutConfig timeout) throws HttpRequestFailedException, HttpConnectionUnavailableException, HttpStatusException {
        try {
            String response = connection().doGet(urlPath, params, timeout);

            if (!connectionAvailable) {
                connectionAvailable = true;
                notifyConnectionStatusListeners(connectionAvailable, null);
            }

            return response;
        } catch (HttpConnectionUnavailableException e) {
            if (connectionAvailable) {
                connectionAvailable = false;
                notifyConnectionStatusListeners(false, e);
            }

            throw e;
        }
    }

    @Nonnull
    private String doPost(String path, String payload, HttpTimeoutConfig timeout) throws HttpRequestFailedException, HttpConnectionUnavailableException, HttpStatusException {
        try {
            String response = connection().doPost(path, payload, timeout);

            if (!connectionAvailable) {
                connectionAvailable = true;
                notifyConnectionStatusListeners(true, null);
            }

            return response;
        } catch (HttpConnectionUnavailableException e) {
            if (connectionAvailable) {
                connectionAvailable = false;
                notifyConnectionStatusListeners(false, e);
            }

            throw e;
        }
    }

    @Nonnull
    private String getEditorFromProductCode(String productCode) {
        // https://plugins.jetbrains.com/docs/marketplace/product-codes.html
        switch (productCode) {
            case "IU":
            case "IC":
            case "IE":
                return "intellij";
            case "PS":
                return "phpstorm";
            case "WS":
                return "webstorm";
            case "PY":
            case "PC":
            case "PE":
                return "pycharm";
            case "RM":
                return "rubymine";
            case "OC":
                return "appcode";
            case "CL":
                return "clion";
            case "GO":
                return "goland";
            case "RD":
                return "rider";
            case "AI":
                return "android-studio";
            case "DB":
                // Kite doesn't recognize DataGrip, fall through to default
            default:
                return KiteConstants.APPLICATION_ID;
        }
    }
}
