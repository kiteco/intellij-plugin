package com.kite.intellij.backend.json.deserializer;

import com.google.gson.*;
import com.kite.intellij.backend.json.JsonUtils;
import com.kite.intellij.backend.model.CompletionRange;
import com.kite.intellij.backend.model.CompletionSnippet;
import com.kite.intellij.backend.model.KiteCompletion;

import java.lang.reflect.Type;

public class CompletionSuggestionDeserializer implements JsonDeserializer<KiteCompletion> {
    @Override
    public KiteCompletion deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();
        String doc = json.has("documentation") && json.get("documentation").isJsonObject()
                ? json.getAsJsonObject("documentation").getAsJsonPrimitive("text").getAsString()
                : json.getAsJsonPrimitive("documentation").getAsString();
        String webID = JsonUtils.nullableString(json.getAsJsonPrimitive("web_id"));
        String localID = JsonUtils.nullableString(json.getAsJsonPrimitive("local_id"));
        CompletionSnippet snippet = context.deserialize(json.getAsJsonObject("snippet"), CompletionSnippet.class);
        CompletionRange replace = context.deserialize(json.getAsJsonObject("replace"), CompletionRange.class);
        KiteCompletion[] children = context.deserialize(json.getAsJsonArray("children"), KiteCompletion[].class);

        return new KiteCompletion(
                JsonUtils.string(json.getAsJsonPrimitive("display")),
                snippet,
                JsonUtils.nullableString(json.getAsJsonPrimitive("hint")),
                doc,
                replace,
                json.getAsJsonPrimitive("smart").getAsBoolean(),
                webID,
                localID,
                children);
    }
}
