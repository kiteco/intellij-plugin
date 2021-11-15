package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.model.Call;
import com.kite.intellij.backend.model.Signature;
import com.kite.intellij.backend.model.ValueExt;

import java.lang.reflect.Type;

public class CallDeserializer implements JsonDeserializer<Call> {
    @Override
    public Call deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        ValueExt callee = context.deserialize(json.get("callee"), ValueExt.class);
        int argIndex = json.getAsJsonPrimitive("arg_index").getAsInt();

        //fixme(jansorg): Remove this fallback as soon as we're sure that most/all users use an up-to-date version of Kite
        JsonElement funcNameProp = json.get("func_name");
        String funcName = funcNameProp != null ? funcNameProp.getAsString() : (callee != null ? callee.getRepresentation() : "");

        boolean inKwargs = false;
        if (json.has("language_details")) {
            JsonObject details = json.getAsJsonObject("language_details");
            if (details.has("python")) {
                JsonObject python = details.getAsJsonObject("python");
                inKwargs = python.has("in_kwargs") && python.getAsJsonPrimitive("in_kwargs").getAsBoolean();
            }
        }

        Signature[] signatures = context.deserialize(json.get("signatures"), Signature[].class);

        return new Call(funcName, callee, argIndex, signatures, inKwargs);
    }
}
