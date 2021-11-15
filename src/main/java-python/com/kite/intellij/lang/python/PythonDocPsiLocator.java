package com.kite.intellij.lang.python;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.*;
import com.kite.intellij.lang.documentation.KiteDocPsiLocator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Supports the documentation elements of the PythonLanguage.
 *
  */
public class PythonDocPsiLocator implements KiteDocPsiLocator {
    @Override
    public boolean supports(@Nonnull PsiFile file) {
        return file.getLanguage() instanceof PythonLanguage;
    }

    @Nullable
    @Override
    public PsiElement findElement(@Nonnull Editor editor, @Nonnull PsiFile file, @Nonnull PsiElement contextElement) {
        if (contextElement instanceof LeafPsiElement) {
            //happens for foo(123<caret>) for example. The context element is the closing parenthesis in this case
            //we want to lookup the 123 literal, though
            PsiElement prev = contextElement.getPrevSibling();

            if (prev == null && contextElement.getTextOffset() > 0) {
                //for "def x<caret>()" the left paren is selected by default, this leaf psi element has no previous sibling in the tree
                prev = file.findElementAt(contextElement.getTextOffset() - 1);
            }

            //x in "def x()" is a leaf, the parent is the whole function. We have to check the element type in this case
            if (prev != null) {
                if (prev instanceof PyTypedElement) {
                    return contextElement;
                }

                if (prev.getNode().getElementType() == PyTokenTypes.IDENTIFIER) {
                    return prev;
                }
            }
        }

        return null;
    }

    /**
     * Locates the argument list of a method call  which spans the offset.
     *
     * @param file   The file to look at
     * @param offset The offset to locate the call expression
     * @return The PSIElement representing the call expression.
     */
    @Nullable
    @Override
    public PsiElement findArgumentList(PsiFile file, int offset) {
        return findArgumentListWithoutFallback(file, offset);
    }

    @Nullable
    private static PsiElement findArgumentListWithoutFallback(PsiFile file, int offset) {
        PsiElement element = file.findElementAt(offset);

        if (offset == file.getTextLength() && element == null && offset >= 1) {
            element = file.findElementAt(offset - 1);
        }

        if (element == null) {
            return null;
        }

        if (element instanceof LeafPsiElement && ((LeafPsiElement) element).getElementType() == PyTokenTypes.RPAR) {
            element = element.getPrevSibling();
        }

        PyCallExpression parent = PsiTreeUtil.getParentOfType(element, PyCallExpression.class);
        if (parent != null) {
            PyArgumentList argumentList = parent.getArgumentList();
            if (argumentList != null) {
                return argumentList;
            }
        }

        //special handling of the print statement, which is not a function call in PyCharm's PSI
        PyPrintStatement printStatement = PsiTreeUtil.getParentOfType(element, PyPrintStatement.class, true, PyCallExpression.class);
        if (printStatement != null) {
            PsiElement callExpression = PsiTreeUtil.getChildOfAnyType(printStatement, PyParenthesizedExpression.class);
            if (callExpression != null) {
                return callExpression;
            }

            return PsiTreeUtil.getChildOfAnyType(printStatement, PyTupleExpression.class);
        }
        return null;
    }
}
