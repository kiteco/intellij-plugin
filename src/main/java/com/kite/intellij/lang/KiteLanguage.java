package com.kite.intellij.lang;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

public enum KiteLanguage {
    Python, Golang, JavaScript, JSX, Vue;

    private static final Set<String> EXT_GO = Collections.singleton("go");
    private static final Set<String> EXT_PY = Collections.unmodifiableSet(Sets.newHashSet("py", "pyw"));
    private static final Set<String> EXT_JS = Collections.unmodifiableSet(Sets.newHashSet("js"));
    private static final Set<String> EXT_JSX = Collections.unmodifiableSet(Sets.newHashSet("jsx"));
    private static final Set<String> EXT_VUE = Collections.unmodifiableSet(Sets.newHashSet("vue"));

    @Nullable
    public static KiteLanguage fromKiteName(String name) {
        for (KiteLanguage lang : KiteLanguage.values()) {
            if (lang.asKiteName().equals(name)) {
                return lang;
            }
        }
        return null;
    }

    /**
     * @param extension THe file extension without a dot, e.g. jsx
     * @return The Kite language, which is associated with the extension
     */
    @Nullable
    public static KiteLanguage findByExtension(String extension) {
        for (KiteLanguage language : KiteLanguage.values()) {
            if (language.getExtensions().contains(extension)) {
                return language;
            }
        }
        return null;
    }

    @NotNull
    public Set<String> getExtensions() {
        switch (this) {
            case Golang:
                return EXT_GO;
            case Python:
                return EXT_PY;
            case JavaScript:
                return EXT_JS;
            case JSX:
                return EXT_JSX;
            case Vue:
                return EXT_VUE;
            default:
                throw new IllegalStateException("unexpected value " + this);
        }
    }

    public String asKiteName() {
        switch (this) {
            case Golang:
                return "go";
            case Python:
                return "python";
            case JavaScript:
                return "javascript";
            case JSX:
                return "jsx";
            case Vue:
                return "vue";
            default:
                throw new IllegalStateException("unexpected value " + this);
        }
    }

    public String jsonName() {
        return asKiteName();
    }

    @NotNull
    public static KiteLanguage fromJson(String name) {
        KiteLanguage language = fromKiteName(name);
        if (language == null) {
            throw new IllegalStateException("unknown language " + name);
        }
        return language;
    }
}
