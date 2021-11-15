package com.kite.intellij.ui;

import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.TestOnly;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 */
class KiteUI {
    private static final Method scaleFontSize_float;
    private static final Method scaleFontSize_int;

    @TestOnly
    private static Float test_fontScaleFactor;

    static {
        Method floatMethod = null;
        try {
            floatMethod = JBUI.class.getDeclaredMethod("scaleFontSize", float.class);
        } catch (NoSuchMethodException ignored) {
        }
        scaleFontSize_float = floatMethod;

        Method intMethod = null;
        try {
            //noinspection JavaReflectionMemberAccess
            intMethod = JBUI.class.getDeclaredMethod("scaleFontSize", int.class);
        } catch (NoSuchMethodException ignored) {
        }
        scaleFontSize_int = intMethod;
    }

    private KiteUI() {

    }

    static int scaleFontSize(float fontSize) {
        if (test_fontScaleFactor != null) {
            return (int) (fontSize * test_fontScaleFactor);
        }

        //we have to call this using reflection because 2016.1 has "int scaleFontSize(int)" and later veresions like 2017.1 have "int scaleFontSize(float)"
        Number result = null;

        //2017.1 variant
        if (scaleFontSize_float != null) {
            try {
                Object invocationResult = scaleFontSize_float.invoke(null, fontSize);
                if (invocationResult instanceof Number) {
                    result = (Number) invocationResult;
                }
            } catch (IllegalAccessException | InvocationTargetException ignored) {
            }
        }

        //2016.1 variant
        if (result == null && scaleFontSize_int != null) {
            try {
                Object invocationResult = scaleFontSize_int.invoke(null, (int) fontSize);
                if (invocationResult instanceof Number) {
                    result = (Number) invocationResult;
                }
            } catch (IllegalAccessException | InvocationTargetException ignored) {
            }
        }

        return result != null ? result.intValue() : (int) fontSize;
    }
}
