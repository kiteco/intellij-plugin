package com.kite.intellij.editor.completion;

import com.google.common.collect.Sets;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.kite.intellij.backend.KiteApiService;
import com.kite.intellij.backend.http.HttpTimeoutConfig;
import com.kite.intellij.backend.http.KiteHttpException;
import com.kite.intellij.backend.model.KiteCompletion;
import com.kite.intellij.backend.response.KiteCompletions;
import com.kite.intellij.editor.util.FileEditorUtil;
import com.kite.intellij.platform.fs.CanonicalFilePath;
import com.kite.intellij.platform.fs.CanonicalFilePathFactory;
import com.kite.intellij.settings.KiteSettingsService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.ide.PooledThreadExecutor;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.kite.intellij.KiteConstants.COMPLETION_WAIT_TIMESLICE_MILLIS;

/**
 * Adds kite completions. If kite returned completions then no further completions will be added.
 */
public class KiteCompletionProvider extends CompletionProvider<CompletionParameters> implements DumbAware {
    private static final Logger LOG = Logger.getInstance("#kite.completions");

    public KiteCompletionProvider() {
    }

    @Override
    protected void addCompletions(@Nonnull CompletionParameters parameters, @NotNull ProcessingContext context, @Nonnull CompletionResultSet result) {
        if (!KiteSettingsService.getInstance().getState().codeCompletionEnabled) {
            return;
        }

        // KiteCompletionContributor guarantees, that the current file is supported
        PsiFile originalFile = parameters.getOriginalFile();
        VirtualFile virtualFile = originalFile.getVirtualFile();
        if (virtualFile == null) {
            return;
        }

        CanonicalFilePath canonicalFilePath = CanonicalFilePathFactory.getInstance().createForSupported(virtualFile);
        if (canonicalFilePath == null) {
            return;
        }

        // we're not showing completion items with placeholders if the editor is already displaying placeholders
        // this is a workaround until we implement nesting of placeholders
        boolean removePlaceholders;
        try {
            Template template = TemplateManager.getInstance(originalFile.getProject()).getActiveTemplate(parameters.getEditor());
            removePlaceholders = template != null && template.getSegmentsCount() >= 2;
        } catch (Exception e) {
            // make sure that we don't break completions for this workaround
            removePlaceholders = false;
        }

        KiteCompletions completions;
        try {
            completions = fetchKiteCompletions(canonicalFilePath, parameters);
        } catch (InterruptedException | KiteHttpException e) {
            // do nothing and fallback to the other completion contributors
            if (LOG.isDebugEnabled()) {
                LOG.debug("Timeout while waiting for an empty kite event queue for completion request", e);
            }
            return;
        }

        ProgressManager.checkCanceled();

        if (completions == null) {
            result.addLookupAdvertisement("Kite completions are currently unavailable");
            return;
        } else if (completions.isEmpty()) {
            // nothing to do if Kite has no completions to dedupe or rank
            return;
        }

        //fixme This should be done only if we know that Kite has more matches for the current completion
        result.restartCompletionOnPrefixChange(StandardPatterns.string());

        KiteCompletion[] kiteCompletions = completions.getItems();
        Set<KiteCompletion> addedKiteItems = Sets.newLinkedHashSetWithExpectedSize(kiteCompletions.length);

        // don't autocomplete placeholders
        // https://github.com/kiteco/kiteco/issues/11202
        final boolean autocompletePlaceholders = false;

        CompletionResultSet kiteWeighedResult = result.withRelevanceSorter(
                CompletionSorter.emptySorter().weigh(new KiteElementWeigher())
        );

        int weight = Integer.MAX_VALUE;
        for (KiteCompletion item : kiteCompletions) {
            if (parameters.getCompletionType() == CompletionType.SMART && !item.isSmart()) {
                // we're not displaying Kite's non-smart completions in smart mode
                continue;
            }

            if (!removePlaceholders || !item.hasPlaceholders()) {
                addCompletionItem(kiteWeighedResult, item, new KiteLookupElement(item, 0, autocompletePlaceholders, weight), parameters, weight);
                weight--;
                addedKiteItems.add(item);
            }

            // add nested elements, if present
            for (KiteCompletion nested : item.getChildren()) {
                if (removePlaceholders && nested.hasPlaceholders()) {
                    continue;
                }

                addCompletionItem(kiteWeighedResult, item, new KiteLookupElement(nested, 1, autocompletePlaceholders, weight), parameters, weight);
                weight--;
                addedKiteItems.add(nested);
            }
        }

        // Insert deduplicated completions of the remaining completion contributors
        addNonDuplicateItems(parameters, result, addedKiteItems);
    }

