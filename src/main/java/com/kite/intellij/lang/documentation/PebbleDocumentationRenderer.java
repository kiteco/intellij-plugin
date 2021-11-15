package com.kite.intellij.lang.documentation;

import com.google.common.collect.Maps;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.*;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.ui.UIUtil;
import com.kite.intellij.KiteRuntimeInfo;
import com.kite.intellij.backend.model.Calls;
import com.kite.intellij.lang.documentation.linkHandler.KiteLinkData;
import com.kite.intellij.lang.documentation.linkHandler.SignatureLinkData;
import com.kite.intellij.ui.KiteThemeUtil;
import com.kite.monitoring.TimeTracker;
import com.kite.monitoring.TimerTrackers;
import com.kite.pebble.KitePebbleExtension;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Renderer implementation using Pebble as rendering engine.
 * <p>
 * If stores the last rendering in the file path defined by the system property 'kite.templateOutputPath.hover'.
 *
  */
public class PebbleDocumentationRenderer implements KiteDocumentationRenderer, Disposable  {
    private static final Logger LOG = Logger.getInstance("#kite.pebble");
    private static final String outputFilePath = System.getProperty("kite.templateOutputPath.hover");

    private final Loader<String> templateLoader;

    private volatile boolean disposed;
    private PebbleEngine cachedEngine;

    public PebbleDocumentationRenderer() {
        // support templates stored on disk to simplify the template development
        // if $HOME/intellij-templates is available, then the templates will be loaded from disk
        // $HOME/intellij-templates/python is expected to exist
        String filePrefixPath = System.getProperty("kite.templatePath", System.getProperty("user.home") + File.separator + "intellij-templates");

        Application application = ApplicationManager.getApplication(); //no injection, allow to run rendering in unit tests without an IntelliJ setup
        if (application != null && !application.isUnitTestMode() && filePrefixPath != null && new File(filePrefixPath).exists()) {
            FileLoader fileLoader = new FileLoader();
            fileLoader.setPrefix(filePrefixPath + File.separator + "python");

            templateLoader = fileLoader;
        } else {
            ClasspathLoader classpathLoader = new ClasspathLoader(getClass().getClassLoader());
            classpathLoader.setPrefix("templates/python");
            classpathLoader.setCharset("UTF-8");

            templateLoader = classpathLoader;
        }

        cachedEngine = createPebbleEngine();
    }

    @Nonnull
    @Override
    public String render(@Nonnull Calls calls, @Nonnull SignatureLinkData linkData, @Nonnull OptionalInt fontSizeOverride) {
        LOG.debug("Rendering calls response");

        try (TimeTracker ignored = TimerTrackers.start("signature rendering")) {
            Map<String, Object> context = Maps.newHashMap();
            context.put("calls", calls);
            context.put("context", linkData);
            context.put("bodyClass", "signatures");

            if (fontSizeOverride.isPresent()) {
                context.put("fontSize", fontSizeOverride.getAsInt());
            }

            return renderTemplate("signature/calls.peb", context);
        } catch (Exception e) {
            throw newRenderingFailedException("calls", linkData, e);
        }
    }

