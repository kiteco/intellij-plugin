package com.kite.intellij.backend.json.deserializer;

import com.google.gson.*;
import com.kite.intellij.backend.json.JsonUtils;
import com.kite.intellij.backend.model.UserInfo;

import java.lang.reflect.Type;

public class UserInfoDeserializer implements JsonDeserializer<UserInfo> {
    @Override
    public UserInfo deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        String id = String.valueOf(json.get("id").getAsInt());
        String name = JsonUtils.nullableString(json.get("name").getAsJsonPrimitive());
        String email = JsonUtils.nullableString(json.get("email").getAsJsonPrimitive());
        String bio = JsonUtils.nullableString(json.get("bio").getAsJsonPrimitive());
        boolean emailVerified = json.get("email_verified").getAsBoolean();
        boolean isInternal = json.get("is_internal").getAsBoolean();
        boolean unsubscribed = json.get("unsubscribed").getAsBoolean();

        return new UserInfo(id, name, email, bio, emailVerified, isInternal, unsubscribed);
    }
}
