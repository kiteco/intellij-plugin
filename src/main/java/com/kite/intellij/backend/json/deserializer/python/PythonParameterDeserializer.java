package com.kite.intellij.backend.json.deserializer.python;

import com.google.gson.*;
import com.kite.intellij.backend.model.ParameterBase;
import com.kite.intellij.backend.model.PythonParameter;
import com.kite.intellij.backend.model.Union;
import com.kite.intellij.lang.KiteLanguage;

import java.lang.reflect.Type;

public class PythonParameterDeserializer implements JsonDeserializer<PythonParameter> {
    @Override
    public PythonParameter deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        ParameterBase base = context.deserialize(jsonElement, ParameterBase.class);

        boolean keywordOnly = false;
        Union defaultValue = null;
        Union annotation = null;

        JsonObject json = jsonElement.getAsJsonObject();
        if (json.has("language_details")) {
            JsonObject languageDetails = json.getAsJsonObject("language_details");
            if (languageDetails.has(KiteLanguage.Python.jsonName()) && languageDetails.get(KiteLanguage.Python.jsonName()).isJsonObject()) {
                JsonObject pythonDetails = languageDetails.getAsJsonObject(KiteLanguage.Python.jsonName());

                keywordOnly = pythonDetails.getAsJsonPrimitive("keyword_only").getAsBoolean();
                defaultValue = context.deserialize(pythonDetails.get("default_value"), Union.class);
                annotation = context.deserialize(pythonDetails.get("annotation"), Union.class);
            }
        }

        return new PythonParameter(base, defaultValue, annotation, keywordOnly);
    }
}
