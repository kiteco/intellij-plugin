package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.json.JsonUtils;
import com.kite.intellij.backend.model.ModuleDetails;
import com.kite.intellij.backend.model.Symbol;

import java.lang.reflect.Type;

public class ModuleDetailsDeserializer implements JsonDeserializer<ModuleDetails> {
    @Override
    public ModuleDetails deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();
        //module details
        String filename = JsonUtils.nullableString(json.getAsJsonPrimitive("filename"));
        int totalMembers = json.get("total_members").getAsInt();
        Symbol[] members = context.deserialize(json.get("members"), Symbol[].class);

        return new ModuleDetails(filename, totalMembers, members);
    }
}
