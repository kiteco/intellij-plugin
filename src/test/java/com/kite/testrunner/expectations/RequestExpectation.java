package com.kite.testrunner.expectations;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.kite.intellij.backend.http.test.MockKiteHttpConnection;
import com.kite.intellij.backend.http.test.RequestInfo;
import com.kite.testrunner.TestContext;
import com.kite.testrunner.TestFailedException;
import com.kite.testrunner.TestRunnerUtil;
import com.kite.testrunner.model.TestStep;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestExpectation implements TestExpectation, RetryableExpectation {
    @Override
    public String getId() {
        return "request";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(TestContext context) {
        TestStep step = context.getStep();

        String method = step.getStringProperty("method", null);
        String path = TestRunnerUtil.resolvePlaceholders(step.getStringProperty("path", null), context);

        String bodyJson;
        Object body = step.properties.get("body");
        if (body == null) {
            bodyJson = null;
        } else if (body instanceof String) {
            // string? -> filename with JSON content
            String content = loadFile((String) body, context);
            // parse json -> replace placeholders -> back to json to let Gson handle the escaping issues
            Map<String, Object> parsed = resolvePlaceholders(context.getGson().fromJson(content, Map.class), context);
            bodyJson = context.getGson().toJson(parsed);
        } else if (body instanceof Map<?, ?>) {
            // object? -> inlined JSON
            bodyJson = context.getGson().toJson(resolvePlaceholders((Map<String, Object>) body, context));
        } else {
            throw new TestFailedException(context, "Unexpected body value: " + body);
        }

        List<RequestInfo> history = MockKiteHttpConnection.getInstance().getHttpRequestHistory();
        LinkedList<RequestInfo> matching = history.stream()
                .filter(p -> method.equals(p.getMethod()))
                .filter(p -> path.equals(p.getPathWithQuery()))
                .collect(Collectors.toCollection(LinkedList::new));

        if (matching.isEmpty()) {
            throw new TestFailedException(context, String.format("Expected request not found: %s %s. Existing requests:\n%s\n", method, path, Joiner.on('\n').join(history)));
        }

        // compare bodies
        List<Map<String, Map<String, Object>>> diff = bodyDiff(context, bodyJson, matching.getLast());
        if (!diff.isEmpty()) {
            throw new TestFailedException(context, "Request bodies do not match. Diff:\n" + context.getGson().toJson(diff));
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Map<String, Object>>> bodyDiff(TestContext context, String expectedJSON, RequestInfo actualRequest) {
        List<Map<String, Map<String, Object>>> diff = Lists.newArrayList();

        String body = actualRequest.getBody();
        if (StringUtils.isEmpty(expectedJSON)) {
            if (StringUtils.isEmpty(body)) {
                return diff;
            }

            diff.add(Collections.singletonMap("body", diffEntry("", body)));
            return diff;
        }

        Gson gson = context.getGson();
        Map<String, Object> expected = gson.fromJson(expectedJSON, Map.class);
        Map<String, Object> actual = body.isEmpty() ? Maps.newHashMap() : gson.fromJson(body, Map.class);

        //special handling for filenames, tests contain relative filenames which use / as delimiter
//        actual.put("filename", actual.get("filename"));
//        expected.put("filename", expected.get("filename"));

        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            Object actualValue = actual.getOrDefault(entry.getKey(), "");
            if (!entry.getValue().equals(actualValue)) {
                // HACK: this plugin sometimes sends a a selection event instead of an edit event (kited is fine with that)
                // to not raise an error for this
                if ("action".equals(entry.getKey()) && "selection".equals(actualValue) && "edit".equals(entry.getValue())) {
                    continue;
                }

                diff.add(Collections.singletonMap(entry.getKey(), diffEntry(entry.getValue(), actualValue)));
            }
        }

        return diff;
    }

    private Map<String, Object> diffEntry(Object expected, Object actual) {
        Map<String, Object> diff = Maps.newLinkedHashMap();
        diff.put("expected", expected);
        diff.put("actual", actual);
        return diff;
    }
}
