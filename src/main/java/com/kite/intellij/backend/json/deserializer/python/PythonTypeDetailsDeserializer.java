package com.kite.intellij.backend.json.deserializer.python;

import com.google.gson.*;
import com.kite.intellij.backend.model.*;
import com.kite.intellij.lang.KiteLanguage;

import java.lang.reflect.Type;

public class PythonTypeDetailsDeserializer implements JsonDeserializer<PythonTypeDetails> {
    @Override
    public PythonTypeDetails deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        TypeDetailsBase base = context.deserialize(jsonElement, TypeDetailsBase.class);

        JsonObject pythonDetails = json;
        if (json.has("language_details")) {
            JsonObject details = json.getAsJsonObject("language_details");
            if (details.has(KiteLanguage.Python.jsonName()) && details.get(KiteLanguage.Python.jsonName()).isJsonObject()) {
                pythonDetails = details.getAsJsonObject(KiteLanguage.Python.jsonName());
            }
        }

        Base[] bases = context.deserialize(pythonDetails.get("bases"), Base[].class);
        PythonFunctionDetails constructor = context.deserialize(pythonDetails.get("constructor"), FunctionDetails.class);

        return new PythonTypeDetails(base, bases, constructor);
    }
}
