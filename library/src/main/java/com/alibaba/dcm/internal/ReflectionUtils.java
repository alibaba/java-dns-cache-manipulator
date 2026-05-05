package com.alibaba.dcm.internal;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;

final class ReflectionUtils {
    static @Nullable Field getFallbackDeclaredFieldOrNull(Class<?> clazz, String... fieldNames) {
        for (String name : fieldNames) {
            try {
                return getDeclaredFieldOrThrow(clazz, name);
            } catch (NoSuchFieldException ignored) {}
        }
        return null;
    }

    static Field getDeclaredFieldOrThrow(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        final Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f;
    }

    static Class<?> getFallbackClassForName(String... classNames) throws ClassNotFoundException {
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ignored) {}
        }
        throw new ClassNotFoundException("None of the classes " + Arrays.toString(classNames) + " found");
    }

    @SuppressWarnings("SameParameterValue")
    static boolean hasDeclaredField(Class<?> clazz, String fieldName) {
        return Arrays.stream(clazz.getDeclaredFields())
                .anyMatch(f -> f.getName().equals(fieldName));
    }

    private ReflectionUtils() {}
}
