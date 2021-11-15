package com.kite.intellij.lang.documentation;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

/**
 * Contains a copy of the python color definitions to avoid a hard dependency on the Python plugin in this source folder.
 *
  */
class KitePyHighlighter {
    static final TextAttributesKey PY_KEYWORD_ARGUMENT = TextAttributesKey.createTextAttributesKey("PY.KEYWORD_ARGUMENT", DefaultLanguageHighlighterColors.PARAMETER);
    static final TextAttributesKey PY_OPERATION_SIGN = TextAttributesKey.createTextAttributesKey("PY.OPERATION_SIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    static final TextAttributesKey PY_BUILTIN_NAME = TextAttributesKey.createTextAttributesKey("PY.BUILTIN_NAME", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL);
    static final TextAttributesKey PY_BRACKETS = TextAttributesKey.createTextAttributesKey("PY.BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
}
