package com.kite.intellij.util;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import javax.annotation.Nonnull;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * Utility functions to work with {@link java.net.URI}s and strings representing URIs.
 */
public class URIUtils {
    @Nonnull
    public static Optional<String> getQueryParamValue(String uri, @Nonnull String param) {
        try {
            return new URIBuilder(uri).getQueryParams().stream().filter(pair -> pair.getName().equals(param)).map(NameValuePair::getValue).findFirst();
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }
}
