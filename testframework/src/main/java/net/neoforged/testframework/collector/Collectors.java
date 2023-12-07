/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.collector;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
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
import net.neoforged.testframework.gametest.StructureTemplateBuilder;
import net.neoforged.testframework.impl.ReflectionUtils;
import net.neoforged.testframework.impl.TestFrameworkImpl;
import net.neoforged.testframework.impl.TestFrameworkInternal;
import net.neoforged.testframework.impl.test.MethodBasedEventTest;
import net.neoforged.testframework.impl.test.MethodBasedTest;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.objectweb.asm.Type;

public final class Collectors {
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
        public static Collector<Test> forClassesWithAnnotation(Class<? extends Annotation> annotation) {
            final Type annType = Type.getType(annotation);
            return (container, acceptor) -> container.getModInfo().getOwningFile().getFile().getScanResult()
                    .getAnnotations().stream().filter(it -> annType.equals(it.annotationType()) && it.targetType() == ElementType.TYPE && SIDE_FILTER.test(it))
                    .map(LamdbaExceptionUtils.rethrowFunction(annotationData -> {
                        final Class<?> clazz = Class.forName(annotationData.clazz().getClassName());
                        return (Test) clazz.getDeclaredConstructor().newInstance();
                    })).forEach(acceptor);
        }

