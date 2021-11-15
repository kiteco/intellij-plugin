package com.kite.intellij.backend;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.kite.intellij.backend.http.HttpConnectionUnavailableException;
import com.kite.intellij.backend.http.HttpRequestFailedException;
import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.HttpTimeoutConfig;
import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.backend.json.KiteJsonParsing;
import com.kite.intellij.backend.model.Calls;
import com.kite.intellij.backend.model.EventType;
import com.kite.intellij.backend.model.Id;
import com.kite.intellij.backend.model.KiteCompletion;
import com.kite.intellij.backend.model.KiteFileStatusResponse;
import com.kite.intellij.backend.model.LicenseInfo;
import com.kite.intellij.backend.model.Symbol;
import com.kite.intellij.backend.model.SymbolExt;
import com.kite.intellij.backend.model.TextSelection;
import com.kite.intellij.backend.model.UserInfo;
import com.kite.intellij.backend.model.Value;
import com.kite.intellij.backend.response.HoverResponse;
import com.kite.intellij.backend.response.KiteCompletions;
import com.kite.intellij.backend.response.MembersResponse;
import com.kite.intellij.backend.response.SymbolReportResponse;
import com.kite.intellij.backend.response.ValueReportResponse;
import com.kite.intellij.codeFinder.KiteFindRelatedError;
import com.kite.intellij.lang.KiteLanguage;
import com.kite.intellij.platform.fs.CanonicalFilePath;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Interface to talk to the Kite application.
 * <p>
 * In test cases an alternative implementation ({@link MockKiteApiService}) is used to provide access to the
 * call history made by the plugin's components.
 *
  */
public interface KiteApiService {
    /**
     * Returns the application wide instance.
     *
     * @return The current instance.
     */
    @Nonnull
    static KiteApiService getInstance() {
        return ApplicationManager.getApplication().getService(KiteApiService.class);
    }

    /**
     * @return {@code true} if the recorded connection status indicates that the connection is available, false if the connection status was set to fals due to an connection error
     */
    boolean isConnectionAvailable();

    /**
     * Sends a new request and returns whether the connection was successful or not. It returns {@code true} even if the
     * response's status code was != 200.
     *
     * @return {@code true} if the connection to Kite was successful.
     */
    boolean checkOnlineStatus();

    /**
     * Adds a listener to receive {@link HttpStatusException}. The listeners are only called for non-200 response codes.
     * It is not guaranteed that the listener will be invoked in the same thread as the caller of the API method.
     *
     * @param listener The listener to notify
     * @param parent
     */
    void addHttpRequestStatusListener(HttpStatusListener listener, @Nullable Disposable parent);

    /**
     * Removes a listener from the list of registered listeners
     *
     * @param listener The listener to remove
     * @return true if the listener was successfully removed, false otherwise
     */
    void removeHttpRequestStatusListener(HttpStatusListener listener);

    /**
     * Registers a new connection status listener.
     *
     * @param listener The new listener
     * @param parent
     */
    void addConnectionStatusListener(ConnectionStatusListener listener, @Nullable Disposable parent);

    /**
     * Removes a given connection status listener.
     *
     * @param listener The listener to remove
     * @return {@code true} if the listener could be removed
     */
    void removeConnectionStatusListener(ConnectionStatusListener listener);

    /**
     * Reset any registered http status or connection status listener.
     */
    void resetStatusListener();

