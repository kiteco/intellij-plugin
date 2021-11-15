package com.kite.intellij.backend.json.deserializer.base;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.kite.intellij.backend.json.JsonUtils;
import com.kite.intellij.backend.model.KiteFileStatus;
import com.kite.intellij.backend.model.KiteFileStatusResponse;
import com.kite.intellij.backend.model.NotificationButton;

import java.lang.reflect.Type;

public class FileStatusDeserializer implements JsonDeserializer<KiteFileStatusResponse> {
    @Override
    public KiteFileStatusResponse deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        KiteFileStatus status = KiteFileStatus.fromJsonString(json.getAsJsonPrimitive("status").getAsString());
        String shortStatus = JsonUtils.nullableString(json.getAsJsonPrimitive("short"));
        String longStatus = JsonUtils.nullableString(json.getAsJsonPrimitive("long"));

        NotificationButton button = null;
        JsonObject jsonButton = json.getAsJsonObject("button");
        if (jsonButton != null && !jsonButton.isJsonNull()) {
            button = new NotificationButton();
            button.text = jsonButton.getAsJsonPrimitive("text").getAsString();
            button.action = jsonButton.getAsJsonPrimitive("action").getAsString();
            if (jsonButton.has("link") && !jsonButton.getAsJsonPrimitive("link").isJsonNull()) {
                button.link = jsonButton.getAsJsonPrimitive("link").getAsString();
            }
        }

        return new KiteFileStatusResponse(status, shortStatus, longStatus, button);
    }
}
