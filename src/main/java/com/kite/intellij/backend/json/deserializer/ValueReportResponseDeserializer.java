package com.kite.intellij.backend.json.deserializer;

import com.google.gson.*;
import com.kite.intellij.backend.model.Report;
import com.kite.intellij.backend.model.ValueExt;
import com.kite.intellij.backend.response.ValueReportResponse;

import java.lang.reflect.Type;

public class ValueReportResponseDeserializer implements JsonDeserializer<ValueReportResponse> {
    @Override
    public ValueReportResponse deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();
        ValueExt value = context.deserialize(json.get("value"), ValueExt.class);
        Report report = context.deserialize(json.get("report"), Report.class);

        return new ValueReportResponse(value, report);
    }
}