        public static Collector<Test> forMethodsWithAnnotation(Class<? extends Annotation> annotation) {
            return (container, acceptor) -> findMethodsWithAnnotation(container, SIDE_FILTER, annotation)
                    .filter(method -> (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].isAssignableFrom(MethodBasedTest.class)) ||
                            (method.getParameterTypes().length == 2 && method.getParameterTypes()[0].isAssignableFrom(MethodBasedTest.class) && method.getParameterTypes()[1] == RegistrationHelper.class))
                    .filter(method -> {
                        if (Modifier.isStatic(method.getModifiers())) {
                            return true;
                        }
                        LogUtils.getLogger().warn("Attempted to register method-based test on non-static method: " + method);
                        return false;
                    })
                    .<Test>map(MethodBasedTest::new).forEach(acceptor);
        }

        public static Collector<Test> eventTestMethodsWithAnnotation(Class<? extends Annotation> annotation) {
            return (container, acceptor) -> findMethodsWithAnnotation(container, SIDE_FILTER, annotation)
                    .filter(method -> method.getParameterTypes().length == 2 && Event.class.isAssignableFrom(method.getParameterTypes()[0]) && method.getParameterTypes()[1].isAssignableFrom(MethodBasedEventTest.class))
                    .filter(method -> {
                        if (Modifier.isStatic(method.getModifiers())) {
                            return true;
                        }
                        LogUtils.getLogger().warn("Attempted to register method-based event test on non-static method: " + method);
                        return false;
                    })
                    .<Test>map(MethodBasedEventTest::new).forEach(acceptor);
        }
    }

    /**
     * This method creates a collector for {@linkplain CollectorType#INIT_LISTENERS init listeners} which is based on static methods
     * accepting exactly one parameter of {@linkplain TestFrameworkInternal} (or parent interfaces).
     *
     * @param annotation  the type of the annotation to look for
     * @param stageGetter the getter of the stage to fire the listener on
     * @param <A>         the annotation
     * @return the collector
     */
    public static <A extends Annotation> Collector<Pair<OnInit.Stage, Consumer<? super TestFrameworkInternal>>> onInitMethodsWithAnnotation(Class<A> annotation, Function<A, OnInit.Stage> stageGetter) {
        return (container, acceptor) -> findMethodsWithAnnotation(container, d -> true, annotation)
                .filter(method -> Modifier.isStatic(method.getModifiers()) && method.getParameterTypes().length == 1 && method.getParameterTypes()[0].isAssignableFrom(TestFrameworkImpl.class))
                .forEach(LamdbaExceptionUtils.rethrowConsumer(method -> {
                    final MethodHandle handle = ReflectionUtils.handle(method);
                    acceptor.accept(Pair.of(stageGetter.apply(method.getAnnotation(annotation)), framework -> {
                        try {
                            handle.invokeWithArguments(framework);
                        } catch (Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    }));
                }));
    }

    public static Collector<Pair<OnInit.Stage, Consumer<? super TestFrameworkInternal>>> defaultOnInitCollector() {
        return onInitMethodsWithAnnotation(OnInit.class, OnInit::value);
    }

    /**
     * This method creates a collector for {@linkplain CollectorType#STRUCTURE_TEMPLATES structure templates} which is based on static fields containing
     * either a {@link StructureTemplate}, a {@link Supplier} of {@linkplain StructureTemplate} or a {@link StructureTemplateBuilder},
     * annotated with an annotation.
     *
     * @param annotationType the type of the annotation to look for
     * @param idGetter       a getter for the template ID
     * @param <A>            the annotation
     * @return the collector
     */
    public static <A extends Annotation> Collector<Pair<ResourceLocation, Supplier<StructureTemplate>>> templatesWithAnnotation(Class<A> annotationType, Function<A, ResourceLocation> idGetter) {
        final Type regStrTemplate = Type.getType(annotationType);
        return (container, acceptor) -> container.getModInfo().getOwningFile().getFile().getScanResult()
                .getAnnotations().stream()
                .filter(it -> it.targetType() == ElementType.FIELD && it.annotationType().equals(regStrTemplate))
                .map(LamdbaExceptionUtils.rethrowFunction(data -> Class.forName(data.clazz().getClassName()).getDeclaredField(data.memberName())))
                .filter(it -> Modifier.isStatic(it.getModifiers()) && (StructureTemplate.class.isAssignableFrom(it.getType()) || Supplier.class.isAssignableFrom(it.getType())))
                .forEach(field -> {
                    try {
                        final Object obj = ReflectionUtils.fieldHandle(field).invoke();
                        final A annotation = field.getAnnotation(annotationType);
                        final ResourceLocation id = idGetter.apply(annotation);
                        if (obj instanceof StructureTemplate template) {
                            acceptor.accept(Pair.of(id, () -> template));
                        } else if (obj instanceof Supplier<?> supplier) {
                            //noinspection unchecked
                            acceptor.accept(Pair.of(id, (Supplier<StructureTemplate>) supplier));
                        } else if (obj instanceof StructureTemplateBuilder builder) {
                            acceptor.accept(Pair.of(id, Suppliers.memoize(builder::build)));
                        }
                    } catch (Throwable exception) {
                        throw new RuntimeException(exception);
                    }
                });
    }

    public static Collector<Pair<ResourceLocation, Supplier<StructureTemplate>>> defaultTemplateCollector() {
        return templatesWithAnnotation(RegisterStructureTemplate.class, an -> new ResourceLocation(an.value()));
    }

    /**
     * This method creates a collector for {@linkplain CollectorType#GROUP_DATA group data} which is based on fields - annotated with
     * an annotation - of type {@link String},
     * whose underlying value is the ID of the group to configure.
     *
     * @param annotationType     the annotation to look for
     * @param title              the getter of the group title
     * @param isEnabledByDefault the getter for the "enabledByDefault" property
     * @param parents            the getter for the group's parents
     * @param <A>                the annotation
     * @return the collector
     */
    public static <A extends Annotation> Collector<CollectorType.GroupData> groupsWithAnnotation(Class<A> annotationType, Function<A, Component> title, Function<A, Boolean> isEnabledByDefault, Function<A, String[]> parents) {
        final Type asmType = Type.getType(annotationType);
        return (container, acceptor) -> container.getModInfo().getOwningFile().getFile().getScanResult()
                .getAnnotations().stream().filter(it -> asmType.equals(it.annotationType()))
                .forEach(LamdbaExceptionUtils.rethrowConsumer(annotationData -> {
                    final Class<?> clazz = Class.forName(annotationData.clazz().getClassName());
                    final Field field = clazz.getDeclaredField(annotationData.memberName());
                    final String groupId = (String) field.get(null);
                    final A annotation = field.getAnnotation(annotationType);
                    acceptor.accept(new CollectorType.GroupData(
                            groupId, title.apply(annotation),
                            isEnabledByDefault.apply(annotation),
                            parents.apply(annotation)));
                }));
    }

    public static Collector<CollectorType.GroupData> defaultGroupCollector() {
        return groupsWithAnnotation(TestGroup.class, it -> Component.translatable(it.name()), TestGroup::enabledByDefault, TestGroup::parents);
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
}
