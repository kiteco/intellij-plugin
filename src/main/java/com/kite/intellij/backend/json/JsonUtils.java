package com.kite.intellij.backend.json;

import com.google.gson.JsonPrimitive;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class JsonUtils {
    @Nullable
    public static String nullableString(@Nullable JsonPrimitive value) {
        return value == null || value.isJsonNull() ? null : StringUtils.trimToNull(value.getAsString());
    }

    @Nonnull
    public static String string(@Nullable JsonPrimitive value) {
        return value.getAsString();
    }
}
