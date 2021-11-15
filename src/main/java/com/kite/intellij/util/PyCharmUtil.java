package com.kite.intellij.util;

import com.intellij.openapi.project.Project;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PyCharmUtil {
    public static boolean isScientificMode(Project project) {
        try {
            Class<?> clazz = PyCharmUtil.class.getClassLoader().loadClass("com.jetbrains.scientific.PySciProjectComponent");
            Method getInstance = clazz.getMethod("getInstance", Project.class);
            Object instance = getInstance.invoke(null, project);

            Method useSciView = instance.getClass().getMethod("useSciView");
            return Boolean.TRUE.equals(useSciView.invoke(instance));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return false;
        }
    }
}
