package com.kite.intellij.backend.model;

import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class KiteCompletion {
    public static final KiteCompletion[] EMPTY = new KiteCompletion[0];

    @Nonnull
    private final String display;
    @Nonnull
    private final CompletionSnippet snippet;
    private final CompletionRange replace;
    private final KiteCompletion[] children;
    @Nullable
    private final String hint;
    private final String documentationText;
    @Nullable
    private final String webID;
    @Nullable
    private final String localID;
    private final boolean smart;

    public KiteCompletion(@Nonnull String display, @Nonnull CompletionSnippet snippet, @Nullable String hint, String documentationText, CompletionRange replace, boolean smart, @Nullable String webID, @Nullable String localID, KiteCompletion[] children) {
        this.display = display;
        this.snippet = snippet;
        this.hint = hint;
        this.documentationText = documentationText;
        this.replace = replace;
        this.smart = smart;
        this.webID = webID;
        this.localID = localID;
        this.children = children == null ? EMPTY : children;
    }

    public boolean hasPlaceholders() {
        return snippet.getPlaceholders().length > 0;
    }

    public List<KiteCompletion> getChildren() {
        return Lists.newArrayList(children);
    }

    public String getInsert() {
        return snippet.getText();
    }

    @Nonnull
    public String getDisplay() {
        return display;
    }

    @Nullable
    public String getHint() {
        return hint;
    }

    public String getDocumentation() {
        return documentationText;
    }

    public boolean isSmart() {
        return smart;
    }

    public String getDocumentationText() {
        return documentationText;
    }

    public CompletionRange getReplace() {
        return replace;
    }

    @Nonnull
    public CompletionSnippet getSnippet() {
        return snippet;
    }

    @Nullable
    public String getWebID() {
        return webID;
    }

    @Nullable
    public String getLocalID() {
        return localID;
    }

    public boolean isKitePro() {
        return smart;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(display, snippet, replace, hint, documentationText, smart, webID, localID);
        result = 31 * result + Arrays.hashCode(children);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KiteCompletion that = (KiteCompletion) o;
        return display.equals(that.display) &&
                snippet.equals(that.snippet) &&
                Objects.equals(replace, that.replace) &&
                Arrays.equals(children, that.children) &&
                Objects.equals(hint, that.hint) &&
                Objects.equals(smart, that.smart) &&
                Objects.equals(webID, that.webID) &&
                Objects.equals(localID, that.localID) &&
                Objects.equals(documentationText, that.documentationText);
    }

    @Override
    public String toString() {
        return "CompletionSuggestion{" +
                "display='" + display + '\'' +
                ", snippet=" + snippet +
                ", replace=" + replace +
                ", smart='" + smart + '\'' +
                ", children=" + Arrays.toString(children) +
                ", hint='" + hint + '\'' +
                ", webID='" + webID + '\'' +
                ", localID='" + localID + '\'' +
                ", documentationText='" + documentationText + '\'' +
                '}';
    }
}
