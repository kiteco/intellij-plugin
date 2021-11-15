package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.model.Symbol;
import com.kite.intellij.backend.model.SymbolExt;
import com.kite.intellij.backend.model.Token;

import java.lang.reflect.Type;

/**
 * {
 * begin_bytes: INT   // offset in bytes from start of file
 * end_bytes: INT
 * begin_runes: INT   // offset in code points from start of file
 * end_runes: INT
 * part_of_syntax: "name" | "attr" | ...
 * symbol: SYMBOL
 * }
 */
public class TokenDeserializer implements JsonDeserializer<Token> {
    @Override
    public Token deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        int beginBytes = json.getAsJsonPrimitive("begin_bytes").getAsInt();
        int endBytes = json.getAsJsonPrimitive("end_bytes").getAsInt();
        int beginRunes = json.getAsJsonPrimitive("begin_runes").getAsInt();
        int endRunes = json.getAsJsonPrimitive("end_runes").getAsInt();
        String partOfSyntax = json.getAsJsonPrimitive("part_of_syntax").getAsString();
        Symbol symbol = context.deserialize(json.getAsJsonObject("symbol"), SymbolExt.class);

        return new Token(beginBytes, endBytes, beginRunes, endRunes, partOfSyntax, symbol);
    }
}
