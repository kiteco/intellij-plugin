package com.kite.intellij.backend.json.deserializer;

import com.google.gson.*;
import com.kite.intellij.backend.model.CompletionRange;

import java.lang.reflect.Type;

public class CompletionRangeDeserializer implements JsonDeserializer<CompletionRange> {
    @Override
    public CompletionRange deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();
        int begin = json.getAsJsonPrimitive("begin").getAsInt();
        int end = json.getAsJsonPrimitive("end").getAsInt();
        return new CompletionRange(begin, end);
    }
}
