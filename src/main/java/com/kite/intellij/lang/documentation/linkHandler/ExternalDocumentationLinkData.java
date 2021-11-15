package com.kite.intellij.lang.documentation.linkHandler;

import java.util.Objects;

public class ExternalDocumentationLinkData implements KiteLinkData {
    private final String id;

    public ExternalDocumentationLinkData(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExternalDocumentationLinkData linkData = (ExternalDocumentationLinkData) o;
        return Objects.equals(id, linkData.id);
    }

    @Override
    public String toString() {
        return "ExternalDocumentationLinkData{" +
                "id='" + id + '\'' +
                '}';
    }
}
