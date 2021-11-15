package com.kite.intellij.backend.json.deserializer;

import com.google.gson.*;
import com.kite.intellij.backend.model.KiteCompletion;
import com.kite.intellij.backend.response.KiteCompletions;

import java.lang.reflect.Type;

public class CompletionResponseDeserializer implements JsonDeserializer<KiteCompletions> {
    @Override
    public KiteCompletions deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonElement completions = json.getAsJsonObject().get("completions");
        if (completions == null || completions.isJsonNull()) {
            return KiteCompletions.EMPTY;
        }

        JsonObject pos = json.getAsJsonObject().getAsJsonObject("position");
        long begin = 0;
        long end = 0;
        if (pos != null) {
            begin = pos.getAsJsonPrimitive("begin").getAsLong();
            JsonPrimitive endProp = pos.getAsJsonPrimitive("end");
            if (endProp != null && !endProp.isJsonNull()) {
                end = endProp.getAsLong();
            }
        }

        return new KiteCompletions(begin, end, context.deserialize(completions, KiteCompletion[].class));
    }
}
