package com.kite.intellij.backend.json.deserializer;

import com.google.gson.*;
import com.kite.intellij.backend.model.ParameterExample;
import com.kite.intellij.backend.model.SignatureBase;

import java.lang.reflect.Type;

public class SignatureBaseDeserializer implements JsonDeserializer<SignatureBase> {
    @Override
    public SignatureBase deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        ParameterExample[] args = context.deserialize(json.get("args"), ParameterExample[].class);

        return new SignatureBase(args);
    }
}
