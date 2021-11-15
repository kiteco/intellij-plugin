package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.intellij.openapi.diagnostic.Logger;
import com.kite.intellij.backend.json.JsonUtils;
import com.kite.intellij.backend.model.*;

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
public class ValueExtDeserializer implements JsonDeserializer<ValueExt> {
    private static final Logger LOG = Logger.getInstance("kite.json.valueExt");

    @Override
    public ValueExt deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        Value value = context.deserialize(json, Value.class);

        JsonPrimitive synopsis = json.getAsJsonPrimitive("synopsis");

        Value[] breadcrumbs = context.deserialize(json.get("breadcrumbs"), Value[].class);

        Detail detail = null;
        if (json.has("details")) {
            JsonObject details = json.getAsJsonObject("details");

            Class<?> type = null;
            String propertyName = null;

            if (details.has("function") && details.get("function").isJsonObject()) {
                type = FunctionDetails.class;
                propertyName = "function";
            } else if (details.has("module") && details.get("module").isJsonObject()) {
                type = ModuleDetails.class;
                propertyName = "module";
            } else if (details.has("type") && details.get("type").isJsonObject()) {
                type = TypeDetails.class;
                propertyName = "type";
            } else if (details.has("instance") && details.get("instance").isJsonObject()) {
                type = InstanceDetails.class;
                propertyName = "instance";
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Unable to parse unknown details object " + details);
            }

            detail = type != null ? context.deserialize(details.get(propertyName), type) : null;
        }

        return new ValueExt(value, JsonUtils.nullableString(synopsis), breadcrumbs, detail);
    }
}
