package com.kite.intellij.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

public class DebugUtil {
    public static String currentStackTrace() {
        try {
            throw new IllegalStateException();
        } catch (IllegalStateException e) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(out);
            e.printStackTrace(writer);
            writer.flush();

            return out.toString();
        }
    }
}
