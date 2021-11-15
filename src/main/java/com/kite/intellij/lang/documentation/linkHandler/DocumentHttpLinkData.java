package com.kite.intellij.lang.documentation.linkHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class DocumentHttpLinkData implements KiteLinkData {
    private final String httpLink;

    public DocumentHttpLinkData(@Nonnull String httpLink) {
        this.httpLink = validateLink(httpLink);
    }

    public String getHttpLink() {
        return httpLink;
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpLink);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DocumentHttpLinkData that = (DocumentHttpLinkData) o;
        return Objects.equals(httpLink, that.httpLink);
    }

    @Override
    public String toString() {
        return "DocumentHttpLinkData{" +
                "httpLink='" + httpLink + '\'' +
                '}';
    }

    private static String validateLink(String httpLink) {
        if (!httpLink.startsWith("http://") && !httpLink.startsWith("https://") && !httpLink.startsWith("kite://")) {
            throw new IllegalArgumentException("Invalid link " + httpLink);
        }
        return httpLink;
    }
}
