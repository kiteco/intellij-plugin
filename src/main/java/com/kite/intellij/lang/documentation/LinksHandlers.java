package com.kite.intellij.lang.documentation;

import com.kite.intellij.lang.documentation.linkHandler.KiteLinkData;
import com.kite.intellij.lang.documentation.linkHandler.LinkHandler;

import java.util.Arrays;
import java.util.Optional;

public class LinksHandlers {
    /**
     * Returns the first link handlers which supports the given link.
     *
     * @param link         The link to support
     * @param linkHandlers The set of available link handlers
     * @return The optional first handler supporting the link
     */
    public static Optional<LinkHandler> findMatchingLinkHandler(String link, LinkHandler[] linkHandlers) {
        return Arrays.stream(linkHandlers).filter(handler -> handler.supportsLink(link)).findFirst();
    }

    @SuppressWarnings("unchecked")
    public static <T extends LinkHandler> Optional<T> findMatchingLinkHandler(Class<T> linkhandlerType, LinkHandler[] linkHandlers) {
        return Arrays.stream(linkHandlers)
                .filter(linkhandlerType::isInstance)
                .map(handler -> (T) handler)
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    public static <T extends KiteLinkData> Optional<LinkHandler> findMachingLinkHandler(T linkData) {
        return Arrays.stream(all())
                .filter(h -> h.supports(linkData))
                .findAny()
                .map(h -> (LinkHandler<T, ?>) h);
    }

    @SuppressWarnings("unchecked")
    public static String asLink(KiteLinkData linkData) {
        Optional<String> result = findMachingLinkHandler(linkData).map(h -> h.asLink(linkData));
        if (!result.isPresent()) {
            throw new IllegalStateException("Could not find link handler for " + linkData.getClass().getName());
        }

        return result.get();
    }

    public static LinkHandler[] all() {
        return LinkHandler.EP_NAME.getExtensions();
    }
}