    @Nullable
    default KiteCompletions completions(CanonicalFilePath cilePath, String fileContent, @Nonnull Integer cursorOffset) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        return completions(cilePath, fileContent, cursorOffset, null, false, HttpTimeoutConfig.DefaultTimeout);
    }

    /**
     * Retrieve a list of code completion suggestions for the given context.
     *
     * @param canonicalFilePath The cononical path for the currently used file.
     * @param fileContent       The current content of the file which will be posted to Kite.
     * @param cursorOffset      The offset in code-points where the suggestions should be shown, i.e. the context of the suggestions.
     * @param cursorEndOffset
     * @param disableSnippets   {@code True} tells Kite to not return snippets in the result
     * @param timeout
     * @return A list of suggestions. Might be empty, won't be null.
     */
    @Nullable
    KiteCompletions completions(CanonicalFilePath canonicalFilePath, String fileContent, @Nonnull Integer cursorOffset, @Nullable Integer cursorEndOffset, boolean disableSnippets, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException;

    /**
     * Reports a completion as selected to the Kite Engine
     *
     * @param filePath   The canonical path for the currently used file.
     * @param completion The selected completion.
     * @param timeout
     */
    @Nullable
    void completionSelected(CanonicalFilePath filePath, KiteCompletion completion, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException;

    /**
     * @param filePath The native path for the currently used file.
     * @param lineNo   The line number of the primary caret (zero-based).
     */
    void relatedCode(CanonicalFilePath filePath, @Nullable Integer lineNo, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException, KiteFindRelatedError;

    /**
     * Returns the hover content for the given file and selection.
     *
     * @param filePath    The cononical path for the currently used file.
     * @param fileContent The current content of the file which will be posted to Kite.
     * @param offset      The end of the range as character offset to report on.
     * @return The hover response, if available. If an error occurred then {@code null} is returned.
     */
    @Nullable
    default HoverResponse hover(CanonicalFilePath filePath, String fileContent, int offset) throws KiteHttpException {
        return hover(filePath, fileContent, offset, HttpTimeoutConfig.DefaultTimeout);
    }

    /**
     * Returns the hover content for the given file and selection.
     *
     * @param filePath    The cononical path for the currently used file.
     * @param fileContent The current content of the file which will be posted to Kite.
     * @param offset      The begining of the range as character offset to report on.
     * @param timeout
     * @return The hover response, if available. If an error occurred then {@code null} is returned.
     */
    @Nullable
    HoverResponse hover(CanonicalFilePath filePath, String fileContent, int offset, HttpTimeoutConfig timeout) throws KiteHttpException;

    /**
     * Returns the report content for a given value.
     *
     * @param value The value whose Id should be send to the kite backend
     * @return The report response, if available
     */
    @Nullable
    default ValueReportResponse report(@Nonnull Value value) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        return report(value, HttpTimeoutConfig.DefaultTimeout);
    }

    /**
     * Returns the report content for a given value.
     *
     * @param value   The value whose Id should be send to the kite backend
     * @param timeout
     * @return The report response, if available
     */
    @Nullable
    default ValueReportResponse report(@Nonnull Value value, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        return valueReport(value.getId(), timeout);
    }

    /**
     * Report for a value given by {@link Id}.
     *
     * @param id The id of a value
     * @return The report, if available, for a value identified by the id.
     */
    @Nullable
    default ValueReportResponse valueReport(@Nonnull Id id) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        return valueReport(id, HttpTimeoutConfig.DefaultTimeout);
    }

    /**
     * Report for a value given by {@link Id}.
     *
     * @param id      The id of a value
     * @param timeout
     * @return The report, if available, for a value identified by the id.
     */
    @Nullable
    ValueReportResponse valueReport(@Nonnull Id id, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException;

    /**
     * Returns the report content for a given value.
     *
     * @param symbol The value whose Id should be send to the kite backend
     * @return The report response, if available
     */
    @Nullable
    default SymbolReportResponse report(@Nonnull Symbol symbol) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        return symbolReport(symbol.getId());
    }

    /**
     * @see #symbolReport(Id)
     */
    @Nullable
    default SymbolReportResponse report(@Nonnull SymbolExt symbol) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        return symbolReport(symbol.getId());
    }

    /**
     * Report for a symbol given by {@link Id}.
     *
     * @param id The id of a value
     * @return The report, if available, for a symbol identified by the id.
     */
    @Nullable
    default SymbolReportResponse symbolReport(@Nonnull Id id) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        return symbolReport(id, HttpTimeoutConfig.DefaultTimeout);
    }

    /**
     * Report for a symbol given by {@link Id}.
     *
     * @param id      The id of a value
     * @param timeout
     * @return The report, if available, for a symbol identified by the id.
     */
    @Nullable
    SymbolReportResponse symbolReport(@Nonnull Id id, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException;

    /**
     * Returns a list of members available in the element identified by {@code id}.
     *
     * @param id     Identifies the element containing members
     * @param offset Index for the first element to retrieve
     * @param limit  The maximum number of elements to return
     * @return The response if data was available. {@code null} if the response could not be retrieved.
     */
    @Nullable
    default MembersResponse members(@Nonnull Id id, int offset, int limit) throws HttpConnectionUnavailableException, HttpRequestFailedException {
        return members(id, offset, limit, HttpTimeoutConfig.DefaultTimeout);
    }

    /**
     * Returns a list of members available in the element identified by {@code id}.
     *
     * @param id      Identifies the element containing members
     * @param offset  Index for the first element to retrieve
     * @param limit   The maximum number of elements to return
     * @param timeout
     * @return The response if data was available. {@code null} if the response could not be retrieved.
     */
    @Nullable
    MembersResponse members(@Nonnull Id id, int offset, int limit, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException;

    /**
     * /**
     * Returns a list of signatures available at the given offset in the file content.
     *
     * @param file        The file path
     * @param fileContent The current file's content
     * @param offset      The cursor offset in codepoints
     * @return The response if data was available. {@code null} if the response could not be retrieved.
     */
    @Nullable
    default Calls signatures(@Nonnull CanonicalFilePath file, String fileContent, int offset) throws KiteHttpException {
        return signatures(file, fileContent, offset, HttpTimeoutConfig.DefaultTimeout);
    }

    /**
     * Returns a list of signatures available at the given offset in the file content.
     *
     * @param file        The file path
     * @param fileContent The current file's content
     * @param offset      The cursor offset in codepoints
     * @param timeout
     * @return The response if data was available. {@code null} if the response could not be retrieved.
     */
    @Nullable
    Calls signatures(@Nonnull CanonicalFilePath file, String fileContent, int offset, HttpTimeoutConfig timeout) throws KiteHttpException;

    /**
     * Returns the file's indexing state.
     *
     * @param file The file to check
     * @return Kite's current state regarding the given file.
     */
    @Nonnull
    default KiteFileStatusResponse fileStatus(@Nonnull CanonicalFilePath file) {
        return fileStatus(file, HttpTimeoutConfig.DefaultTimeout);
    }

    /**
     * Returns the file's indexing state.
     *
     * @param file    The file to check
     * @param timeout
     * @return Kite's current state regarding the given file.
     */
    @Nonnull
    KiteFileStatusResponse fileStatus(@Nonnull CanonicalFilePath file, HttpTimeoutConfig timeout);

    /**
     * Returns information about the currently logged in user.
     *
     * @return The user information, if available
     */
    @Nullable
    default UserInfo userInfo() {
        return userInfo(HttpTimeoutConfig.DefaultTimeout);
    }

    /**
     * Returns information about the currently logged in user.
     *
     * @param timeout
     * @return The user information, if available
     */
    @Nullable
    UserInfo userInfo(HttpTimeoutConfig timeout);

    /**
     * Sends an application event to Kite.
     *
     * @param eventType                        The event to dispatch to Kite. This is one of the events mentioned in the API documentation.
     * @param cononicalFilePath                The path of the file which is the source of the event.
     * @param fileContent                      The current content of the file.
     * @param selections                       The currently available selections. If no selection is available then the current caret position should be passed (see {@link TextSelection#create(int, int)}} and {@link #sendEvent(EventType, CanonicalFilePath, String, List, boolean, HttpTimeoutConfig)}).
     * @param statusUpdateNotificationsEnabled
     * @param timeout
     * @return {@code true} if the event was successfully sent to Kite, {@code false} otherwise.
     */
    boolean sendEvent(EventType eventType, CanonicalFilePath cononicalFilePath, String fileContent, List<TextSelection> selections, boolean statusUpdateNotificationsEnabled, HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException;

    default Set<KiteLanguage> languages() throws HttpConnectionUnavailableException, HttpRequestFailedException {
        return languages(HttpTimeoutConfig.DefaultTimeout);
    }

    /**
     * Returns the languages which is currently supported by Kite.
     *
     * @return A set of all currently supported and enabled languages
     */
    Set<KiteLanguage> languages(HttpTimeoutConfig timeout) throws HttpConnectionUnavailableException, HttpRequestFailedException;

    /**
     * Tells kited to open the copilot application.
     */
    void openKiteCopilot() throws HttpConnectionUnavailableException, HttpStatusException, HttpRequestFailedException;

    /**
     * @return Information about the current license used in kited
     */
    LicenseInfo licenseInfo();

    // onboarding

    /**
     * @param language The language to pass to kited
     * @return The path of the onboarding file to show. {@code null} is returned when an exception occurred.
     */
    @Nullable
    CanonicalFilePath getOnboardingFilePath(@Nonnull KiteLanguage language);

    // settings

    /**
     * @param name The name of the setting to retrieve.
     * @return The current value of the named setting. If it's not yet stored in Kite's settings then null is returned.
     */
    @Nullable
    String getSetting(@Nonnull String name);

    /**
     * Updates the value of a Kite settings.
     *
     * @param name  The name of the setting to update
     * @param value The new value
     */
    void setSetting(@Nonnull String name, String value) throws KiteHttpException;

    @Nonnull
    KiteJsonParsing getJsonParsing();
}
