package com.kite.testrunner.actions;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory.Context;
import com.kite.intellij.test.KiteTestUtils;
import com.kite.testrunner.TestContext;
import com.kite.testrunner.TestFailedException;
import com.kite.testrunner.model.TestStep;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class OpenFileAction implements TestAction {
    @Override
    public String getId() {
        return "open";
    }

    @Override
    public void run(TestContext context) throws Throwable {
        try {
            TestStep step = context.getStep();
            String filePath = step.getStringProperty("file", null);
            String data = fileContent(context, step);
            boolean focus = step.getBooleanProperty("focus", true);

            VirtualFile virtualFile;
            try {
                virtualFile = context.getFixture().getTempDirFixture().createFile(filePath, data);
            } catch (AssertionError e) {
                // file probably exists already
                virtualFile = context.getFixture().findFileInTempDir(filePath);
            }

            PsiFile psiFile = PsiManager.getInstance(context.getProject()).findFile(virtualFile);
            if (psiFile == null) {
                throw new IllegalStateException("PSIFile not found for " + filePath);
            }

            context.getFixture().openFileInEditor(virtualFile);
            if (focus) {
                KiteTestUtils.emulateFocusEvent(psiFile);
            }

            CanonicalFilePathFactory factory = CanonicalFilePathFactory.getInstance();
            CanonicalFilePath canonicalFilePath = factory.createFor(psiFile, Context.AnyFile);
            Assert.assertNotNull(canonicalFilePath);

            context.putContextProperty(String.format("editors.%s.filename", filePath), canonicalFilePath.asOSDelimitedPath());
            context.putContextProperty(String.format("editors.%s.filename_escaped", filePath), canonicalFilePath.asKiteEncodedPath());
            context.putContextProperty(String.format("editors.%s.offset", filePath), "0");
            context.putContextProperty(String.format("editors.%s.hash", filePath), DigestUtils.md5Hex(psiFile.getText().getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new TestFailedException(context, "error while loading data", e);
        }
    }

    protected String fileContent(TestContext context, TestStep step) throws IOException {
        return loadFile(step, context);
    }
}
