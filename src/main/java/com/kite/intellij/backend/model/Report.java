package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.Objects;

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
@Immutable
@ThreadSafe
public class Report {
    @Nullable
    private final Location definition;
    private final String descriptionText;
    private final String descriptionHtml;
    @Nonnull
    private final Example[] examples;
    @Nonnull
    private final Usage[] usages;
    private final int totalUsages;

    public Report(String descriptionText, String descriptionHtml) {
        this(null, descriptionText, descriptionHtml, null, null, 0);
    }

    public Report(@Nullable Location definition, String descriptionText, String descriptionHtml, @Nullable Example[] examples, @Nullable Usage[] usages, int totalUsages) {
        this.definition = definition;
        this.descriptionText = descriptionText;
        this.descriptionHtml = descriptionHtml;
        this.examples = (examples == null || examples.length == 0) ? Example.EMPTY : examples;
        this.usages = (usages == null || usages.length == 0) ? Usage.EMPTY : usages;
        this.totalUsages = totalUsages;
    }

    @Nullable
    public Location getDefinition() {
        return definition;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public Report withDescriptionText(String descriptionText) {
        return new Report(definition, descriptionText, descriptionHtml, examples, usages, totalUsages);
    }

    public String getDescriptionHtml() {
        return descriptionHtml;
    }

    public Report withDescriptionHtml(String descriptionHtml) {
        return new Report(definition, descriptionText, descriptionHtml, examples, usages, totalUsages);
    }

    @Nonnull
    public Example[] getExamples() {
        return examples;
    }

    public boolean hasExamples() {
        return examples.length > 0;
    }

    public int getTotalExamples() {
        return examples.length;
    }

    @Nonnull
    public Usage[] getUsages() {
        return usages;
    }

    public boolean hasUsages() {
        return usages.length > 0;
    }

    public int getTotalUsages() {
        return totalUsages;
    }

    @Override
    public int hashCode() {
        return Objects.hash(definition, descriptionText, descriptionHtml, examples, usages);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Report report = (Report) o;
        return Objects.equals(definition, report.definition) &&
                Objects.equals(descriptionText, report.descriptionText) &&
                Objects.equals(descriptionHtml, report.descriptionHtml) &&
                Arrays.equals(examples, report.examples) &&
                Arrays.equals(usages, report.usages) &&
                Objects.equals(totalUsages, report.totalUsages);
    }

    @Override
    public String toString() {
        return "Report{" +
                "definition=" + definition +
                ", descriptionText='" + descriptionText + '\'' +
                ", descriptionHtml='" + descriptionHtml + '\'' +
                ", examples=" + Arrays.toString(examples) +
                ", usages=" + Arrays.toString(usages) +
                ", totalUsages=" + totalUsages +
                '}';
    }
}
