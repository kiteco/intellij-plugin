package com.kite.intellij.backend.json.deserializer.endpoint;

import com.google.gson.*;
import com.kite.intellij.backend.model.Example;
import com.kite.intellij.backend.model.Location;
import com.kite.intellij.backend.model.Report;
import com.kite.intellij.backend.model.Usage;

import java.lang.reflect.Type;

/**
 * definition: {
 * filename: STRING
 * line: INT
 * }
 * description_text: STRING   // full documentation
 * description_html: STRING
 * examples: [EXAMPLE, EXAMPLE, ...]
 * usages: [USAGE, USAGE, ...]
 */
public class ReportDeserializer implements JsonDeserializer<Report> {
    @Override
    public Report deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        String descriptionText = json.getAsJsonPrimitive("description_text").getAsString();
        String descriptionHtml = json.getAsJsonPrimitive("description_html").getAsString();
        Location definition = context.deserialize(json.get("definition"), Location.class);
        Example[] examples = context.deserialize(json.get("examples"), Example[].class);
        Usage[] usages = context.deserialize(json.get("usages"), Usage[].class);
        int totalUsages = json.has("total_usages") ? json.getAsJsonPrimitive("total_usages").getAsInt() : 0;

        return new Report(definition, descriptionText, descriptionHtml, examples, usages, totalUsages);
    }
}