    @Override
    public String renderStaticContent(String content, @Nonnull OptionalInt fontSizeOverride) {
        LOG.debug("Rendering static content");

        try (TimeTracker ignored = TimerTrackers.start("static content")) {
            Map<String, Object> context = Maps.newHashMap();
            context.put("content", content);

            if (fontSizeOverride.isPresent()) {
                context.put("fontSize", fontSizeOverride.getAsInt());
            }

            return renderTemplate("common/staticContent.peb", context);
        } catch (Exception e) {
            throw newRenderingFailedException("static content", null, e);
        }
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            cachedEngine = null;
        }
    }

    @Nonnull
    private Map<String, Object> createContext(Map<String, Object> templateContext, EditorColorsScheme globalScheme) {
        boolean darkTheme = KiteThemeUtil.isDarkTheme(globalScheme);

        Map<String, Object> context = Maps.newLinkedHashMap();
        context.putAll(templateContext);

        //os info
        context.put("windows", SystemInfo.isWindows);
        context.put("mac", SystemInfo.isMac);
        context.put("linux", SystemInfo.isLinux);

        //configured theme
        context.put("dark", darkTheme);
        context.put("light", !darkTheme);

        Color panelBackground = KiteThemeUtil.getDocPanelBackground(globalScheme);
        RenderStyle defaultStyle = new RenderStyle(globalScheme.getDefaultForeground(), panelBackground, false, false, null);

        //colors
        context.put("textColor", defaultStyle.getForeground());
        context.put("bgColor", defaultStyle.getBackground());
        context.put("caretRowColor", globalScheme.getColor(EditorColors.CARET_ROW_COLOR));
        context.put("textStyle", createStyle(globalScheme, HighlighterColors.TEXT, defaultStyle));
        context.put("callStyle", createStyle(globalScheme, DefaultLanguageHighlighterColors.FUNCTION_CALL, defaultStyle));
        context.put("parenStyle", createStyle(globalScheme, DefaultLanguageHighlighterColors.PARENTHESES, defaultStyle));
        context.put("paramStyle", createStyle(globalScheme, DefaultLanguageHighlighterColors.PARAMETER, defaultStyle));
        context.put("commaStyle", createStyle(globalScheme, DefaultLanguageHighlighterColors.COMMA, defaultStyle));
        context.put("kwArgStyle", createStyle(globalScheme, KitePyHighlighter.PY_KEYWORD_ARGUMENT, defaultStyle));
        context.put("opSignArgStyle", createStyle(globalScheme, KitePyHighlighter.PY_OPERATION_SIGN, defaultStyle));
        context.put("builtinNameStyle", createStyle(globalScheme, KitePyHighlighter.PY_BUILTIN_NAME, defaultStyle));
        context.put("bracketStyle", createStyle(globalScheme, KitePyHighlighter.PY_BRACKETS, defaultStyle));
        context.put("unusedStyle", createStyle(globalScheme, CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES, defaultStyle));
        context.put("linkStyle", createStyle(globalScheme, CodeInsightColors.HYPERLINK_ATTRIBUTES, defaultStyle));

        //font family and size
        //getEditorFontSize() is already scaled (i.e. on windows it is scaled according to the Windows UI settings)
        int scaledIdeFontSize;
        if (templateContext.containsKey("fontSize")) {
            scaledIdeFontSize = (int) templateContext.get("fontSize");
            LOG.debug(String.format("Using font size specified in Kite settings: %d", scaledIdeFontSize));
        } else {
            scaledIdeFontSize = globalScheme.getEditorFontSize();
        }

        if (LOG.isDebugEnabled() && !ApplicationManager.getApplication().isHeadlessEnvironment()) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            LOG.debug(String.format("Screen: %s, DPI: %d, IDE font size: %d", toolkit.getScreenSize(), toolkit.getScreenResolution(), scaledIdeFontSize));
        }

        context.put("fontSize", scaledIdeFontSize);
        context.put("fontFamily", UIUtil.getToolTipFont().getFamily());
        context.put("fontFamilyEditor", globalScheme.getEditorFontName());

        //general ui settings
        context.put("hiDPI", KiteRuntimeInfo.isHighDpiScreen());

        return context;
    }

    private RenderStyle createStyle(EditorColorsScheme globalScheme, TextAttributesKey key, @Nullable RenderStyle fallback) {
        TextAttributes attrs = globalScheme.getAttributes(key);

        int type = attrs.getFontType();
        return new RenderStyle(attrs.getForegroundColor(), attrs.getBackgroundColor(), (type & Font.BOLD) == Font.BOLD, (type & Font.ITALIC) == Font.ITALIC, fallback);
    }

    private static RuntimeException newRenderingFailedException(@Nonnull String name, @Nullable KiteLinkData linkData, @Nullable Throwable cause) {
        return new RuntimeException("Rendering failed. Type: " + name + ", link: " + linkData, cause);
    }

    private PebbleEngine createPebbleEngine() {
        return new PebbleEngine.Builder()
                .extension(new KitePebbleExtension())
                .loader(templateLoader)
                .newLineTrimming(true)
                .cacheActive(true)
                .autoEscaping(false)
                .defaultEscapingStrategy("html_attr")
                .build();
    }

    private String renderTemplate(String templateName, Map<String, Object> templateContext) throws PebbleException, IOException {
        EditorColorsScheme globalScheme = EditorColorsManager.getInstance().getGlobalScheme();

        //if file loading is done then a new pebble engine will be created every time to avoid cached templates
        PebbleEngine engine = templateLoader instanceof FileLoader ? createPebbleEngine() : cachedEngine;
        PebbleTemplate compiledTemplate = engine.getTemplate(templateName);

        StringWriter writer = new StringWriter();
        compiledTemplate.evaluate(writer, createContext(templateContext, globalScheme));
        writer.flush();

        //Debug help: store the rendering result in the path given by -Dkite.templateOutputPath.hover
        if (outputFilePath != null) {
            try (FileOutputStream out = new FileOutputStream(new File(outputFilePath))) {
                out.write(writer.toString().getBytes(StandardCharsets.UTF_8));
                LOG.debug("Stored hover rendering in " + outputFilePath);
            } catch (Exception e) {
                LOG.warn("Error storing hover rendering in " + outputFilePath, e);
            }
        }

        return writer.toString();
    }

}
