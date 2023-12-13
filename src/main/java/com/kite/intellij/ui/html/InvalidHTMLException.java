package com.kite.intellij.ui.html;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class InvalidHTMLException extends RuntimeException {
    public InvalidHTMLException(@Nullable String id, @Nullable String html, @Nullable Exception cause) {
        super("Unsupported HTML found. ID: " + StringUtils.trimToEmpty(id) + ", HTML: \n" + StringUtils.trimToEmpty(html), cause);
    }
}
