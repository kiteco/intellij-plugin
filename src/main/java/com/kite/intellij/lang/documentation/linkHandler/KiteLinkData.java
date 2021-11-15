package com.kite.intellij.lang.documentation.linkHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Defines the context which is available while processing a link in a {@link LinkHandler}.
 * The context is used to create the link and to pass the information into the rendering template.
 *
  */
public interface KiteLinkData {
    /**
     * Generic way to update a property of this link data.
     * The default implementation checks for a method "withPropertyName" and calls it with the property, if possible.
     *
     * @param propertyName  The property to set
     * @param propertyValue The value to set for the given property
     * @return A link data instance which contains the new value. Not necessarily a new instance, but it's immutable always.
     * @throws IllegalArgumentException if the propertyName or value are incompatible with this link data
     */
    default KiteLinkData with(String propertyName, Object propertyValue) {
        Class<? extends KiteLinkData> currentClass = getClass();

        String methodName = "with" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);

        for (Method method : currentClass.getDeclaredMethods()) {
            if (methodName.equals(method.getName())) {
                if (!method.getReturnType().isAssignableFrom(currentClass)) {
                    throw new IllegalStateException(String.format("Return type of method %s is incompatible with class %s.", methodName, currentClass));
                }

                if (method.getParameterCount() != 1) {
                    throw new IllegalStateException(String.format("Expected just one parameter for %s.%s", currentClass, methodName));
                }

                Class<?> paramType = method.getParameterTypes()[0];
                if (!paramType.isInstance(propertyValue)) {
                    throw new IllegalStateException(String.format("Parameter type %s not compatible with argument value %s", paramType, propertyValue));
                }

                try {
                    return (KiteLinkData) method.invoke(this, propertyValue);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException(String.format("Error invoking method %s on %s", methodName, this));
                }
            }
        }

        throw new IllegalStateException(String.format("No matching method %s found on %s.", methodName, currentClass));
    }
}
