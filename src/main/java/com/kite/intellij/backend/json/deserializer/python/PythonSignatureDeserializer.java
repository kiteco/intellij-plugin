package com.kite.intellij.backend.json.deserializer.python;

import com.google.gson.*;
import com.kite.intellij.backend.model.ParameterExample;
import com.kite.intellij.backend.model.PythonSignature;
import com.kite.intellij.backend.model.SignatureBase;
import com.kite.intellij.lang.KiteLanguage;

import java.lang.reflect.Type;

public class PythonSignatureDeserializer implements JsonDeserializer<PythonSignature> {
    @Override
    public PythonSignature deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        SignatureBase base = context.deserialize(jsonElement, SignatureBase.class);

        ParameterExample[] kwargs = null;
        if (json.has("language_details")) {
            JsonObject details = json.getAsJsonObject("language_details");
            if (details.has(KiteLanguage.Python.jsonName()) && details.get(KiteLanguage.Python.jsonName()).isJsonObject()) {
                JsonObject pythonDetails = details.getAsJsonObject(KiteLanguage.Python.jsonName());
                kwargs = context.deserialize(pythonDetails.get("kwargs"), ParameterExample[].class);
            }
        } else if (json.has("kwargs")) {
            //compatibility fallback
            kwargs = context.deserialize(json.get("kwargs"), ParameterExample[].class);
        }

        //mark deserialized kwargs parameters as such
        if (kwargs != null) {
            for (int i = 0; i < kwargs.length; i++) {
                kwargs[i] = kwargs[i].asKwarg();
            }
        }

        return new PythonSignature(base, kwargs);
    }
}
