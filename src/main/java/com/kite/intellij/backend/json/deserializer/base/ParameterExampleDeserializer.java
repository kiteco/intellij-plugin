package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.json.JsonUtils;
import com.kite.intellij.backend.model.ParameterExample;
import com.kite.intellij.backend.model.ParameterTypeExample;

import java.lang.reflect.Type;

public class ParameterExampleDeserializer implements JsonDeserializer<ParameterExample> {
    @Override
    public ParameterExample deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        String name = JsonUtils.string(json.getAsJsonPrimitive("name"));
        ParameterTypeExample[] types = context.deserialize(json.get("types"), ParameterTypeExample[].class);

        return new ParameterExample(name, false, types);
    }
}
