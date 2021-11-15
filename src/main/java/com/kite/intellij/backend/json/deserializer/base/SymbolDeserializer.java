package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.json.JsonUtils;
import com.kite.intellij.backend.model.Id;
import com.kite.intellij.backend.model.Symbol;
import com.kite.intellij.backend.model.Union;
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
public class SymbolDeserializer implements JsonDeserializer<Symbol> {

    @Override
    public Symbol deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        JsonPrimitive id = json.getAsJsonPrimitive("id");
        JsonPrimitive name = json.getAsJsonPrimitive("name");
        Value namespace = context.deserialize(json.get("namespace"), Value.class);
        Union value = context.deserialize(json.get("value"), Union.class);

        String idValue = JsonUtils.nullableString(id);
        return new Symbol(idValue == null || idValue.isEmpty() ? null : Id.of(idValue), JsonUtils.nullableString(name), namespace, value);
    }
}
