package com.kite.testrunner.model;

import com.kite.testrunner.TestContext;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class RouteResponse {
    public Integer status;
    public String body;

    public boolean isStatus200() {
        return status == null || status == 200;
    }

    @Nullable
    public String loadBody(TestContext context) {
        Path path = context.getRootPath().resolve(body);
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }
}
