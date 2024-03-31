/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import com.google.common.base.Suppliers;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.mojang.logging.LogUtils;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModAnnotation;
import net.neoforged.neoforgespi.language.ModFileScanData;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.RegisterStructureTemplate;
import net.neoforged.testframework.annotation.TestGroup;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;
import net.neoforged.testframework.impl.test.MethodBasedEventTest;
import net.neoforged.testframework.impl.test.MethodBasedGameTestTest;
import net.neoforged.testframework.impl.test.MethodBasedTest;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.objectweb.asm.Type;

public final class FrameworkCollectors {
    private static final Predicate<ModFileScanData.AnnotationData> SIDE_FILTER = data -> {
        final Dist current = FMLLoader.getDist();
        Object sidesValue = data.annotationData().get("side");
        if (sidesValue == null) sidesValue = data.annotationData().get("dist");
        if (sidesValue == null) return true;
        @SuppressWarnings("unchecked")
        final EnumSet<Dist> sides = ((List<ModAnnotation.EnumHolder>) sidesValue).stream().map(eh -> Dist.valueOf(eh.getValue())).collect(java.util.stream.Collectors.toCollection(() -> EnumSet.noneOf(Dist.class)));
        return sides.contains(current);
    };

    public static final class Tests {
        public static List<Test> forClassesWithAnnotation(ModContainer container, Class<? extends Annotation> annotation) {
            final Type annType = Type.getType(annotation);
            return container.getModInfo().getOwningFile().getFile().getScanResult()
                    .getAnnotations().stream().filter(it -> annType.equals(it.annotationType()) && it.targetType() == ElementType.TYPE && SIDE_FILTER.test(it))
                    .map(LamdbaExceptionUtils.rethrowFunction(annotationData -> {
                        final Class<?> clazz = Class.forName(annotationData.clazz().getClassName());
                        return (Test) clazz.getDeclaredConstructor().newInstance();
                    })).toList();
        }

        public static List<Test> forMethodsWithAnnotation(ModContainer container, Class<? extends Annotation> annotation) {
            return findMethodsWithAnnotation(container, SIDE_FILTER, annotation)
                    .filter(method -> (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].isAssignableFrom(MethodBasedTest.class)) ||
                            (method.getParameterTypes().length == 2 && method.getParameterTypes()[0].isAssignableFrom(MethodBasedTest.class) && method.getParameterTypes()[1] == RegistrationHelper.class))
                    .filter(method -> {
                        if (Modifier.isStatic(method.getModifiers())) {
                            return true;
                        }
                        LogUtils.getLogger().warn("Attempted to register method-based test on non-static method: " + method);
                        return false;
                    })
                    .<Test>map(MethodBasedTest::new).toList();
        }

        public static List<Test> forGameTestMethodsWithAnnotation(ModContainer container, Class<? extends Annotation> annotation) {
            return findMethodsWithAnnotation(container, SIDE_FILTER, annotation)
                    .filter(method -> method.getParameterTypes().length == 1 && GameTestHelper.class.isAssignableFrom(method.getParameterTypes()[0]))
                    .filter(method -> {
                        if (Modifier.isStatic(method.getModifiers())) {
                            return true;
                        }
                        LogUtils.getLogger().warn("Attempted to register method-based gametest test on non-static method: " + method);
                        return false;
                    })
                    .<Test>map(LamdbaExceptionUtils.rethrowFunction(method -> {
                        if (method.getParameterTypes()[0].isAssignableFrom(ExtendedGameTestHelper.class)) {
                            return new MethodBasedGameTestTest(method, ExtendedGameTestHelper.class);
                        }

                        return new MethodBasedGameTestTest(method, method.getParameterTypes()[0]);
                    })).toList();
        }

