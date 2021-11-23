package com.kite.intellij.test;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.kite.intellij.editor.events.DefaultEditorEventListener;
import com.kite.intellij.editor.events.EditorEventListener;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
import org.jsoup.parser.XmlTreeBuilder;
import org.junit.Assert;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

/**
 *
 */
public class KiteTestUtils {
    public static String getTestDataRoot() {
        try {
            URL url = KiteTestUtils.class.getResource("/test-data-marker.txt");

            File dir = new File(url.toURI());
            do {
                dir = dir.getParentFile();
            } while (dir != null && !new File(dir, "build.gradle").exists());

            if (dir == null || !dir.exists() || !dir.isDirectory()) {
                throw new IllegalStateException("Expected test-data dir not found");
            }

            return new File(dir, "test-data").getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("Error locating test-data root", e);
        }
    }

    public static String getSourceDataRoot() {
        String testDataRoot = getTestDataRoot();
        return new File(new File(testDataRoot).getParentFile(), "src").getAbsolutePath();
    }

    /**
     * Loads a test data located under the test-data root directory.
     *
     * @param filePath The relative file path, /-delimted
     * @return The contents of the file
     * @throws IOException if the file wasn't found
     */
    public static String loadTestDataFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file = new File(getTestDataRoot(), filePath.replace('/', File.separatorChar));
        }

        return StreamUtil.readTextFrom(new FileReader(file));
    }

    /**
     * Returns the pretty-printed body content.
     *
     * @param html The html to process, may be a full html structure
     * @return The html contained in the body tag of the pretty-printed html document
     */
    public static String prettyPrintHtmlBody(String html) {
        Parser parser = new Parser(new XmlTreeBuilder());
        parser.setTrackErrors(10);
        parser.settings(new ParseSettings(false, false));

        Document htmlDocument = parser.parseInput(html, "http://www.example.com");
        if (!parser.getErrors().isEmpty()) {
            throw new IllegalStateException("html parsing errors: " + parser.getErrors());
        }

        Document.OutputSettings outputSettings = new Document.OutputSettings()
                .prettyPrint(true)
                .syntax(Document.OutputSettings.Syntax.xml);
        htmlDocument.outputSettings(outputSettings);

        //condense spaces, remove spaces at line ends
        Element bodyHtml = htmlDocument.body().hasText() ? htmlDocument.body() : htmlDocument;
        return trimHtmlWhitespace(bodyHtml.html());
    }

    /**
     * Updates a file in the test path with new content.
     *
     * @param filePath The path to the file
     * @param content  The new content to write into the file
     */
    public static void saveTestDataFile(String filePath, String content) throws IOException {
        String root = getTestDataRoot();
        File file = new File(root, filePath.replace('/', File.separatorChar));
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("File doesn't exist or is not a file: " + file.getAbsolutePath());
        }

        FileUtil.writeToFile(file, content);
    }

    /**
     * This system property can be used to override the file content of the expected.html files
     * with the current rendering result.
     * This is useful to avoid to manually change all expected.html files if the current rendering is the new reference rendering
     */
    public static boolean isOverridingReferenceRenderings() {
        return "true".equals(System.getProperty("kite.testCase.updateReferenceRendering"));
    }

    /**
     * IntelliJ suppresses focus event listeners in unit testing mode (see FileEditorManagerImpl.java below the comment // Transfer focus into editor)
     * We emulate the focus event to let the events be the same as in production mode.
     *
     * @param filePath  The path in the test-data directory
     * @param myFixture
     */
    public static void configureByFileAndFocus(String filePath, CodeInsightTestFixture myFixture) {
        PsiFile psiFile = myFixture.configureByFile(filePath);
        Assert.assertNotNull(psiFile);

        emulateFocusEvent(psiFile);
    }

    public static void emulateFocusEvent(PsiFile psiFile) {
        Project project = psiFile.getProject();
        Editor selectedTextEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        ((DefaultEditorEventListener) EditorEventListener.getInstance(project)).fileFocused(selectedTextEditor, psiFile.getVirtualFile());
    }

    public static void emulatedFrameActivation(Project project) {
        ((DefaultEditorEventListener) EditorEventListener.getInstance(project)).onFrameActivated();
    }

    private static String trimHtmlWhitespace(String prettyHtml) {
        return prettyHtml
                .replaceAll("[ ]+", " ")
                .replaceAll("\\s*(\\r\\n|\\n|\\r)+\\s*", "\n")
                .replaceAll(" <span", "<span")
                .replace("<html>\n<body></body>\n</html>", "")
                .trim();
    }
}
