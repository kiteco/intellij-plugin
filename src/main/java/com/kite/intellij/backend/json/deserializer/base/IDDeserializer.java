package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.kite.intellij.backend.model.Id;

import java.lang.reflect.Type;

public class IDDeserializer implements JsonDeserializer<Id> {
    @Override
    public Id deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Id.of(jsonElement.getAsString());
    }
}
