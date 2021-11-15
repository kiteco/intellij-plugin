package com.kite.intellij.backend.json.deserializer;

import com.google.gson.*;
import com.kite.intellij.backend.json.JsonUtils;
import com.kite.intellij.backend.model.CompletionRange;
import com.kite.intellij.backend.model.CompletionSnippet;

import java.lang.reflect.Type;

public class CompletionSnippetDeserializer implements JsonDeserializer<CompletionSnippet> {
    @Override
    public CompletionSnippet deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();
        String text = JsonUtils.string(json.getAsJsonPrimitive("text"));
        CompletionRange[] placeholders = context.deserialize(json.get("placeholders"), CompletionRange[].class);
        return new CompletionSnippet(text, placeholders);
    }
}
