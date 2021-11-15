package com.kite.intellij.backend.model;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * {"id":42,"name":"Firstname Lastname","email":"address","bio":"","email_verified":true,"is_internal":true,"unsubscribed":false}
 */
public class UserInfo {
    @Nonnull
    private final String id;
    private final String name;
    private final String email;
    private final String bio;
    private final boolean emailVerified;
    private final boolean isInternal;
    private final boolean unsubscribed;

    public UserInfo(@Nonnull String id, String name, String email, String bio, boolean emailVerified, boolean isInternal, boolean unsubscribed) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.emailVerified = emailVerified;
        this.isInternal = isInternal;
        this.unsubscribed = unsubscribed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, bio, emailVerified, isInternal, unsubscribed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserInfo userInfo = (UserInfo) o;
        return emailVerified == userInfo.emailVerified &&
                isInternal == userInfo.isInternal &&
                unsubscribed == userInfo.unsubscribed &&
                Objects.equals(id, userInfo.id) &&
                Objects.equals(name, userInfo.name) &&
                Objects.equals(email, userInfo.email) &&
                Objects.equals(bio, userInfo.bio);
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", bio='" + bio + '\'' +
                ", emailVerified=" + emailVerified +
                ", isInternal=" + isInternal +
                ", unsubscribed=" + unsubscribed +
                '}';
    }

    @Nonnull
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getBio() {
        return bio;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isInternal() {
        return isInternal;
    }

    public boolean isUnsubscribed() {
        return unsubscribed;
    }
}
