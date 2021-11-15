package com.kite.testrunner.model;

import com.kite.testrunner.TestContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class TestStep {
    public String description;
    public String step;
    public String type;
    public Map<String, Object> properties;

    @Nullable
    public <T> T parseProperties(TestContext context, Class<T> type) {
        String json = context.getGson().toJson(properties);
        return context.getGson().fromJson(json, type);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public List<String> getStringListProperty(String name, List<String> defaultValue) {
        List<String> value = (List<String>) properties.getOrDefault(name, defaultValue);
        if (value == null) {
            throw new IllegalStateException(String.format("String array property %s not found", name));
        }
        return value;
    }

    @Nonnull
    public String getStringProperty(String name, String defaultValue) {
        String value = (String) properties.getOrDefault(name, defaultValue);
        if (value == null) {
            throw new IllegalStateException(String.format("String property %s not found", name));
        }
        return value;
    }

    public int getIntProperty(String name, Integer defaultValue) {
        Number value = (Number) properties.getOrDefault(name, defaultValue);
        if (value == null) {
            throw new IllegalStateException("Integer value not found for key " + name);
        }
        return value.intValue();
    }

    public boolean getBooleanProperty(String name, Boolean defaultValue) {
        Boolean value = (Boolean) properties.getOrDefault(name, defaultValue);
        if (value == null) {
            throw new IllegalStateException("Boolean value not found for key " + name);
        }
        return value;
    }

    @Override
    public String toString() {
        return "TestStep{" +
                "description='" + description + '\'' +
                ", step='" + step + '\'' +
                ", type='" + type + '\'' +
                ", properties=" + properties +
                '}';
    }
}
