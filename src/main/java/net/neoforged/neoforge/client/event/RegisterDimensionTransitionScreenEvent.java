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
import net.neoforged.neoforge.client.DimensionTransitionScreenManager.ReceivingLevelScreenFactory;
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
    private final Map<Pair<ResourceKey<Level>, ResourceKey<Level>>, ReceivingLevelScreenFactory> conditionalDimensionEffects;
    private final Map<ResourceKey<Level>, ReceivingLevelScreenFactory> toEffects;
    private final Map<ResourceKey<Level>, ReceivingLevelScreenFactory> fromEffects;

    @ApiStatus.Internal
    public RegisterDimensionTransitionScreenEvent(Map<Pair<ResourceKey<Level>, ResourceKey<Level>>, ReceivingLevelScreenFactory> conditionalDimensionEffects,
            Map<ResourceKey<Level>, ReceivingLevelScreenFactory> toEffects,
            Map<ResourceKey<Level>, ReceivingLevelScreenFactory> fromEffects) {
        this.conditionalDimensionEffects = conditionalDimensionEffects;
        this.toEffects = toEffects;
        this.fromEffects = fromEffects;
    }

    /**
     * Registers a dimension transition when traveling to a dimension.
     *
     * @return {@code true} if the screen was registered, {@code false} otherwise.
     */
    public boolean registerIncomingEffect(ResourceKey<Level> dimension, ReceivingLevelScreenFactory screen) {
        return this.toEffects.putIfAbsent(dimension, screen) == null;
    }

    /**
     * Registers a dimension transition when traveling from a dimension.
     *
     * @return {@code true} if the screen was registered, {@code false} otherwise.
     */
    public boolean registerOutgoingEffect(ResourceKey<Level> dimension, ReceivingLevelScreenFactory screen) {
        return this.fromEffects.putIfAbsent(dimension, screen) == null;
    }

    /**
     * Registers a dimension transition when traveling to a dimension from a certain dimension.
     * This registration method takes priority over the normal to and from dimension checks.
     *
     * @return {@code true} if the screen was registered, {@code false} otherwise.
     */
    public boolean registerConditionalEffect(ResourceKey<Level> toDimension, ResourceKey<Level> fromDimension, ReceivingLevelScreenFactory screen) {
        return this.conditionalDimensionEffects.putIfAbsent(Pair.of(toDimension, fromDimension), screen) == null;
    }
}
