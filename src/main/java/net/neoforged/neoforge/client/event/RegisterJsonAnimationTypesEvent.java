/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.entity.animation.AnimationTarget;
import org.jetbrains.annotations.ApiStatus;

public class RegisterJsonAnimationTypesEvent extends Event implements IModBusEvent {
    private final ImmutableMap.Builder<ResourceLocation, AnimationTarget> targets;
    private final ImmutableMap.Builder<ResourceLocation, AnimationChannel.Interpolation> interpolations;

    @ApiStatus.Internal
    public RegisterJsonAnimationTypesEvent(
            ImmutableMap.Builder<ResourceLocation, AnimationTarget> targets,
            ImmutableMap.Builder<ResourceLocation, AnimationChannel.Interpolation> interpolations) {
        this.targets = targets;
        this.interpolations = interpolations;
    }

    public void registerTarget(ResourceLocation key, AnimationTarget target) {
        targets.put(key, target);
    }

    public void registerInterpolation(ResourceLocation key, AnimationChannel.Interpolation interpolation) {
        interpolations.put(key, interpolation);
    }
}
