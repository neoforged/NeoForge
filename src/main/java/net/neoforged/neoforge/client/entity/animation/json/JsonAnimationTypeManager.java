/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.entity.animation.json;

import com.google.common.collect.ImmutableMap;
import java.util.stream.Collectors;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.entity.animation.AnimationTarget;
import net.neoforged.neoforge.client.event.RegisterJsonAnimationTypesEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class JsonAnimationTypeManager {
    private static ImmutableMap<ResourceLocation, AnimationTarget> TARGETS;
    private static ImmutableMap<ResourceLocation, AnimationChannel.Interpolation> INTERPOLATIONS;
    private static String TARGET_LIST;
    private static String INTERPOLATION_LIST;

    private JsonAnimationTypeManager() {}

    @Nullable
    public static AnimationTarget getTarget(ResourceLocation name) {
        return TARGETS.get(name);
    }

    @Nullable
    public static AnimationChannel.Interpolation getInterpolation(ResourceLocation name) {
        return INTERPOLATIONS.get(name);
    }

    public static String getTargetList() {
        return TARGET_LIST;
    }

    public static String getInterpolationList() {
        return INTERPOLATION_LIST;
    }

    @ApiStatus.Internal
    public static void init() {
        final var targets = ImmutableMap.<ResourceLocation, AnimationTarget>builder()
                .put(ResourceLocation.withDefaultNamespace("position"), AnimationTarget.POSITION)
                .put(ResourceLocation.withDefaultNamespace("rotation"), AnimationTarget.ROTATION)
                .put(ResourceLocation.withDefaultNamespace("scale"), AnimationTarget.SCALE);
        final var interpolations = ImmutableMap.<ResourceLocation, AnimationChannel.Interpolation>builder()
                .put(ResourceLocation.withDefaultNamespace("linear"), AnimationChannel.Interpolations.LINEAR)
                .put(ResourceLocation.withDefaultNamespace("catmullrom"), AnimationChannel.Interpolations.CATMULLROM);
        final var event = new RegisterJsonAnimationTypesEvent(targets, interpolations);
        ModLoader.postEventWrapContainerInModOrder(event);
        TARGETS = targets.buildOrThrow();
        INTERPOLATIONS = interpolations.buildOrThrow();
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
