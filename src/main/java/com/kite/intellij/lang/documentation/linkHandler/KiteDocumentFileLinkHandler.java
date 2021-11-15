package com.kite.intellij.lang.documentation.linkHandler;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.ex.FileEditorProviderManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.kite.intellij.lang.documentation.KiteDocumentationRenderer;
import com.kite.intellij.lang.documentation.KiteLinkHandlerUtils;
import org.apache.http.client.utils.URIBuilder;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Optional;

/**
 * Handles links to source files with a line number.
 *
  */
public class KiteDocumentFileLinkHandler implements LinkHandler<DocumentFileLinkData, VirtualFile> {
    public KiteDocumentFileLinkHandler() {
    }

    @Override
    public boolean supportsLink(@Nonnull String link) {
        return link.startsWith("file:/");
    }

    @Override
    public boolean supports(KiteLinkData linkData) {
        return linkData instanceof DocumentFileLinkData;
    }

    @Override
    public String asLink(@Nonnull DocumentFileLinkData linkData) {
        URIBuilder uriBuilder = new URIBuilder().setScheme("file").setPath(linkData.getFile());

        linkData.getLine().ifPresent(l -> uriBuilder.addParameter("line", String.valueOf(l)));

        return uriBuilder.toString();
    }

    @Override
    public DocumentFileLinkData createLinkData(@Nonnull String link) {
        URI uri = URI.create(link);
        int line = Integer.valueOf(KiteLinkHandlerUtils.computeQueryParams(link).getOrDefault("line", "0"));

        return new DocumentFileLinkData(uri.getPath(), line);
    }

    @Override
    public Optional<VirtualFile> rawResponseData(DocumentFileLinkData linkData, LinkRenderContext renderContext) {
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            return Optional.empty();
        }

        return ApplicationManager.getApplication().runReadAction((Computable<Optional<VirtualFile>>) () -> {
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(linkData.getFile());
            return Optional.ofNullable(file);
        });
    }

    @Override
    public Optional<String> render(Optional<VirtualFile> file, @Nonnull DocumentFileLinkData linkData, LinkRenderContext renderContext, KiteDocumentationRenderer renderer) {
        return Optional.empty();
    }

    @Override
    public void postRender(String link, LinkRenderContext renderContext) {
        DocumentFileLinkData linkData = createLinkData(link);
        Optional<VirtualFile> fileOpt = rawResponseData(linkData, renderContext);
        if (!fileOpt.isPresent()) {
            return;
        }

        VirtualFile file = fileOpt.get();

        FileEditorProvider[] providers = FileEditorProviderManager.getInstance().getProviders(renderContext.getProject(), file);
        if (providers.length > 0) {
            //configured line at column 0, the logical line passed to the descriptor seems to start at 0
            OpenFileDescriptor descriptor = new OpenFileDescriptor(renderContext.getProject(), file, linkData.getLine().orElse(1) - 1, 0);
            FileEditorManager.getInstance(renderContext.getProject()).openTextEditor(descriptor, true);
        } else {
            Messages.showErrorDialog(renderContext.getProject(), "The file could not be found on your system.", "Can't Open File");
        }
    }
}
