package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.json.JsonUtils;
import com.kite.intellij.backend.model.ParameterBase;
import com.kite.intellij.backend.model.Union;

import java.lang.reflect.Type;

public class ParameterBaseDeserializer implements JsonDeserializer<ParameterBase> {
    @Override
    public ParameterBase deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        JsonPrimitive name = json.getAsJsonPrimitive("name");
        JsonPrimitive synopsis = json.getAsJsonPrimitive("synopsis");
        Union inferredValue = context.deserialize(json.get("inferred_value"), Union.class);

        return new ParameterBase(JsonUtils.string(name), inferredValue, JsonUtils.nullableString(synopsis));
    }
}
