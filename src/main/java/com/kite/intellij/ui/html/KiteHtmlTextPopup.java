package com.kite.intellij.ui.html;

import com.google.common.collect.Lists;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.kite.intellij.lang.documentation.KiteDocumentationRenderer;
import com.kite.intellij.lang.documentation.LinksHandlers;
import com.kite.intellij.lang.documentation.linkHandler.KiteLinkData;
import com.kite.intellij.lang.documentation.linkHandler.LinkHandler;
import com.kite.intellij.lang.documentation.linkHandler.LinkRenderContext;
import com.kite.intellij.ui.KiteThemeUtil;
import com.kite.intellij.ui.SwingWorkerUtil;
import com.kite.monitoring.TimeTracker;
import com.kite.monitoring.TimerTrackers;
import org.jetbrains.annotations.NotNull;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.LinkListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Defines a popup which is displayed in its own, borderless window above an editor.
 * It contains an html widget to display text and additional options to go back and forward in the history.
 * <p>
 * It handles the link found in the content.
 */
public class KiteHtmlTextPopup extends JPanel implements Disposable {
    private static final Logger LOG = Logger.getInstance("#kite.uil.htmlPopup");
    private static final int VIEWPORT_BORDER_WIDTH = 2;

    protected final KiteDocumentationRenderer renderer;
    private final List<KiteContentUpdateListener> contentUpdateListeners = Lists.newCopyOnWriteArrayList();

    private final Editor editor;
    @Nullable
    private final DocumentBuilder docBuilder;

    private KiteXHTMLPanel htmlPanel;
    private JComponent contentPanel;
    private volatile boolean disposed;

    public KiteHtmlTextPopup(KiteDocumentationRenderer renderer, Editor editor, @Nullable DocumentBuilder docBuilder, @NotNull Disposable parentDisposable) {
        super(new BorderLayout());

        this.renderer = renderer;
        this.editor = editor;
        this.docBuilder = docBuilder;

        Disposer.register(parentDisposable, this);
    }

    public void init(boolean alwaysShowVerticalScrollbar) {
        htmlPanel = createHtmlComponent();
        contentPanel = createContentPanel(htmlPanel, alwaysShowVerticalScrollbar);
        add(contentPanel);

        Color panelBackground = KiteThemeUtil.getDocPanelBackground();
        setBackground(panelBackground);
        htmlPanel.setBackground(panelBackground);
        contentPanel.setBackground(panelBackground);

        LinkHandler[] linkHandlers = LinksHandlers.all();
        setupHtmlListeners(editor, linkHandlers, htmlPanel);
    }

