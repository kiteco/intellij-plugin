package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.kite.intellij.lang.KiteLanguage;

import java.lang.reflect.Type;

public class LanguageDeserializer implements JsonDeserializer<KiteLanguage> {
    @Override
    public KiteLanguage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        return KiteLanguage.fromKiteName(jsonElement.getAsString());
    }
}
