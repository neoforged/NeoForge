/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.DimensionTransitionScreen;
import org.jetbrains.annotations.ApiStatus;

/**
 * <p>Event for registering screen effects when transitioning across dimensions.</p>
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterDimensionTransitionEvent extends Event implements IModBusEvent {
    private final Map<ResourceKey<Level>, DimensionTransitionScreen> toEffects;
    private final Map<ResourceKey<Level>, DimensionTransitionScreen> fromEffects;

    @ApiStatus.Internal
    public RegisterDimensionTransitionEvent(Map<ResourceKey<Level>, DimensionTransitionScreen> toEffects, Map<ResourceKey<Level>, DimensionTransitionScreen> fromEffects) {
        this.toEffects = toEffects;
        this.fromEffects = fromEffects;
    }

    /**
     * Registers a dimension transition when traveling to a dimension.
     */
    public void registerTo(ResourceKey<Level> dimension, DimensionTransitionScreen screen) {
        this.toEffects.put(dimension, screen);
    }

    /**
     * Registers a dimension transition when traveling from a dimension.
     */
    public void registerFrom(ResourceKey<Level> dimension, DimensionTransitionScreen screen) {
        this.fromEffects.put(dimension, screen);
    }

    /**
     * Registers a dimension transition when traveling to or from a dimension.
     */
    public void registerBiDirectional(ResourceKey<Level> dimension, DimensionTransitionScreen screen) {
        this.toEffects.put(dimension, screen);
        this.fromEffects.put(dimension, screen);
    }
}
