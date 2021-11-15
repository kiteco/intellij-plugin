package com.kite.intellij.util;

import com.intellij.util.Alarm;
import org.jetbrains.annotations.TestOnly;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class KiteAlarm {
    private KiteAlarm() {
    }

    /**
     * Flush the pending events of the alarm.
     * 183.x removed "flush()" but has "drainRequestsInTest()". We have to call the matching function via reflection.
     * versions before 183.x have a method "flush()".
     *
     * @param alarm the alarm to flush
     */
    @TestOnly
    public static void flush(Alarm alarm) {
        Class<? extends Alarm> c = alarm.getClass();

        try {
            Method method = c.getDeclaredMethod("drainRequestsInTest");
            method.invoke(alarm);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            try {
                Method method = c.getDeclaredMethod("flush");
                method.invoke(alarm);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e1) {
                //ignore
            }
        }
    }
}
