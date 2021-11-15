package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.json.JsonUtils;
import com.kite.intellij.backend.model.Id;
import com.kite.intellij.backend.model.Kind;
import com.kite.intellij.backend.model.Value;

import java.lang.reflect.Type;

/**
 * {
 * id: ID            // can be empty if not identifiable
 * kind: KIND
 * repr: STRING      // e.g. "list", "Model", "numpy", "123"
 * type: STRING
 * type_id: ID
 * components: [VALUE, ...],  // e.g. for "list<tuple<int, str>>"
 * }
 */
public class ValueDeserializer implements JsonDeserializer<Value> {
    @Override
    public Value deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        JsonPrimitive id = json.getAsJsonPrimitive("id");

        JsonPrimitive kindValue = json.getAsJsonPrimitive("kind");
        Kind kind = kindValue == null || kindValue.isJsonNull() ? null : Kind.fromJsonString(kindValue.getAsString());

        JsonPrimitive repr = json.getAsJsonPrimitive("repr");
        JsonPrimitive type = json.getAsJsonPrimitive("type");
        JsonPrimitive typeId = json.getAsJsonPrimitive("type_id");
        Value[] components = context.deserialize(json.getAsJsonArray("components"), Value[].class);

        return new Value(Id.of(id.getAsString()), kind, JsonUtils.nullableString(repr), JsonUtils.nullableString(type), JsonUtils.nullableString(typeId), components);
    }
}
