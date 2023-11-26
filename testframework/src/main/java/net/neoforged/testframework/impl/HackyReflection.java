package net.neoforged.testframework.impl;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import sun.misc.Unsafe;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class HackyReflection {
    public static final Unsafe UNSAFE;
    public static final MethodHandles.Lookup LOOKUP;

    static {
        try {
            final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);

            LOOKUP = getStaticField(MethodHandles.Lookup.class, "IMPL_LOOKUP");
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("BARF!", e);
        }
    }

    public static <T> T getStaticField(Class<?> clazz, String name) {
        try {
            final Field field = clazz.getDeclaredField(name);
            return getStaticField(field);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("BARF!", e);
        }
    }

    public static <T> T getStaticField(Field field) {
        return (T) UNSAFE.getObject(UNSAFE.staticFieldBase(field), UNSAFE.staticFieldOffset(field));
    }

    public static <T> T getInstanceField(Object instance, String name) {
        try {
            final Field field = instance.getClass().getDeclaredField(name);
            return (T) UNSAFE.getObject(instance, UNSAFE.objectFieldOffset(field));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("BARF!", e);
        }
    }

    public static void setInstanceField(Object instance, String name, Object value) {
        try {
            final Field field = instance.getClass().getDeclaredField(name);
            UNSAFE.putObject(instance, UNSAFE.objectFieldOffset(field), value);
        } catch (NoSuchFieldException e) {
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
            return LOOKUP.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("BARF!", e);
        }
    }

    public static MethodHandle staticHandle(Method method) {
        try {
            return LOOKUP.findStatic(method.getDeclaringClass(), method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("BARF!", e);
        }
    }

    public static MethodHandle virtualHandle(Method method) {
        try {
            return LOOKUP.findVirtual(method.getDeclaringClass(), method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("BARF!", e);
        }
    }

    public static MethodHandle handle(Method method) {
        return Modifier.isStatic(method.getModifiers()) ? staticHandle(method) : virtualHandle(method);
    }

    public static MethodHandle constructor(Class<?> owner, MethodType type) {
        try {
            return LOOKUP.findConstructor(owner, type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("BARF!", e);
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

    public static VarHandle varHandle(Class<?> clazz, String name) {
        try {
            final Field field = clazz.getDeclaredField(name);
            return Modifier.isStatic(field.getModifiers()) ? LOOKUP.findStaticVarHandle(clazz, name, field.getType()) : LOOKUP.findVarHandle(clazz, name, field.getType());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("BARF!", e);
        }
    }

    private static final VarHandle TEST_INFO = varHandle(GameTestHelper.class, "testInfo");

    public static void addListener(GameTestHelper helper, GameTestListener listener) {
        ((GameTestInfo) TEST_INFO.get(helper)).addListener(listener);
    }
}
