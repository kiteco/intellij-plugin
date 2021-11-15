package com.kite.intellij.backend.json.deserializer;

import com.google.gson.*;
import com.kite.intellij.backend.model.Report;
import com.kite.intellij.backend.model.SymbolExt;
import com.kite.intellij.backend.response.SymbolReportResponse;

import java.lang.reflect.Type;

public class SymbolReportResponseDeserializer implements JsonDeserializer<SymbolReportResponse> {
    @Override
    public SymbolReportResponse deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();
        SymbolExt symbol = context.deserialize(json.get("symbol"), SymbolExt.class);
        Report report = context.deserialize(json.get("report"), Report.class);

        return new SymbolReportResponse(symbol, report);
    }
}
