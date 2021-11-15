package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.*;
import com.kite.intellij.backend.model.Example;
import com.kite.intellij.backend.model.Id;

import java.lang.reflect.Type;

public class ExampleDeserializer implements JsonDeserializer<Example> {
    @Override
    public Example deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();
        String id = json.getAsJsonPrimitive("id").getAsString();
        String title = json.getAsJsonPrimitive("title").getAsString();

        return new Example(Id.of(id), title);
    }
}
