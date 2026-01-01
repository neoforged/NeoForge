/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.entity.animation.AnimationTarget;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows registering custom {@link AnimationTarget}s and
 * {@link AnimationChannel.Interpolation interpolation function}s for loading JSON entity animation files.
 */
public class RegisterJsonAnimationTypesEvent extends Event implements IModBusEvent {
    private final ImmutableMap.Builder<Identifier, AnimationTarget> targets;
    private final ImmutableMap.Builder<Identifier, AnimationChannel.Interpolation> interpolations;
    private final Map<Identifier, AnimationTarget> registeredTargets = new HashMap<>();
    private final Map<Identifier, AnimationChannel.Interpolation> registeredInterpolations = new HashMap<>();

    @ApiStatus.Internal
    public RegisterJsonAnimationTypesEvent(
            ImmutableMap.Builder<Identifier, AnimationTarget> targets,
            ImmutableMap.Builder<Identifier, AnimationChannel.Interpolation> interpolations) {
        this.targets = targets;
        this.interpolations = interpolations;
    }

    /**
     * Register a custom {@link AnimationTarget} with the specified {@code key}.
     */
    public void registerTarget(Identifier key, AnimationTarget target) {
        checkDuplicate("target", key, registeredTargets, target);
        targets.put(key, target);
    }

    /**
     * Register a custom {@link AnimationChannel.Interpolation interpolation function} with the specified {@code key}.
     */
    public void registerInterpolation(Identifier key, AnimationChannel.Interpolation interpolation) {
        checkDuplicate("interpolation", key, registeredInterpolations, interpolation);
        interpolations.put(key, interpolation);
    }

    private static <T> void checkDuplicate(String what, Identifier key, Map<Identifier, T> by, T obj) {
        final var prevObj = by.putIfAbsent(key, obj);
        if (prevObj != null) {
            throw new IllegalStateException(
                    "Duplicate " + what + " registration for " + key + ". " +
                            obj + " tried to overwrite " + prevObj + ".");
        }
    }
}
