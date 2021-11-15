package com.kite.intellij.backend.json.deserializer;

import com.google.gson.*;
import com.kite.intellij.backend.model.Report;
import com.kite.intellij.backend.model.SymbolExt;
import com.kite.intellij.backend.response.HoverResponse;

import java.lang.reflect.Type;

/**
 * {
 * part_of_syntax: "name" | "attr" | ...
 * symbol: [SYMBOL_EXT, SYMBOL_EXT, ...]  // empty if not resolved
 * report: REPORT  // corresponds to the first symbol
 * }
 */
public class HoverResponseDeserializer implements JsonDeserializer<HoverResponse> {
    @Override
    public HoverResponse deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        String partOfSyntax = json.getAsJsonPrimitive("part_of_syntax").getAsString();
        SymbolExt[] symbols = context.deserialize(json.get("symbol"), SymbolExt[].class);
        Report report = context.deserialize(json.get("report"), Report.class);

        return new HoverResponse(partOfSyntax, symbols, report);
    }
}
