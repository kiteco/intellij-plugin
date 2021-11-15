package com.kite.intellij.lang.documentation.linkHandler;

import com.kite.intellij.lang.documentation.KiteDocumentationRenderer;
import com.kite.intellij.util.KiteBrowserUtil;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Handles http and https links embedded in the rendered content.
 *
  */
public class KiteDocumentHttpLinkHandler implements LinkHandler<DocumentHttpLinkData, DocumentHttpLinkData> {
    public KiteDocumentHttpLinkHandler() {
    }

    @Override
    public boolean supportsLink(@Nonnull String link) {
        return link.startsWith("http://") || link.startsWith("https://") || link.startsWith("kite://");
    }

    @Override
    public boolean supports(KiteLinkData linkData) {
        return linkData instanceof DocumentHttpLinkData;
    }

    @Override
    public String asLink(@Nonnull DocumentHttpLinkData linkData) {
        return linkData.getHttpLink();
    }

    @Override
    public DocumentHttpLinkData createLinkData(@Nonnull String link) {
        return new DocumentHttpLinkData(link);
    }

    @Override
    public Optional<DocumentHttpLinkData> rawResponseData(DocumentHttpLinkData linkData, LinkRenderContext renderContext) {
        return Optional.of(linkData);
    }

    @Override
    public Optional<String> render(Optional<DocumentHttpLinkData> data, @Nonnull DocumentHttpLinkData linkData, LinkRenderContext renderContext, KiteDocumentationRenderer renderer) {
        KiteBrowserUtil.browse(linkData.getHttpLink());

        return Optional.empty();
    }
}
