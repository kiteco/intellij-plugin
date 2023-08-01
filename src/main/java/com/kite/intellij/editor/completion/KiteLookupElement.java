package com.kite.intellij.editor.completion;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.kite.intellij.Icons;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.http.HttpConnectionUnavailableException;
import com.kite.intellij.backend.http.HttpRequestFailedException;
import com.kite.intellij.backend.http.HttpTimeoutConfig;
import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.backend.model.CompletionRange;
import com.kite.intellij.backend.model.CompletionSnippet;
import com.kite.intellij.backend.model.KiteCompletion;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory;
import com.kite.intellij.util.PyCharmUtil;
import gnu.trove.THashSet;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

/**
 * Lookup presentation for Kite completion items.
 * <p>
 * It defines how a code completion item will appear in the user interface.
 *
  */
@SuppressWarnings("deprecation")
public class KiteLookupElement extends LookupElement {
    private static final Logger LOG = Logger.getInstance("#kite.completion");
    private final KiteCompletion kiteData;
    private final int nestingLevel;
    private final boolean autocompletePlaceholders;
    private final int weight;
    private final Set<String> lookupStrings;
    private String replacedSuffix;

    public KiteLookupElement(KiteCompletion kiteData, int nestingLevel, boolean autocompletePlaceholders, int weight) {
        this.kiteData = kiteData;
        this.nestingLevel = nestingLevel;
        this.autocompletePlaceholders = autocompletePlaceholders;
        this.weight = weight;

        // add an empty string as lookup string to force a prefix match, which contributes to sort order
        Set<String> lookupStrings = new THashSet<>(2);
        lookupStrings.add("");
        lookupStrings.add(kiteData.getSnippet().getText());
        this.lookupStrings = Collections.unmodifiableSet(lookupStrings);
    }

    public int getWeight() {
        return weight;
    }

    @Nonnull
    @Override
    public String getLookupString() {
        return kiteData.getSnippet().getText();
    }

    @Override
    public Set<String> getAllLookupStrings() {
        return this.lookupStrings;
    }

