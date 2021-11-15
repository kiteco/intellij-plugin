package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.model.Usage;

import java.lang.reflect.Type;

public class UsageDeserializer implements JsonDeserializer<Usage> {
    @Override
    public Usage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();
        String code = json.getAsJsonPrimitive("code").getAsString();
        String filename = json.getAsJsonPrimitive("filename").getAsString();
        int line = json.getAsJsonPrimitive("line").getAsInt();

        return new Usage(code, filename, line);
    }
}
