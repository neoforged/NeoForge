/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestListener;

@SuppressWarnings("unchecked")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ReflectionUtils {

    public static <T> T getInstanceField(Object instance, String name) {
        try {
            final Field field = instance.getClass().getDeclaredField(name);
            return (T) fieldHandle(field).invoke(instance);
        } catch (Throwable e) {
            throw new RuntimeException("BARF!", e);
        }
    }

    public static Field getField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("BARF!", e);
        }
    }

    public static MethodHandle fieldHandle(Field field) {
        try {
            return MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup()).unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("BARF!", e);
        }
    }

    public static MethodHandle handle(Method method) {
        try {
            return MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup()).unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle constructor(Class<?> owner, MethodType type) {
        try {
            return MethodHandles.privateLookupIn(owner, MethodHandles.lookup()).findConstructor(owner, type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Method methodMatching(Class<?> clazz, Predicate<Method> methodPredicate) {
        return Stream.of(clazz.getDeclaredMethods())
                .filter(methodPredicate).findFirst().orElseThrow();
    }

    public static Class<?> parentOrTopLevel(Class<?> clazz) {
        if (clazz.getEnclosingClass() != null) return clazz.getEnclosingClass();
        return clazz;
    }

    public static void addListener(GameTestHelper helper, GameTestListener listener) {
        helper.testInfo.addListener(listener);
    }
}
