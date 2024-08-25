/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.datafixers.util.Pair;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.DimensionTransitionScreenManager;
import org.jetbrains.annotations.ApiStatus;

/**
 * <p>Event for registering screen effects when transitioning across dimensions.
 * Note that there is a priority order when it comes to what screens are displayed: <br>
 * - Using registerConditionalEffect has priority over the usual transition effects, and will only fire when travelling to the specified dimension coming from a certain dimension. <br>
 * - If a dimension has a screen that displays when entering it, that will have priority over a dimension that has one when you leave it. </p>
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterDimensionTransitionScreenEvent extends Event implements IModBusEvent {
    private final Map<Pair<ResourceKey<Level>, ResourceKey<Level>>, DimensionTransitionScreenManager.ReceivingLevelScreenFactory> conditionalDimensionEffects;
    private final Map<ResourceKey<Level>, DimensionTransitionScreenManager.ReceivingLevelScreenFactory> toEffects;
    private final Map<ResourceKey<Level>, DimensionTransitionScreenManager.ReceivingLevelScreenFactory> fromEffects;

    @ApiStatus.Internal
    public RegisterDimensionTransitionScreenEvent(Map<Pair<ResourceKey<Level>, ResourceKey<Level>>, DimensionTransitionScreenManager.ReceivingLevelScreenFactory> conditionalDimensionEffects, Map<ResourceKey<Level>, DimensionTransitionScreenManager.ReceivingLevelScreenFactory> toEffects, Map<ResourceKey<Level>, DimensionTransitionScreenManager.ReceivingLevelScreenFactory> fromEffects) {
        this.conditionalDimensionEffects = conditionalDimensionEffects;
        this.toEffects = toEffects;
        this.fromEffects = fromEffects;
    }

    /**
     * Registers a dimension transition when traveling to a dimension.
     */
    public void registerIncomingEffect(ResourceKey<Level> dimension, DimensionTransitionScreenManager.ReceivingLevelScreenFactory screen) {
        this.toEffects.put(dimension, screen);
    }

    /**
     * Registers a dimension transition when traveling from a dimension.
     */
    public void registerOutgoingEffect(ResourceKey<Level> dimension, DimensionTransitionScreenManager.ReceivingLevelScreenFactory screen) {
        this.fromEffects.put(dimension, screen);
    }

    /**
     * Registers a dimension transition when traveling to or from a dimension.
     */
    public void registerBiDirectionalEffect(ResourceKey<Level> dimension, DimensionTransitionScreenManager.ReceivingLevelScreenFactory screen) {
        this.toEffects.put(dimension, screen);
        this.fromEffects.put(dimension, screen);
    }

    /**
     * Registers a dimension transition when traveling to a dimension from a certain dimension.
     * This registration method takes priority over the normal to and from dimension checks.
     */
    public void registerConditionalEffect(ResourceKey<Level> toDimension, ResourceKey<Level> fromDimension, DimensionTransitionScreenManager.ReceivingLevelScreenFactory screen) {
        this.conditionalDimensionEffects.put(Pair.of(toDimension, fromDimension), screen);
    }
}
