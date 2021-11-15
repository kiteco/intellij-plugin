package com.kite.intellij.lang.documentation;

import com.google.common.collect.Lists;
import com.intellij.openapi.util.io.FileUtil;
import com.kite.intellij.test.KiteTestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This test makes sure that all attributes in the pebble templates are escaped.
 * We have to make sure that all html attributes which contain dynamic content are properly escaped.
 */
public class PebbleEscapeTest {
    @Test
    public void testTemplateEscapes() throws Exception {
        String sourceRoot = KiteTestUtils.getSourceDataRoot();
        File templateRoot = new File(sourceRoot, "main/resources/templates/python");

        List<String> errorFiles = Lists.newArrayList();
        for (File file : FileUtil.findFilesByMask(Pattern.compile(".*\\.peb$"), templateRoot)) {
            String content = FileUtil.loadFile(file);

            Matcher matcher = Pattern.compile("=\"([^\"]*\\{\\{[^\"]+}}[^\"]*)\"").matcher(content);
            while (matcher.find()) {
                String match = matcher.group(1);
                if (!match.contains("escape(\"html_attr\")")) {
                    errorFiles.add(file.getName() + ": " + match);
                }
            }
        }

        Assert.assertTrue("All attribute values must be escaped:\n" + errorFiles.stream().collect(Collectors.joining("\n")), errorFiles.isEmpty());
    }
}
