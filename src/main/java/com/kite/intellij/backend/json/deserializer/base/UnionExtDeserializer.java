package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.model.UnionExt;
import com.kite.intellij.backend.model.ValueExt;

import java.lang.reflect.Type;

public class UnionExtDeserializer implements JsonDeserializer<UnionExt> {
    @Override
    public UnionExt deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonArray json = jsonElement.getAsJsonArray();

        ValueExt[] values = context.deserialize(json, ValueExt[].class);
        return new UnionExt(values);
    }
}