    @Override
    public void handleInsert(InsertionContext context) {
        Document doc = context.getDocument();

        // convert filepath to canonical
        CanonicalFilePathFactory filePathFactory = CanonicalFilePathFactory.getInstance();
        CanonicalFilePath canonicalFilePath = filePathFactory.createFor(context.getFile(), CanonicalFilePathFactory.Context.AnyFile);

        // report the insertion to the Kite Engine
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                KiteApiService.getInstance().completionSelected(canonicalFilePath, kiteData, HttpTimeoutConfig.DefaultTimeout);
            } catch (KiteHttpException e) {
                // Reporting the selection failed, but we should continue with the insertion.
            }
        });

        // IntelliJ inserts in different modes
        // 1: enter replaces prefix with the insert value, then handleInsert() is called
        // 2: tab replaces prefix+current token with the insert value, then handleInsert() is called
        // 3: comma, dot, etc. replace the prefix with the insert value, then handleInsert() is called,
        //    then the typed character is inserted at the current cursor position
        // fixme we're not yet support (3), because we don't know which range was replaced
        //   by IntelliJ before the completion was inserted.
        if (!replacedSuffix.isEmpty() && context.getCompletionChar() != Lookup.REPLACE_SELECT_CHAR) {
            int caretOffset = context.getEditor().getCaretModel().getOffset();

            TextRange suffixRange = TextRange.from(caretOffset, replacedSuffix.length());
            if (suffixRange.getEndOffset() <= doc.getTextLength()) {
                String actualSuffix = doc.getText(suffixRange);
                if (replacedSuffix.equals(actualSuffix)) {
                    doc.deleteString(suffixRange.getStartOffset(), suffixRange.getEndOffset());
                }
            }
        }

        // don't override the default inserted by PyCharm if we're not offering placeholders
        if (kiteData.getSnippet().getPlaceholders().length == 0) {
            return;
        }

        // disable placeholders in scientific mode
        // https://github.com/kiteco/intellij-plugin-private/issues/603
        if (PyCharmUtil.isScientificMode(context.getProject())) {
            return;
        }

        // to implement placeholders we're creating a temporary Live template
        // and execute it in place
        // The live template is filled with the plain text and placeholders
        // The variables are added in the same order as defined by Kite
        // to defined the iteration order to follow Kite's data

        // remove the completion inserted by PyCharm, we're re-inserting our own data
        TextRange ideRange = TextRange.create(context.getStartOffset(), context.getTailOffset());
        doc.deleteString(ideRange.getStartOffset(), ideRange.getEndOffset());
        context.setAddCompletionChar(false);

        // calc and insert our placeholders using a Live Template
        Template tmpl = createLiveTemplate();

        // remove the replaces range and insert the expanded live template
        CompletionRange replace = kiteData.getReplace();
        if (replace != null) {
            TextRange kiteRange = TextRange.create(replace.getBegin(), replace.getEnd());
            if (ideRange.intersects(kiteRange)) {
                // remove what still left after the removal above
                if (!ideRange.contains(kiteRange)) {
                    TextRange intersection = ideRange.intersection(kiteRange);
                    if (intersection != null) {
                        doc.deleteString(intersection.getStartOffset(), intersection.getEndOffset());
                    }
                }
            } else {
                doc.deleteString(replace.getBegin(), replace.getEnd());
            }
        }

        try {
            TemplateManager.getInstance(context.getProject()).startTemplate(context.getEditor(), tmpl);
        } catch (Exception e) {
            throw new RuntimeException(String.format("error handling startTemplate. suffix: %s, json: %s", replacedSuffix, kiteData.toString()), e);
        }
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        presentation.setItemText(StringUtils.repeat("  ", nestingLevel) + kiteData.getDisplay());
        presentation.setTypeText(kiteData.getHint(), null);
        presentation.setIcon(findIcon());
        presentation.setTypeGrayed(true);
    }

    public String getDocumentation() {
        return kiteData.getDocumentationText();
    }

    @Nullable
    public String getID() {
        return kiteData.getLocalID();
    }

    public void setSuffixToRemove(String replacedSuffix) {
        this.replacedSuffix = replacedSuffix;
    }

    @Nullable
    protected Icon findIcon() {
        String hint = kiteData.getHint();
        if (StringUtils.isEmpty(hint)) {
            // If no type hint is returned, use the non-overlay, full-sized icon.
            //
            // In particular, either all completions will have a hint or none will,
            // depending on the language (the latter happens for JS & Go)
            //
            // Thus there will be no visual inconsistency within the same Kite language.
            return Icons.KiteSmall;
        }

        switch (hint) {
            case "module":
                return Icons.KiteCompletionModule;

            case "function":
                return Icons.KiteCompletionFunction;

            case "instance":
                return Icons.KiteCompletionInstance;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Unable to locate completion icon for hint " + kiteData.getHint());
        }
        // default to the non-overlay, default kite icon
        return Icons.KiteCompletionFallback;
    }

    @NotNull
    private Template createLiveTemplate() {
        CompletionSnippet snippet = kiteData.getSnippet();

        // copy the list of placeholders and sort it
        CompletionRange[] sortedPlaceholders = Arrays.copyOf(snippet.getPlaceholders(), snippet.getPlaceholders().length);
        Arrays.sort(sortedPlaceholders, Comparator.comparingInt(CompletionRange::getBegin));

        // validate that there's not overlap
        for (int lastEnd = -1, i = 0; i < sortedPlaceholders.length; i++) {
            CompletionRange range = sortedPlaceholders[i];
            if (range.getBegin() < lastEnd) {
                throw new IllegalStateException("detected overlap of completion placeholders ranges");
            }
        }

        // iterate over the sorted placeholders
        String text = snippet.getText();
        TemplateImpl tmpl = new TemplateImpl("kite", "kite");
        tmpl.setToIndent(false);

        int lastEnd;
        int i;
        for (lastEnd = 0, i = 0; i < sortedPlaceholders.length; i++) {
            CompletionRange p = sortedPlaceholders[i];
            int begin = p.getBegin();
            int end = p.getEnd();
            // add text after last placeholder
            if (begin > lastEnd) {
                tmpl.addTextSegment(text.substring(lastEnd, begin));
            }

            tmpl.addVariableSegment(String.valueOf(i));
            lastEnd = end;
        }
        // add trailing text
        if (lastEnd < text.length()) {
            tmpl.addTextSegment(text.substring(lastEnd));
        }

        final String tmplVarExpr = this.autocompletePlaceholders ? "complete()" : null;

        // add placeholders in the order defined by Kite
        for (CompletionRange range : snippet.getPlaceholders()) {
            // name is derived from the position in the sorted placeholders
            int pos = -1;
            for (int n = 0; n < sortedPlaceholders.length; n++) {
                if (range.equals(sortedPlaceholders[n])) {
                    pos = n;
                    break;
                }
            }

            if (pos >= 0) {
                String value = text.substring(range.getBegin(), range.getEnd());
                tmpl.addVariable(String.valueOf(pos), tmplVarExpr, String.format("\"%s\"", value), true);
            }
        }

        return tmpl;
    }
}
