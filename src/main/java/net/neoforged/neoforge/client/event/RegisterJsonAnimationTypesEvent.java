/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.entity.animation.AnimationTarget;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows registering custom {@link AnimationTarget}s and
 * {@link AnimationChannel.Interpolation interpolation function}s for loading JSON entity animation files.
 */
public class RegisterJsonAnimationTypesEvent extends Event implements IModBusEvent {
    private final ImmutableMap.Builder<ResourceLocation, AnimationTarget> targets;
    private final ImmutableMap.Builder<ResourceLocation, AnimationChannel.Interpolation> interpolations;
    private final Map<ResourceLocation, String> targetsRegisteredBy = new HashMap<>();
    private final Map<ResourceLocation, String> interpolationsRegisteredBy = new HashMap<>();

    @ApiStatus.Internal
    public RegisterJsonAnimationTypesEvent(
            ImmutableMap.Builder<ResourceLocation, AnimationTarget> targets,
            ImmutableMap.Builder<ResourceLocation, AnimationChannel.Interpolation> interpolations) {
        this.targets = targets;
        this.interpolations = interpolations;
    }

    /**
     * Register a custom {@link AnimationTarget} with the specified {@code key}.
     */
    public void registerTarget(ResourceLocation key, AnimationTarget target) {
        checkDuplicate("target", key, targetsRegisteredBy);
        targets.put(key, target);
    }

    /**
     * Register a custom {@link AnimationChannel.Interpolation interpolation function} with the specified {@code key}.
     */
    public void registerInterpolation(ResourceLocation key, AnimationChannel.Interpolation interpolation) {
        checkDuplicate("interpolation", key, interpolationsRegisteredBy);
        interpolations.put(key, interpolation);
    }

    private static void checkDuplicate(String what, ResourceLocation key, Map<ResourceLocation, String> by) {
        final var currentModId = ModLoadingContext.get().getActiveContainer().getModId();
        final var prevModId = by.putIfAbsent(key, currentModId);
        if (prevModId != null) {
            throw new IllegalStateException(
                    "Duplicate " + what + " registration for " + key + ". " +
                            currentModId + " tried to overwrite " + prevModId + ".");
        }
    }
}
