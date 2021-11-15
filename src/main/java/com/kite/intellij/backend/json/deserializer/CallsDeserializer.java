package com.kite.intellij.backend.json.deserializer;

import com.google.gson.*;
import com.kite.intellij.backend.model.Call;
import com.kite.intellij.backend.model.Calls;

import java.lang.reflect.Type;

public class CallsDeserializer implements JsonDeserializer<Calls> {
    @Override
    public Calls deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        Call[] calls = context.deserialize(json.get("calls"), Call[].class);

        return new Calls(calls);
    }
}
