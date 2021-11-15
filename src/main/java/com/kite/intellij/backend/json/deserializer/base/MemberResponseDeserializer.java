package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.model.SymbolExt;
import com.kite.intellij.backend.response.MembersResponse;

import java.lang.reflect.Type;

/**
 * {
 * total: int  // The total number of members for the value
 * start: int  // Index from which members started being returned
 * end: int    // Index from which the next page begins
 * members: [SYMBOL_EXT, ...]
 * }
 */
public class MemberResponseDeserializer implements JsonDeserializer<MembersResponse> {
    @Override
    public MembersResponse deserialize(JsonElement jsonData, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonData.getAsJsonObject();

        int total = json.get("total").getAsInt();
        int start = json.get("start").getAsInt();
        int end = json.get("end").getAsInt();
        SymbolExt[] members = context.deserialize(json.get("members"), SymbolExt[].class);

        return new MembersResponse(total, start, end, members);
    }
}
