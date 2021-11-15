package com.kite.intellij.lang;

import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class KiteJupyterNotebookSupportTest {
    @Test
    public void patchFilename() {
        KiteJupyterNotebookSupport support = new KiteJupyterNotebookSupport();
        assertEquals("file.py", support.patchFilename("file.ipynb"));

        assertEquals(Paths.get("parent", "child", ".virtual_documents", "file.py").toString(),
                support.patchFilename(Paths.get("parent", "child", "file.ipynb").toString()));

        assertNull(support.patchFilename(Paths.get("parent", "child", "file.py").toString()));
    }
}