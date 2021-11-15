package com.kite.intellij.backend.json.deserializer.python;

import com.google.gson.*;
import com.kite.intellij.backend.model.FunctionDetailsBase;
import com.kite.intellij.backend.model.Parameter;
import com.kite.intellij.backend.model.PythonFunctionDetails;
import com.kite.intellij.backend.model.Union;
import com.kite.intellij.lang.KiteLanguage;

import java.lang.reflect.Type;

public class PythonFunctionDetailsDeserializer implements JsonDeserializer<PythonFunctionDetails> {
    @Override
    public PythonFunctionDetails deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        FunctionDetailsBase base = context.deserialize(jsonElement, FunctionDetailsBase.class);

        if (json.has("language_details")) {
            JsonObject details = json.getAsJsonObject("language_details");
            if (details.has(KiteLanguage.Python.jsonName()) && details.get(KiteLanguage.Python.jsonName()).isJsonObject()) {
                JsonObject pythonDetails = details.getAsJsonObject(KiteLanguage.Python.jsonName());

                Parameter receiver = context.deserialize(pythonDetails.get("receiver"), Parameter.class);
                Parameter vararg = context.deserialize(pythonDetails.get("vararg"), Parameter.class);
                Parameter kwarg = context.deserialize(pythonDetails.get("kwarg"), Parameter.class);
                Union returnAnnotation = pythonDetails.has("return_annotation") ? context.deserialize(pythonDetails.get("return_annotation"), Union.class) : null;
                Parameter[] kwargParameters = context.deserialize(pythonDetails.get("kwarg_parameters"), Parameter[].class);

                return new PythonFunctionDetails(base, receiver, vararg, kwarg, returnAnnotation, kwargParameters);
            }
        }

        return new PythonFunctionDetails(base, null, null, null, null, null);
    }
}
