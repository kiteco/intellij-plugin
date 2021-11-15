package com.kite.intellij.lang.documentation.linkHandler;

public class LinksLinkData implements KiteLinkData {
    private final String id;
    private final String label;
    private final int length;

    public LinksLinkData(String id, String label, int length) {
        this.id = id;
        this.label = label;
        this.length = length;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + length;
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

        LinksLinkData that = (LinksLinkData) o;

        if (length != that.length) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        return label != null ? label.equals(that.label) : that.label == null;
    }

    @Override
    public String toString() {
        return "LinksLinkData{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", length=" + length +
                '}';
    }

    public String getLabel() {

        return label;
    }

    public String getId() {
        return id;
    }

    public int getLength() {
        return length;
    }
}
