package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.model.FunctionDetailsBase;
import com.kite.intellij.backend.model.Parameter;
import com.kite.intellij.backend.model.Signature;
import com.kite.intellij.backend.model.Union;

import java.lang.reflect.Type;

public class FunctionDetailsBaseDeserializer implements JsonDeserializer<FunctionDetailsBase> {
    @Override
    public FunctionDetailsBase deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        Union returnValue = json.has("return_value") ? context.deserialize(json.get("return_value"), Union.class) : null;
        Parameter[] parameters = context.deserialize(json.get("parameters"), Parameter[].class);
        Signature[] signatures = context.deserialize(json.get("signatures"), Signature[].class);

        return new FunctionDetailsBase(signatures, parameters, returnValue);
    }
}
