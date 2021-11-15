package com.kite.intellij.lang.documentation.linkHandler;

import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.lang.documentation.KiteDocumentationRenderer;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.OptionalInt;

public class KiteEmptyContentLinkHandler implements LinkHandler<EmptyContentLinkData, String> {

    @Override
    public boolean supportsLink(@Nonnull String link) {
        return link.equals(LINK_SCHEME + "://empty");
    }

    @Override
    public boolean supports(KiteLinkData linkData) {
        return linkData instanceof EmptyContentLinkData;
    }

    @Override
    public String asLink(@Nonnull EmptyContentLinkData linkData) {
        return createBaseURI().setPath("/empty").toString();
    }

    @Override
    public EmptyContentLinkData createLinkData(@Nonnull String link) {
        return new EmptyContentLinkData();
    }

    @Override
    public Optional<String> rawResponseData(EmptyContentLinkData linkData, LinkRenderContext renderContext) throws KiteHttpException {
        return Optional.of("No documentation found.");
    }

    @Override
    public Optional<String> render(Optional<String> data, @Nonnull EmptyContentLinkData linkData, LinkRenderContext renderContext, KiteDocumentationRenderer renderer) {
        return data.map(s -> renderer.renderStaticContent(s, OptionalInt.empty()));
    }
}
