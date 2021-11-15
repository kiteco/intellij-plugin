package com.kite.intellij.backend.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kite.intellij.backend.model.Calls;
import com.kite.intellij.backend.model.KiteFileStatus;
import com.kite.intellij.backend.model.KiteFileStatusResponse;
import com.kite.intellij.backend.model.KiteServiceNotification;
import com.kite.intellij.backend.model.LicenseInfo;
import com.kite.intellij.backend.model.UserInfo;
import com.kite.intellij.backend.response.CodeFinderResponse;
import com.kite.intellij.backend.response.HoverResponse;
import com.kite.intellij.backend.response.KiteCompletions;
import com.kite.intellij.backend.response.MembersResponse;
import com.kite.intellij.backend.response.SymbolReportResponse;
import com.kite.intellij.backend.response.ValueReportResponse;
import com.kite.intellij.lang.KiteLanguage;
import com.kite.monitoring.TimeTracker;
import com.kite.monitoring.TimerTrackers;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Type;

/**
 * Parsing of the model's JSON data structures. This implementation is thread-safe.
 *
  */
@Immutable
@ThreadSafe
public class KiteJsonParsing {
    private final JsonParser jsonParser = new JsonParser();
    private final Gson gson = KiteGsonFactory.createPython();

    public KiteJsonParsing() {
    }

    /**
     * Parses the given string as a Kite completion request response.
     *
     * @param json The data to parse. This is expected to be valid JSON.
     * @return The parsed completion response.
     */
    public KiteCompletions parseCompletionResponse2(String json) {
        try (TimeTracker ignored = TimerTrackers.start("completion2 json parsing")) {
            return fromJson(json, KiteCompletions.class);
        }
    }

    /**
     * Parses the given string as a Kite hover response.
     *
     * @param json The data to parse. This is expected to be valid JSON.
     * @return The parsed hover response.
     */
    public HoverResponse parseHoverResponse(String json) {
        try (TimeTracker ignored = TimerTrackers.start("hover json parsing")) {
            return fromJson(json, HoverResponse.class);
        }
    }

    /**
     * Parses the given string as a Kite hover response.
     *
     * @param json The data to parse. This is expected to be valid JSON.
     * @return The parsed hover response.
     */
    public ValueReportResponse parseValueReportResponse(String json) {
        try (TimeTracker ignored = TimerTrackers.start("value report json parsing")) {
            return fromJson(json, ValueReportResponse.class);
        }
    }

    /**
     * Parses the given string as a Kite hover response.
     *
     * @param json The data to parse. This is expected to be valid JSON.
     * @return The parsed hover response.
     */
    public SymbolReportResponse parseSymbolReportResponse(String json) {
        try (TimeTracker ignored = TimerTrackers.start("symbol report json parsing")) {
            return fromJson(json, SymbolReportResponse.class);
        }
    }

    /**
     * Parses the given string as Kite members response.
     *
     * @param json The data to parse. This is expected to be valid JSON and to conform to the schema expected for /api/editor/value/{ID}/members?offset={offset}&limit={limit}
     * @return The parsed response, might be null if json is null
     */
    @Contract("null -> null; !null -> !null")
    public MembersResponse parseMembersResponse(@Nullable String json) {
        try (TimeTracker ignored = TimerTrackers.start("members json parsing")) {
            return fromJson(json, MembersResponse.class);
        }
    }

    public UserInfo parseUserInfo(String json) {
        try (TimeTracker ignored = TimerTrackers.start("userinfo json parsing")) {
            return fromJson(json, UserInfo.class);
        }
    }

    public LicenseInfo parseLicenseInfo(String json) {
        try (TimeTracker ignored = TimerTrackers.start("license-info json parsing")) {
            return fromJson(json, LicenseInfo.class);
        }
    }

    public Calls parseCalls(String json) {
        try (TimeTracker ignored = TimerTrackers.start("calls json parsing")) {
            return fromJson(json, Calls.class);
        }
    }

    public KiteFileStatusResponse parseFileStatus(String json) {
        try (TimeTracker ignored = TimerTrackers.start("fileStatus json parsing")) {
            return fromJson(json, KiteFileStatus.class);
        }
    }

    public CodeFinderResponse parseCodeFinderResponse(String json) {
        try (TimeTracker ignored = TimerTrackers.start("codeFinderResponse json parsing")) {
            return fromJson(json, CodeFinderResponse.class);
        }
    }

    /**
     * @param json
     * @return the value of property "reason", if available
     */
    @Nullable
    public String parseReason(String json) {
        try (TimeTracker ignored = TimerTrackers.start("reason json parsing")) {
            JsonElement element = jsonParser.parse(json);
            if (!element.isJsonObject()) {
                return null;
            }

            if (!element.getAsJsonObject().has("reason")) {
                return null;
            }

            return element.getAsJsonObject().get("reason").getAsString();
        }
    }

    @Nonnull
    public KiteLanguage[] parseLanguages(String json) {
        try (TimeTracker ignored = TimerTrackers.start("languages json parsing")) {
            return fromJson(json, KiteLanguage[].class);
        }
    }

    @Nonnull
    public KiteServiceNotification parseKiteNotification(String json) {
        try (TimeTracker ignored = TimerTrackers.start("languages json parsing")) {
            JsonObject element = jsonParser.parse(json).getAsJsonObject();
            JsonObject notification = element.get("notification").getAsJsonObject();
            return gson.fromJson(notification, KiteServiceNotification.class);
        }
    }

    <T> T fromJson(String json, Type expectedType) {
        JsonElement element = jsonParser.parse(json);

        KiteLanguage kiteLanguage = languageOf(element);
        if (kiteLanguage != null && !KiteLanguage.Python.equals(kiteLanguage)) {
            throw new IllegalStateException("only python is supported");
        }

        return gson.fromJson(element, expectedType);
    }

    @Nullable
    private KiteLanguage languageOf(JsonElement json) {
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();

            if (object.has("language")) {
                return KiteLanguage.fromKiteName(object.get("language").getAsString());
            }
        }

        return null;
    }
}
