package com.kite.intellij.ui.html;

import com.google.common.collect.Maps;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfoRt;
import com.kite.monitoring.TimeTracker;
import com.kite.monitoring.TimerTrackers;
import org.w3c.dom.Document;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.swing.CursorListener;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.HoverListener;
import org.xhtmlrenderer.swing.ScalableXHTMLPanel;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRLogger;
import org.xhtmlrenderer.util.XRRuntimeException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.logging.Level;

/**
 * Customized XHTML panel which adds the painting optimizations needed to look like the other IntelliJ UI components.
 *
  * @see ScalableXHTMLPanel
 */
@SuppressWarnings("unchecked")
public class KiteXHTMLPanel extends XHTMLPanel {
    private static final Logger LOG = Logger.getInstance("#kite.ui.html");

    static {
        try {
            // https://github.com/kiteco/intellij-plugin-private/issues/564
            // we're enforcing fractional font metrics to work around an issue in JetBrains runtime 11
            // it's probably the better solution for text rendering, anyway
            if (SystemInfoRt.isLinux) {
                System.setProperty("xr.text.fractional-font-metrics", "true");
            }
            // use a very high threshold to make it never activate. We're using the IDE's antialiasing settings
            System.setProperty("xr.text.aa-fontsize-threshhold", "100");
            System.setProperty("xr.text.aa-rendering-hint", "java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF");
        } catch (Exception e) {
            LOG.warn("Overriding XHTMLRenderer system properties failed", e);
        }

        try {
            Map<String, Logger> LOGGERS = Maps.newHashMap();
            XRLog.listRegisteredLoggers().forEach(name -> LOGGERS.put(name, Logger.getInstance(name)));
            XRLog.setLoggingEnabled(true);
            //calls the delayed init and has to be before setLoggerImpl
            XRLog.setLevel(XRLog.GENERAL, Level.INFO);
            XRLog.setLoggerImpl(new KiteXRLogger(LOGGERS));
        } catch (Exception e) {
            //make sure that no exception breaks the initialisation of this class
            LOG.warn("Error initialising KiteXHTMLPanel", e);
        }
    }

    KiteXHTMLPanel() {
        //remove the listeners added by the default configuration, we don't want the original link handler
        getMouseTrackingListeners().forEach(l -> removeMouseTrackingListener((FSMouseListener) l));
        addMouseTrackingListener(new HoverListener());
        addMouseTrackingListener(new CursorListener());
        setOpaque(false); //let the html define its own background color
    }

    @Override
    public void paintComponent(Graphics g) {
        try (TimeTracker ignored = TimerTrackers.start(String.format("xhtml painting (%d x %d)", getWidth(), getHeight()))) {
            UISettings.setupAntialiasing(g);
            super.paintComponent(g);
        }
    }

    void setXHTML(@Nonnull String xml, @Nullable DocumentBuilder builder) {
        if (builder == null) {
            return;
        }

        try (TimeTracker ignored = TimerTrackers.start("xhtml rendering")) {
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            setDocument(document, "kite:");
        } catch (XRRuntimeException | SAXException | IOException e) {
            throw new InvalidHTMLException("", xml, e);
        }
    }

    private static final class KiteXRLogger implements XRLogger {
        private final Map<String, Logger> LOGGERS;

        KiteXRLogger(Map<String, Logger> LOGGERS) {
            this.LOGGERS = LOGGERS;
        }

        @Override
        public void log(String where, Level level, String msg) {
            Logger log = LOGGERS.get(where);
            if (log != null) {
                if (level == Level.SEVERE) {
                    log.error(msg);
                } else if (level == Level.WARNING) {
                    log.warn(msg);
                } else if (level == Level.INFO) {
                    log.info(msg);
                } else if (log.isDebugEnabled() && level == Level.FINE) {
                    log.debug(msg);
                } else if (log.isTraceEnabled() && (level == Level.FINER || level == Level.FINEST || level == Level.ALL)) {
                    log.trace(msg);
                }
            }
        }

        @Override
        public void log(String where, Level level, String msg, Throwable throwable) {
            Logger log = LOGGERS.get(where);
            if (log != null) {
                if (level == Level.SEVERE) {
                    log.error(msg, throwable);
                } else if (level == Level.WARNING) {
                    log.warn(msg, throwable);
                } else if (level == Level.INFO) {
                    log.info(msg, throwable);
                } else if (log.isDebugEnabled() && level == Level.FINE) {
                    log.debug(msg, throwable);
                } else if (log.isTraceEnabled() && (level == Level.FINER || level == Level.FINEST || level == Level.ALL)) {
                    log.trace(msg); //no throwable supported
                }
            }
        }

        @Override
        public void setLevel(String logger, Level level) {
            Logger log = LOGGERS.get(logger);
            if (log != null) {
                org.apache.log4j.Level targetLevel;
                if (level == Level.ALL) {
                    targetLevel = org.apache.log4j.Level.ERROR;
                } else if (level == Level.SEVERE) {
                    targetLevel = org.apache.log4j.Level.ERROR;
                } else if (level == Level.WARNING) {
                    targetLevel = org.apache.log4j.Level.WARN;
                } else if (level == Level.INFO) {
                    targetLevel = org.apache.log4j.Level.INFO;
                } else if (level == Level.FINE) {
                    targetLevel = org.apache.log4j.Level.DEBUG;
                } else {
                    targetLevel = org.apache.log4j.Level.TRACE;
                }

                log.setLevel(targetLevel);
            }
        }
    }
}
