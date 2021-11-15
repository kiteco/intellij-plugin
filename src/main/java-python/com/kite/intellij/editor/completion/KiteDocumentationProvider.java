package com.kite.intellij.editor.completion;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Provides documentation for {@link KiteLookupElement} and {@link KiteLookupElement}.
 * It uses a mock PSIElement to achieve this.
 *
  */
public class KiteDocumentationProvider implements DocumentationProvider {
    @Nullable
    @Override
    public String getQuickNavigateInfo(PsiElement psiElement, PsiElement psiElement1) {
        return null;
    }

    @Nullable
    @Override
    public List<String> getUrlFor(PsiElement psiElement, PsiElement psiElement1) {
        return null;
    }

    @Nullable
    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (element instanceof KiteLookupMockPsiElement) {
            String doc = StringUtils.trimToEmpty(((KiteLookupMockPsiElement) element).getDocSnippet());
            if (doc.isEmpty()) {
                return "No documentation available";
            }

            String id = ((KiteLookupMockPsiElement) element).getId();
            if (!id.isEmpty()) {
                return doc + String.format("&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"%s\">More ›</a>", "kite://docs/" + id);
            }

            return doc;
        }
        return null;
    }

    @Nullable
    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object item, PsiElement psiElement) {
        String documentation = null;
        String id = "";
        if (item instanceof KiteLookupElement) {
            documentation = ((KiteLookupElement) item).getDocumentation();
            id = StringUtils.trimToEmpty(((KiteLookupElement) item).getID());
        }

        return documentation == null ? null : new KiteLookupMockPsiElement(id, documentation, psiManager);
    }

    @Nullable
    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String s, PsiElement psiElement) {
        return null;
    }
}
