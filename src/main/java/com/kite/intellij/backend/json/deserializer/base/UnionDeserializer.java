package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.model.Union;
import com.kite.intellij.backend.model.Value;

import java.lang.reflect.Type;

public class UnionDeserializer implements JsonDeserializer<Union> {
    @Override
    public Union deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonArray json = jsonElement.getAsJsonArray();

        Value[] values = context.deserialize(json, Value[].class);
        return new Union(values);
    }
}