        public static List<Test> eventTestMethodsWithAnnotation(ModContainer container, Class<? extends Annotation> annotation) {
            return findMethodsWithAnnotation(container, SIDE_FILTER, annotation)
                    .filter(method -> method.getParameterTypes().length == 2 && Event.class.isAssignableFrom(method.getParameterTypes()[0]) && method.getParameterTypes()[1].isAssignableFrom(MethodBasedEventTest.class))
                    .filter(method -> {
                        if (Modifier.isStatic(method.getModifiers())) {
                            return true;
                        }
                        LogUtils.getLogger().warn("Attempted to register method-based event test on non-static method: " + method);
                        return false;
                    })
                    .<Test>map(MethodBasedEventTest::new).toList();
        }
    }

    /**
     * This method collects init listeners based on static methods
     * accepting exactly one parameter of {@linkplain MutableTestFramework} (or parent interfaces).
     */
    public static SetMultimap<OnInit.Stage, Consumer<MutableTestFramework>> onInitMethodsWithAnnotation(ModContainer container) {
        final SetMultimap<OnInit.Stage, Consumer<MutableTestFramework>> set = Multimaps.newSetMultimap(new EnumMap<>(OnInit.Stage.class), HashSet::new);
        findMethodsWithAnnotation(container, d -> true, OnInit.class)
                .filter(method -> Modifier.isStatic(method.getModifiers()) && method.getParameterTypes().length == 1 && method.getParameterTypes()[0].isAssignableFrom(TestFrameworkImpl.class))
                .forEach(LamdbaExceptionUtils.rethrowConsumer(method -> {
                    final MethodHandle handle = ReflectionUtils.handle(method);
                    set.put(method.getAnnotation(OnInit.class).value(), framework -> {
                        try {
                            handle.invokeWithArguments(framework);
                        } catch (Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    });
                }));
        return set;
    }

    /**
     * This method collects structure templates based on static fields containing
     * either a {@link StructureTemplate}, a {@link Supplier} of {@linkplain StructureTemplate} or a {@link StructureTemplateBuilder},
     * annotated with {@link RegisterStructureTemplate}.
     */
    public static void templatesWithAnnotation(final ModContainer container, BiConsumer<ResourceLocation, Supplier<StructureTemplate>> acceptor) {
        final Type regStrTemplate = Type.getType(RegisterStructureTemplate.class);
        container.getModInfo().getOwningFile().getFile().getScanResult()
                .getAnnotations().stream()
                .filter(it -> it.targetType() == ElementType.FIELD && it.annotationType().equals(regStrTemplate))
                .map(LamdbaExceptionUtils.rethrowFunction(data -> Class.forName(data.clazz().getClassName()).getDeclaredField(data.memberName())))
                .filter(it -> Modifier.isStatic(it.getModifiers()) && (StructureTemplate.class.isAssignableFrom(it.getType()) || Supplier.class.isAssignableFrom(it.getType())))
                .forEach(field -> {
                    try {
                        final Object obj = ReflectionUtils.fieldHandle(field).invoke();
                        final var annotation = field.getAnnotation(RegisterStructureTemplate.class);
                        final ResourceLocation id = new ResourceLocation(annotation.value());
                        if (obj instanceof StructureTemplate template) {
                            acceptor.accept(id, () -> template);
                        } else if (obj instanceof Supplier<?> supplier) {
                            //noinspection unchecked
                            acceptor.accept(id, (Supplier<StructureTemplate>) supplier);
                        } else if (obj instanceof StructureTemplateBuilder builder) {
                            acceptor.accept(id, Suppliers.memoize(builder::build));
                        }
                    } catch (Throwable exception) {
                        throw new RuntimeException(exception);
                    }
                });
    }

    /**
     * Collects group information from string fields annotated with {@link TestGroup}.
     */
    public static void groupsWithAnnotation(ModContainer container, Consumer<GroupData> consumer) {
        final Type asmType = Type.getType(TestGroup.class);
        container.getModInfo().getOwningFile().getFile().getScanResult()
                .getAnnotations().stream().filter(it -> asmType.equals(it.annotationType()))
                .forEach(LamdbaExceptionUtils.rethrowConsumer(annotationData -> {
                    final Class<?> clazz = Class.forName(annotationData.clazz().getClassName());
                    final Field field = clazz.getDeclaredField(annotationData.memberName());
                    final String groupId = (String) field.get(null);
                    final var annotation = field.getAnnotation(TestGroup.class);
                    consumer.accept(new GroupData(
                            groupId, Component.literal(annotation.name()),
                            annotation.enabledByDefault(),
                            annotation.parents()));
                }));
    }

    public static Stream<Method> findMethodsWithAnnotation(ModContainer container, Predicate<ModFileScanData.AnnotationData> annotationPredicate, Class<? extends Annotation> annotation) {
        final Type forEach = Type.getType(ForEachTest.class);

        final var excludedSides = container.getModInfo().getOwningFile().getFile().getScanResult()
                .getAnnotations().stream().filter(it -> forEach.equals(it.annotationType()) && it.targetType() == ElementType.TYPE)
                .filter(data -> !SIDE_FILTER.test(data))
                .map(data -> data.clazz().getClassName())
                .collect(java.util.stream.Collectors.toSet());

        final Type annType = Type.getType(annotation);
        return container.getModInfo().getOwningFile().getFile().getScanResult()
                .getAnnotations().stream().filter(it -> annType.equals(it.annotationType()) && it.targetType() == ElementType.METHOD && annotationPredicate.test(it))
                .filter(it -> !excludedSides.contains(it.clazz().getClassName()))
                .map(LamdbaExceptionUtils.rethrowFunction(annotationData -> {
                    final Class<?> clazz = Class.forName(annotationData.clazz().getClassName());
                    final String methodName = annotationData.memberName().substring(0, annotationData.memberName().indexOf("("));
                    return ReflectionUtils.methodMatching(clazz, it -> it.getName().equals(methodName) && it.getAnnotation(annotation) != null);
                }));
    }

    public record GroupData(String id, @Nullable Component title, boolean isEnabledByDefault, String[] parents) {}
}
