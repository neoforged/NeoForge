/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.entity.animation.json;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.entity.animation.AnimationTarget;
import net.neoforged.neoforge.client.event.RegisterJsonAnimationTypesEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class AnimationTypeManager {
    private static final ImmutableBiMap<ResourceLocation, AnimationTarget> DEFAULT_TARGETS = ImmutableBiMap.of(
            ResourceLocation.withDefaultNamespace("position"), AnimationTarget.POSITION,
            ResourceLocation.withDefaultNamespace("rotation"), AnimationTarget.ROTATION,
            ResourceLocation.withDefaultNamespace("scale"), AnimationTarget.SCALE);
    private static final ImmutableBiMap<ResourceLocation, AnimationChannel.Interpolation> DEFAULT_INTERPOLATIONS = ImmutableBiMap.of(
            ResourceLocation.withDefaultNamespace("linear"), AnimationChannel.Interpolations.LINEAR,
            ResourceLocation.withDefaultNamespace("catmullrom"), AnimationChannel.Interpolations.CATMULLROM);

    private static ImmutableBiMap<ResourceLocation, AnimationTarget> TARGETS = DEFAULT_TARGETS;
    private static ImmutableMap<AnimationChannel.Target, AnimationTarget> TARGETS_BY_CHANNEL_TARGET = ImmutableMap.of();
    private static ImmutableBiMap<ResourceLocation, AnimationChannel.Interpolation> INTERPOLATIONS = DEFAULT_INTERPOLATIONS;
    private static String TARGET_LIST = "";
    private static String INTERPOLATION_LIST = "";

    static {
        recomputeDerivedFields();
    }

    private AnimationTypeManager() {}

    @Nullable
    public static AnimationTarget getTarget(ResourceLocation name) {
        return TARGETS.get(name);
    }

    @Nullable
    public static ResourceLocation getTargetName(AnimationTarget target) {
        return TARGETS.inverse().get(target);
    }

    @Nullable
    public static AnimationTarget getTargetFromChannelTarget(AnimationChannel.Target target) {
        return TARGETS_BY_CHANNEL_TARGET.get(target);
    }

    @Nullable
    public static AnimationChannel.Interpolation getInterpolation(ResourceLocation name) {
        return INTERPOLATIONS.get(name);
    }

    @Nullable
    public static ResourceLocation getInterpolationName(AnimationChannel.Interpolation interpolation) {
        return INTERPOLATIONS.inverse().get(interpolation);
    }

    public static String getTargetList() {
        return TARGET_LIST;
    }

    public static String getInterpolationList() {
        return INTERPOLATION_LIST;
    }

    @ApiStatus.Internal
    public static void init() {
        final var targets = ImmutableBiMap.<ResourceLocation, AnimationTarget>builder().putAll(DEFAULT_TARGETS);
        final var interpolations = ImmutableBiMap.<ResourceLocation, AnimationChannel.Interpolation>builder().putAll(DEFAULT_INTERPOLATIONS);
        final var event = new RegisterJsonAnimationTypesEvent(targets, interpolations);
        ModLoader.postEventWrapContainerInModOrder(event);
        TARGETS = targets.buildOrThrow();
        INTERPOLATIONS = interpolations.buildOrThrow();
        recomputeDerivedFields();
    }

    private static void recomputeDerivedFields() {
        TARGETS_BY_CHANNEL_TARGET = TARGETS.values()
                .stream()
                .collect(ImmutableMap.toImmutableMap(AnimationTarget::channelTarget, Function.identity()));
        TARGET_LIST = TARGETS.keySet()
                .stream()
                .map(ResourceLocation::toString)
                .collect(Collectors.joining(", "));
        INTERPOLATION_LIST = INTERPOLATIONS.keySet()
                .stream()
                .map(ResourceLocation::toString)
                .collect(Collectors.joining(", "));
    }
}
