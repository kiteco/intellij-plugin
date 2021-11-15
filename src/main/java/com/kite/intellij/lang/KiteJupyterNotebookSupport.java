package com.kite.intellij.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * This extension point must not depend on classes provided by the Python plugin.
 * It's enabled in 'kite-jupyter.xml' and only loaded when Jupyter notebook support is available, i.e. only
 * when PyCharm professional is running.
 * Jupyter Notebook support adds support for the .ipynb file extension. These files are send to kite
 * as .py files.
 * <p>
 * PyCharm is managing notebooks as .ipynb files, which are Python source code files internally. Therefore we're sending
 * .ipynb files as .py files to Kite. Kite has no special support for .ipynb, yet.
 * PyCharm is taking care to map the cell definitions into python code and it's also taking care of completion
 * offset handling, etc.
 */
public class KiteJupyterNotebookSupport implements LangSupportEP {
    @Override
    public boolean supportsFileExtension(@NotNull String ext) {
        return "ipynb".equals(ext);
    }

    @Override
    @Nullable
    public String patchFilename(@NotNull String filename) {
        if (!filename.endsWith(".ipynb")) {
            return null;
        }

        int lastSeparator = filename.lastIndexOf(File.separatorChar);
        if (lastSeparator <= 0) {
            return filename.substring(0, filename.length() - 6) + ".py";
        }

        String path = filename.substring(0, lastSeparator);
        String name = filename.substring(lastSeparator + 1);
        return path + File.separatorChar + ".virtual_documents" + File.separatorChar + name.substring(0, name.length() - 6) + ".py";
    }

    @Override
    public boolean supportsFeature(@NotNull KiteLanguageSupport.Feature feature) {
        return true;
    }
}
