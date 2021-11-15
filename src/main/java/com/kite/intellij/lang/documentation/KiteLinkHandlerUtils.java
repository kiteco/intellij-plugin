package com.kite.intellij.lang.documentation;

import com.intellij.openapi.project.Project;
import com.kite.intellij.util.KiteBrowserUtil;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import javax.annotation.Nonnull;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KiteLinkHandlerUtils {
    @Nonnull
    public static Map<String, String> computeQueryParams(URI uri) {
        Map<String, String> paramMap = Collections.emptyMap();
        if (uri.getRawQuery() != null) {
            List<NameValuePair> queryParams = URLEncodedUtils.parse(uri.getRawQuery(), StandardCharsets.UTF_8);
            paramMap = queryParams.stream().collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
        }
        return paramMap;
    }

    @Nonnull
    public static Map<String, String> computeQueryParams(String link) {
        return computeQueryParams(URI.create(link));
    }

    public static void openExternalUrl(String url, Project project) {
        KiteBrowserUtil.browse(url);
    }
}
