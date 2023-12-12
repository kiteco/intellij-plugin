package com.kite.testrunner;

import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.psi.PsiDocumentManager;
import com.kite.intellij.editor.events.TestcaseEditorEventListener;
import com.kite.testrunner.model.TestStep;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.ide.PooledThreadExecutor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("unchecked")
public interface TestStepHelper {

    @Nonnull
    default String loadFile(TestStep step, TestContext context) throws IOException {
        Path path = resolveFile(step, context);
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Nonnull
    default String loadFile(String relativePath, TestContext context) {
        try {
            Path path = context.getRootPath().resolve(relativePath);
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new TestFailedException(context, "unable to load file " + relativePath);
        }
    }

    @Nonnull
    default Path resolveFile(TestStep step, TestContext context) {
        String filename = step.getStringProperty("file", null);
        String[] parts = StringUtils.split(filename, '/');
        if (parts.length == 1) {
            return context.getRootPath().resolve(filename);
        }
        return context.getRootPath().resolve(Paths.get(parts[0], Arrays.copyOfRange(parts, 1, parts.length)));
    }


    @Nonnull
    default Map<String, Object> resolvePlaceholders(@Nonnull Map<String, Object> value, TestContext context) {
        // copy to avoid ConcurrentModificationException
        List<Map.Entry<String, Object>> keys = Lists.newArrayList(value.entrySet());

        for (Map.Entry<String, Object> e: keys) {
            Object v = e.getValue();
            if (v instanceof String) {
                value.put(e.getKey(), TestRunnerUtil.resolvePlaceholders((String) e.getValue(), context));
            } else if (v instanceof Map<?, ?>) {
                resolvePlaceholders((Map<String, Object>) e.getValue(), context);
            }
        }

        return value;
    }

}
