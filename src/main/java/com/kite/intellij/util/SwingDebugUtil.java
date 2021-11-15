package com.kite.intellij.util;

import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Helper class to debug the hierarchy of AWT/Swing components.
 *
  */
@SuppressWarnings("unused")
public class SwingDebugUtil {
    public static Component findTopParent(Component component) {
        if (component.getParent() == null) {
            return component;
        }

        return findTopParent(component.getParent());
    }

    public static String printComponentHierarchy(Component component) {
        return printComponentHierarchy(component, 1);
    }

    private static String printComponentHierarchy(Component component, int indentLevel) {
        StringBuilder debug = new StringBuilder();

        debug.append(indent(indentLevel));
        debug.append(debugString(component, indentLevel));

        if (component instanceof Container) {
            if (component.isVisible()) {
                synchronized (component.getTreeLock()) {
                    for (Component child : ((Container) component).getComponents()) {
                        debug.append("\n").append(printComponentHierarchy(child, indentLevel + 1));
                    }
                }
            }
        }

        return debug.toString();
    }

    private static String debugString(Component component, int indentLevel) {
        StringBuilder b = new StringBuilder();

        b.append(String.format("[%s]: ", component.getClass().getCanonicalName()));
        b.append("{\n").append(indent(indentLevel + 1));
        b.append("\"bounds\": ").append(component.getBounds()).append(",\n").append(indent(indentLevel + 1));
        if (component instanceof Container) {
            Container container = (Container) component;
            b.append("\"inset\": ").append(container.getInsets()).append(",\n").append(indent(indentLevel + 1));
        }
        if (component instanceof JComponent) {
            JComponent container = (JComponent) component;
            b.append("\"border\": ").append(container.getBorder()).append(",\n").append(indent(indentLevel + 1));
        }
        b.append("\"background\": ").append(component.getBackground()).append(",\n").append(indent(indentLevel + 1));
        b.append("\"foreground\": ").append(component.getForeground()).append("\n");
        b.append(indent(indentLevel + 1)).append("}\n");

        return b.toString();
    }

    private static String indent(int indentLevel) {
        return StringUtils.repeat("   ", indentLevel);
    }
}
