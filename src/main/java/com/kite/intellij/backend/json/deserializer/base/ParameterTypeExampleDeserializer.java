package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.json.JsonUtils;
import com.kite.intellij.backend.model.Id;
import com.kite.intellij.backend.model.ParameterTypeExample;

import java.lang.reflect.Type;

public class ParameterTypeExampleDeserializer implements JsonDeserializer<ParameterTypeExample> {
    @Override
    public ParameterTypeExample deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        String id = JsonUtils.string(json.getAsJsonPrimitive("id"));
        String name = JsonUtils.string(json.getAsJsonPrimitive("name"));
        String[] examples = context.deserialize(json.get("examples"), String[].class);

        return new ParameterTypeExample(Id.of(id), name, examples);
    }
}
