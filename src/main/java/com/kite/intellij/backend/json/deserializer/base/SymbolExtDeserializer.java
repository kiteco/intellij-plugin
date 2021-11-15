package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.json.JsonUtils;
import com.kite.intellij.backend.model.Id;
import com.kite.intellij.backend.model.SymbolExt;
import com.kite.intellij.backend.model.UnionExt;
import com.kite.intellij.backend.model.Value;

import java.lang.reflect.Type;

/**
 * {
 * id: ID   // identifies the symbol, not the value. Can be empty.
 * name: STRING         // e.g. "name"
 * qualname: STRING     // e.g. "Counter.name"
 * namespace: VALUE
 * value: UNION_EXT
 * synopsis: STRING     // first paragraph from documentation
 * }
 */
public class SymbolExtDeserializer implements JsonDeserializer<SymbolExt> {

    @Override
    public SymbolExt deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        JsonPrimitive id = json.getAsJsonPrimitive("id");
        JsonPrimitive name = json.getAsJsonPrimitive("name");
        JsonPrimitive qualifiedName = json.getAsJsonPrimitive("qualname");
        Value namespace = context.deserialize(json.get("namespace"), Value.class);
        UnionExt value = context.deserialize(json.get("value"), UnionExt.class);
        JsonPrimitive synopsis = json.getAsJsonPrimitive("synopsis");

        return new SymbolExt(Id.of(id.getAsString()), JsonUtils.nullableString(name), JsonUtils.nullableString(qualifiedName), namespace, value, JsonUtils.nullableString(synopsis));
    }
}
