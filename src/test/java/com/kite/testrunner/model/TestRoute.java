package com.kite.testrunner.model;

import com.kite.intellij.backend.http.HttpStatusException;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.testrunner.TestContext;
import com.kite.testrunner.TestRunnerUtil;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestRoute {
    public String match;
    public RouteResponse response;

    public boolean matchesRequest(TestContext context, String path, Map<String, String> queryParams, @Nullable String body) {
        List<NameValuePair> pairs = queryParams.entrySet()
                .stream()
                .map(e -> new BasicNameValuePair(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        String url = match;
        if (!pairs.isEmpty()) {
            path += "?" + URLEncodedUtils.format(pairs, StandardCharsets.UTF_8);
        }

        String urlRegexp = TestRunnerUtil.resolvePlaceholders(url, context);
        return Pattern.compile(urlRegexp).matcher(path).matches();
    }

    public void applyTo(TestContext context, MockKiteHttpConnection http) {
        http.addGenericRequestHandler((method, path, queryParams, body) -> {
            if (matchesRequest(context, path, queryParams, body)) {
                String respBody = response.loadBody(context);
                if (!response.isStatus200()) {
                    throw new HttpStatusException("route response data", response.status, respBody);
                }
                return respBody;
            }
            throw new HttpStatusException("not supported", HttpStatus.SC_NOT_FOUND, null, null, true);
        }, context.getTestRootDisposable());
    }
}
