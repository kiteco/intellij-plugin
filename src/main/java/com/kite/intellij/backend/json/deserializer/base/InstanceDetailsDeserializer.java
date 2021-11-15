package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.model.InstanceDetails;
import com.kite.intellij.backend.model.Union;

import java.lang.reflect.Type;

public class InstanceDetailsDeserializer implements JsonDeserializer<InstanceDetails> {
    @Override
    public InstanceDetails deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();
        Union instanceType = context.deserialize(json.get("type"), Union.class);
        return new InstanceDetails(instanceType);
    }
}