    /**
     * Add the lookup element to the completion result.
     * Takes care of Kite's replacement range.
     * <p>
     * The custom prefix matcher takes care of the text left of the caret.
     * The lookupElement's handleInsert takes care of the text right to the caret.
     *
     * @param result
     * @param item
     * @param lookupElement
     * @param parameters
     * @param weight
     */
    private void addCompletionItem(@Nonnull CompletionResultSet result, @Nonnull KiteCompletion item, @Nonnull KiteLookupElement lookupElement, @Nonnull CompletionParameters parameters, int weight) {
        int caretOffset = parameters.getOffset();

        int prefixStart = item.getReplace().getBegin();
        if (!ApplicationManager.getApplication().isUnitTestMode()) {
            assert caretOffset >= prefixStart;
        } else if (caretOffset < prefixStart) {
            // this is usually incomplete JSON test data
            return;
        }

        int replacementEnd = item.getReplace().getEnd();
        if (caretOffset > replacementEnd) {
            throw new AssertionError(String.format("%d <= %d", caretOffset, replacementEnd));
        }

        TextRange prefixRange = TextRange.create(prefixStart, caretOffset);
        String replacedPrefix = parameters.getEditor().getDocument().getText(prefixRange);

        TextRange suffixRangeBeforeInsert = TextRange.create(caretOffset, replacementEnd);
        String replacedSuffix = parameters.getEditor().getDocument().getText(suffixRangeBeforeInsert);
        lookupElement.setSuffixToRemove(replacedSuffix);

        LookupElement prioritized = PrioritizedLookupElement.withPriority(lookupElement, weight);
        result.withPrefixMatcher(replacedPrefix).addElement(prioritized);
    }

    /**
     * Add all completions by the other contributors which do not duplicate the already inserted items
     *
     * @param params
     * @param result
     * @param kiteCompletions
     */
    private void addNonDuplicateItems(CompletionParameters params, @Nonnull CompletionResultSet result, Set<KiteCompletion> kiteCompletions) {
        // prepare a set of cleaned-up items which is then used to properly filter duplicates
        // List of possible duplicates:
        //     PyCharm - Kite
        //         x() - x()
        //      x(foo) - x(foo)
        //      x(foo) - x(bar)
        //      x      - x
        //      x()    - x
        //      x(foo) - x

        Set<String> kiteStrings = Sets.newHashSet();
        for (KiteCompletion item : kiteCompletions) {
            kiteStrings.add(chopCompletion(item.getInsert()).trim());
            kiteStrings.add(chopCompletion(item.getDisplay()).trim());
        }

        // add items one-by-one to reduce delay in the UI
        result.runRemainingContributors(params, item -> {
            String lookupString = item.getLookupElement().getLookupString();
            String chopped = chopCompletion(lookupString).trim();

            if (!kiteStrings.contains(chopped)) {
                result.passResult(item);
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("filtered duplicate completion item " + lookupString);
            }
        }, true);
    }

    @NotNull
    private static String chopCompletion(@NotNull String completionString) {
        int i = completionString.indexOf("(");
        return i >= 1 ? completionString.substring(0, i) : completionString;
    }

    private KiteCompletions fetchKiteCompletions(@Nonnull CanonicalFilePath filePath, @Nonnull CompletionParameters parameters) throws InterruptedException, KiteHttpException {
        ProgressManager.checkCanceled();

        KiteApiService apiService = KiteApiService.getInstance();

        //the content must be retrieved in the current thread (the current call is wrapped in a read action opened by the completion framework)
        String text = FileEditorUtil.contentOf(parameters.getEditor());

        Integer endOffset;
        if (parameters.getEditor().getSelectionModel().hasSelection() && parameters.getEditor().getSelectionModel().getSelectionStart() == parameters.getOffset()) {
            // we're assuming a placeholder completion here
            endOffset = parameters.getEditor().getSelectionModel().getSelectionEnd();
        } else {
            endOffset = null;
        }

        final boolean useSnippets = KiteSettingsService.getInstance().getState().useNewCompletions;
        Future<KiteCompletions> responseFuture = PooledThreadExecutor.INSTANCE.submit(() ->
                apiService.completions(filePath,
                        text,
                        parameters.getOffset(),
                        endOffset,
                        !useSnippets,
                        HttpTimeoutConfig.ShortTimeout)
        );

        //wait for completion and check whether IntelliJ cancelled the completion call
        KiteCompletions response = null;
        do {
            try {
                ProgressManager.checkCanceled();

                response = responseFuture.get(COMPLETION_WAIT_TIMESLICE_MILLIS, TimeUnit.MILLISECONDS);
            } catch (ProcessCanceledException e) {
                // cancel HTTP request and rethrow on process cancelled
                responseFuture.cancel(true);
                throw e;
            } catch (InterruptedException | ExecutionException e) {
                return null;
            } catch (TimeoutException e) {
                //ignore, continue with the waiting if the Future.get call timed out
            }
        } while (!responseFuture.isDone() && !responseFuture.isCancelled());

        return response;
    }
}
