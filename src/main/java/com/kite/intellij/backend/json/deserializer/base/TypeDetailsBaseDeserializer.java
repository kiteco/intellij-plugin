package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.model.Symbol;
import com.kite.intellij.backend.model.TypeDetailsBase;

import java.lang.reflect.Type;

public class TypeDetailsBaseDeserializer implements JsonDeserializer<TypeDetailsBase> {
    @Override
    public TypeDetailsBase deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        Symbol[] members = context.deserialize(json.get("members"), Symbol[].class);
        int totalMembers = json.getAsJsonPrimitive("total_members").getAsInt();

        return new TypeDetailsBase(totalMembers, members);
    }
}
