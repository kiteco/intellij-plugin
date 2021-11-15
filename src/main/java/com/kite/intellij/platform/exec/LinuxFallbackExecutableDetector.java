package com.kite.intellij.platform.exec;

import com.google.common.collect.Lists;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class LinuxFallbackExecutableDetector extends PathFallbackExecutableDetector {
    public LinuxFallbackExecutableDetector() {
        super("kited", "kitedEnterprise");
    }

    @Override
    public List<Path> detectKiteExecutableFiles() {
        Path homePath = Paths.get(System.getProperty("user.home"), ".local/share/kite/kited");
        Path systemPath = Paths.get("/opt/kite/kited");
        for (Path p : Lists.newArrayList(homePath, systemPath)) {
            if (Files.isExecutable(p)) {
                return Collections.singletonList(p);
            }
        }

        return super.detectKiteExecutableFiles();
    }
}
