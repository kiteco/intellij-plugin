package com.kite.intellij.lang.documentation;

import com.kite.intellij.backend.model.Calls;
import com.kite.intellij.lang.documentation.linkHandler.SignatureLinkData;

import javax.annotation.Nonnull;
import java.util.OptionalInt;

/**
 * Generic interface to allow different implementations of the documentation rendering.
 *
  */
public interface KiteDocumentationRenderer {
    /**
     * @param calls
     * @param linkData
     * @param fontSizeOverride If present then this value will be used as the page's base font size
     * @return
     */
    @Nonnull
    String render(@Nonnull Calls calls, @Nonnull SignatureLinkData linkData, @Nonnull OptionalInt fontSizeOverride);

    /**
     * Renders static content in the basic html template.
     *
     * @param content
     * @param fontSizeOverride
     * @return
     */
    String renderStaticContent(String content, @Nonnull OptionalInt fontSizeOverride);
}
