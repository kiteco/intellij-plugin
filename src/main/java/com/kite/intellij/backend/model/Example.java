package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 */
public class Example implements WithId {
    public static final Example[] EMPTY = new Example[0];

    @Nonnull
    private final Id id;
    private final String title;

    public Example(@Nonnull Id id, String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Example example = (Example) o;
        return Objects.equals(id, example.id) &&
                Objects.equals(title, example.title);
    }

    @Override
    public String toString() {
        return "Example{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }

    @Nonnull
    @Override
    public Id getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
