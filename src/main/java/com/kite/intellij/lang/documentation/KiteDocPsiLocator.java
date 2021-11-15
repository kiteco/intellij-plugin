package com.kite.intellij.lang.documentation;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Extension point to define per-language PSI element lookup if documentation was requested in a file.
 *
  */
public interface KiteDocPsiLocator {
    ExtensionPointName<KiteDocPsiLocator> EP_NAME = ExtensionPointName.create("com.kite.intellij.kiteDocPsiLocator");

    /**
     * @param file
     * @return {@code true} if the current file is supported by this implementation
     */
    boolean supports(@Nonnull PsiFile file);

    /**
     * @param editor         The editor which displays the current file
     * @param file           The current file
     * @param contextElement The context element at the offset the user used
     * @return Returns the element which should be passed on to Kite for the documentation lookup. If no different PsiElement should be used instead of contextElement then {@code null} is returned. If a non-null value is returned then it will be used instead of the original context element.
     */
    @Nullable
    PsiElement findElement(@Nonnull Editor editor, @Nonnull PsiFile file, @Nonnull PsiElement contextElement);

    @Nullable
    PsiElement findArgumentList(PsiFile file, int offset);
}
