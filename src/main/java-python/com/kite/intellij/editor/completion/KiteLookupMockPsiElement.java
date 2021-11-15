package com.kite.intellij.editor.completion;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.PythonLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

class KiteLookupMockPsiElement extends UserDataHolderBase implements PsiElement, Navigatable {
    @NotNull
    private final String id;
    @NotNull
    private final String documentation;
    @NotNull
    private final PsiManager manager;

    KiteLookupMockPsiElement(@NotNull String id, @NotNull String documentation, @NotNull PsiManager manager) {
        this.id = id;
        this.documentation = documentation;
        this.manager = manager;
    }

    public String getDocSnippet() {
        return documentation;
    }

    // mock implementation follows

    @Override
    public void navigate(boolean requestFocus) {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public boolean canNavigate() {
        return false;
    }

    @Override
    public boolean canNavigateToSource() {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    @Nullable
    public <T> T getCopyableUserData(@NotNull final Key<T> key) {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public <T> void putCopyableUserData(@NotNull final Key<T> key, final T value) {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    @NotNull
    public Project getProject() {
        return manager.getProject();
    }

    @Override
    @NotNull
    public Language getLanguage() {
        return PythonLanguage.getInstance();
    }

    @NotNull
    @Override
    public PsiManager getManager() {
        return manager;
    }

    @Override
    @NotNull
    public PsiElement[] getChildren() {
        return EMPTY_ARRAY;
    }

    @Override
    public PsiElement getParent() {
        return null;
    }

    @Override
    @Nullable
    public PsiElement getFirstChild() {
        return null;
    }

    @Override
    @Nullable
    public PsiElement getLastChild() {
        return null;
    }

    @Override
    @Nullable
    public PsiElement getNextSibling() {
        return null;
    }

    @Override
    @Nullable
    public PsiElement getPrevSibling() {
        return null;
    }

    @Override
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return null;
    }

    @Override
    public TextRange getTextRange() {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public int getStartOffsetInParent() {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public int getTextLength() {
        return 0;
    }

    @Override
    @Nullable
    public PsiElement findElementAt(final int offset) {
        return null;
    }

    @Override
    @Nullable
    public PsiReference findReferenceAt(final int offset) {
        return null;
    }

    @Override
    public int getTextOffset() {
        return 0;
    }

    @Override
    @NonNls
    public String getText() {
        return "";
    }

    @Override
    @NotNull
    public char[] textToCharArray() {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public PsiElement getNavigationElement() {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public PsiElement getOriginalElement() {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public boolean textMatches(@NotNull final CharSequence text) {
        return false;
    }

    @Override
    public boolean textMatches(@NotNull final PsiElement element) {
        return false;
    }

    @Override
    public boolean textContains(final char c) {
        return false;
    }

    @Override
    public void accept(@NotNull final PsiElementVisitor visitor) {
    }

    @Override
    public void acceptChildren(@NotNull final PsiElementVisitor visitor) {
    }

    @Override
    public PsiElement copy() {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public PsiElement add(@NotNull final PsiElement element) throws IncorrectOperationException {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public PsiElement addBefore(@NotNull final PsiElement element, final PsiElement anchor) throws IncorrectOperationException {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public PsiElement addAfter(@NotNull final PsiElement element, final PsiElement anchor) throws IncorrectOperationException {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    @Deprecated
    public void checkAdd(@NotNull final PsiElement element) throws IncorrectOperationException {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public PsiElement addRange(final PsiElement first, final PsiElement last) throws IncorrectOperationException {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public PsiElement addRangeBefore(@NotNull final PsiElement first, @NotNull final PsiElement last, final PsiElement anchor) throws
            IncorrectOperationException {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public PsiElement addRangeAfter(final PsiElement first, final PsiElement last, final PsiElement anchor) throws IncorrectOperationException {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public void delete() throws IncorrectOperationException {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    @Deprecated
    public void checkDelete() throws IncorrectOperationException {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public void deleteChildRange(final PsiElement first, final PsiElement last) throws IncorrectOperationException {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public PsiElement replace(@NotNull final PsiElement newElement) throws IncorrectOperationException {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    @Nullable
    public PsiReference getReference() {
        return null;
    }

    @Override
    @NotNull
    public PsiReference[] getReferences() {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    public boolean processDeclarations(@NotNull final PsiScopeProcessor processor,
                                       @NotNull final ResolveState state, final PsiElement lastParent, @NotNull final PsiElement place) {
        return true;
    }

    @Override
    @Nullable
    public PsiElement getContext() {
        return null;
    }

    @Override
    public boolean isPhysical() {
        return false;
    }

    @Override
    @NotNull
    public GlobalSearchScope getResolveScope() {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    @NotNull
    public SearchScope getUseScope() {
        throw new UnsupportedOperationException("Method not yet implemented in " + getClass().getName());
    }

    @Override
    @Nullable
    public ASTNode getNode() {
        return null;
    }

    @Override
    public boolean isEquivalentTo(final PsiElement another) {
        return this == another;
    }

    @Override
    public Icon getIcon(final int flags) {
        return null;
    }

    @NotNull
    public String getId() {
        return id;
    }
}