    /**
     * @return The height of the rendered html document with the current width and the layout borders added to it
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public Dimension computeOptimalDimension(@Nonnull int[] widthSizes) {
        int layoutWidth;
        int layoutHeight;

        int viewportBorderWidth = VIEWPORT_BORDER_WIDTH;
        int viewportBorderHeight = 0;

        //test all widths and take the first one where the height didn't grow
        //we test all because several dimensions may result in the same rendering height, but with the next dimension
        //resulting in a smaller height.
        Dimension minSize = Arrays.stream(widthSizes).mapToObj(width -> {
            KiteXHTMLPanel panel = this.htmlPanel;
            if (panel == null) {
                // happens when disposed while computing dimensions
                return null;
            }

            setSize(new Dimension(JBUI.scale(width) + viewportBorderWidth, getMinimumSize().height));
            panel.setSize(JBUI.scale(width), getMinimumSize().height - viewportBorderWidth);
            revalidate();
            doLayout();
            panel.doDocumentLayout(getGraphics());

            return new Dimension(panel.getPreferredSize().width, panel.getPreferredSize().height);
        }).filter(Objects::nonNull).min(Comparator.comparingInt(d -> d.height)).orElseGet(this::getMinimumSize);

        //add the borders to the html layout height to avoid scrollbars
        layoutWidth = minSize.width + viewportBorderWidth;
        layoutHeight = minSize.height;

        if (layoutHeight > 0) {
            //add the borders to the html layout height to avoid scrollbars
            layoutHeight += viewportBorderHeight;
        }

        return new Dimension(layoutWidth, layoutHeight);
    }

    public void setXHTML(String xhtml) {
        if (disposed) {
            return;
        }

        htmlPanel.setXHTML(xhtml, docBuilder);
    }

    @Override
    public synchronized void dispose() {
        if (!disposed) {
            disposed = true;

            // shouldn't happen, but could be possible if the parent component is closed while the popup is shown
            // setVisible() must be called on the AWT thread, https://github.com/kiteco/intellij-plugin-private/issues/582
            if (ApplicationManager.getApplication() != null && ApplicationManager.getApplication().isDispatchThread()) {
                setVisible(false);
            }

            contentPanel = null;
            htmlPanel = null;

            contentUpdateListeners.clear();
        }
    }

    public boolean isDisposed() {
        return disposed;
    }

    public KiteDocumentationRenderer getRenderer() {
        return renderer;
    }

    public synchronized void registerContentUpdateListener(KiteContentUpdateListener listener) {
        contentUpdateListeners.add(listener);
    }

    public synchronized void removeContentUpdateListener(KiteContentUpdateListener listener) {
        contentUpdateListeners.remove(listener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!TimerTrackers.isEnabled()) {
            super.paintComponent(g);
            return;
        }

        try (TimeTracker ignored = TimerTrackers.start(String.format("text popup painting (%d x %d)", getWidth(), getHeight()))) {
            super.paintComponent(g);
        }
    }

    @Override
    public void requestFocus() {
        contentPanel.requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
        if (isDisposed()) {
            return false;
        }
        return contentPanel.requestFocusInWindow();
    }

    @Nonnull
    protected KiteXHTMLPanel createHtmlComponent() {
        return new KiteXHTMLPanel();
    }

    protected void setupHtmlListeners(Editor editor, LinkHandler[] linkHandlers, KiteXHTMLPanel htmlPanel) {
        LinkListener listener = new LinkListener() {
            @Override
            public void linkClicked(BasicPanel panel, String uri) {
                Optional<LinkHandler> linkHandler = LinksHandlers.findMatchingLinkHandler(uri, linkHandlers);
                if (!linkHandler.isPresent()) {
                    return;
                }

                LinkRenderContext renderContext = LinkRenderContext.create(editor);
                Project project = editor.getProject();

                SwingWorkerUtil.compute(
                        () -> RenderUtil.renderHtml(uri, linkHandler, renderContext, renderer, project),
                        (htmlOpt) -> {
                            if (htmlOpt == null) {
                                //timeout errors shouldn't trigger an error reporter, warn for now
                                LOG.warn(String.format("Error while rendering content for '%s'", uri));
                                return;
                            }

                            linkHandler.ifPresent(lh -> lh.postRender(uri, renderContext));

                            //if the rendering failed or this widget is disposed hide it
                            if (!htmlOpt.isPresent() || disposed) {
                                return;
                            }

                            setXHTML(htmlOpt.get());

                            //the links are either #id links included in the kite report docs or links generated by our rendering
                            KiteLinkData linkData = linkHandler.get().createLinkData(uri);

                            //notify all registered listeners
                            synchronized (KiteHtmlTextPopup.this) {
                                contentUpdateListeners.forEach(listener -> listener.contentUpdated(linkData, linkHandler.get()));
                            }
                        }
                );
            }
        };

        htmlPanel.addMouseTrackingListener(listener);
        Disposer.register(this, () -> htmlPanel.removeMouseTrackingListener(listener));
    }

    /**
     * Creates the content panel which will contain all other panels (the html panel, action buttons, etc).
     * The returned component should be a {@link JBScrollPane}.
     *
     * @param htmlPanel                   The html panel to include in the main content panel
     * @param alwaysShowVerticalScrollbar
     * @return The content panel
     */
    @Nonnull
    protected JComponent createContentPanel(KiteXHTMLPanel htmlPanel, boolean alwaysShowVerticalScrollbar) {
        JBScrollPane scrollPane = new JBScrollPane(VIEWPORT_BORDER_WIDTH);
        scrollPane.setViewportView(htmlPanel);
        scrollPane.setMinimumSize(JBUI.size(30, 30));
        scrollPane.setFocusable(true);
        scrollPane.setBorder(null);
        if (alwaysShowVerticalScrollbar) {
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        }

        return scrollPane;
    }
}
