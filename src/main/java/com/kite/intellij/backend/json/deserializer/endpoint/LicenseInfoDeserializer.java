package com.kite.intellij.backend.json.deserializer.endpoint;

import com.google.gson.*;
import com.kite.intellij.backend.json.JsonUtils;
import com.kite.intellij.backend.model.LicenseInfo;

import java.lang.reflect.Type;

public class LicenseInfoDeserializer implements JsonDeserializer<LicenseInfo> {
    @Override
    public LicenseInfo deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        String product = JsonUtils.nullableString(json.getAsJsonPrimitive("product"));
        String plan = JsonUtils.nullableString(json.getAsJsonPrimitive("plan"));
        boolean trialAvailable = json.getAsJsonPrimitive("trial_available").getAsBoolean();
        int days = json.has("days_remaining") ? json.getAsJsonPrimitive("days_remaining").getAsInt() : 0;

        return new LicenseInfo(product, plan, days, trialAvailable);
    }
}
