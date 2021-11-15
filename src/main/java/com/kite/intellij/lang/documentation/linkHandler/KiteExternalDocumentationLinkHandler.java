package com.kite.intellij.lang.documentation.linkHandler;

import com.kite.intellij.backend.WebappLinks;
import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.lang.documentation.KiteDocumentationRenderer;
import com.kite.intellij.lang.documentation.KiteLinkHandlerUtils;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Optional;

/**
 * Handles links to external Kite documentantion.
 *
  */
public class KiteExternalDocumentationLinkHandler implements LinkHandler<ExternalDocumentationLinkData, Void> {

    public KiteExternalDocumentationLinkHandler() {
    }

    @Override
    public boolean supportsLink(@Nonnull String link) {
        return link.startsWith(LINK_SCHEME + ":/externalDocs");
    }

    @Override
    public boolean supports(KiteLinkData linkData) {
        return linkData instanceof ExternalDocumentationLinkData;
    }

    @Override
    public String asLink(@Nonnull ExternalDocumentationLinkData linkData) {
        return createBaseURI().setPath("/externalDocs")
                .addParameter("id", linkData.getId())
                .toString();
    }

    @Override
    public ExternalDocumentationLinkData createLinkData(@Nonnull String link) {
        String id = KiteLinkHandlerUtils.computeQueryParams(URI.create(link)).get("id");
        return new ExternalDocumentationLinkData(id);
    }

    @Override
    public Optional<Void> rawResponseData(ExternalDocumentationLinkData linkData, LinkRenderContext renderContext) throws KiteHttpException {
        return Optional.empty();
    }

    @Override
    public Optional<String> render(Optional<Void> data, @Nonnull ExternalDocumentationLinkData linkData, LinkRenderContext renderContext, KiteDocumentationRenderer renderer) {
        KiteLinkHandlerUtils.openExternalUrl(WebappLinks.getInstance().symbolDocs(linkData.getId()), renderContext.getProject());

        return Optional.empty();
    }
}
