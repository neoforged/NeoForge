/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world.poi;

import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired in order to add additional {@link BlockState}s to foreign {@link PoiType}s.
 * <p>
 * This event is fired on the mod-specific event bus.
 */
public final class ExtendPoiTypesEvent extends Event implements IModBusEvent {
    private final BiConsumer<ResourceKey<PoiType>, Set<BlockState>> registrar;

    ExtendPoiTypesEvent(BiConsumer<ResourceKey<PoiType>, Set<BlockState>> registrar) {
        this.registrar = registrar;
    }

    /**
     * Add the provided {@link Block}'s {@link BlockState}s to the {@link PoiType#matchingStates} of the provided {@link PoiType}
     *
     * @param typeKey The {@link ResourceKey} of the {@link PoiType} to append the states to
     * @param block   The {@link Block} whose {@link BlockState}s to append to the PoI type
     */
    public void addBlockToPoi(ResourceKey<PoiType> typeKey, Block block) {
        this.addStatesToPoi(typeKey, Set.copyOf(block.getStateDefinition().getPossibleStates()));
    }

    /**
     * Add the provided {@link BlockState}s to the {@link PoiType#matchingStates} of the provided {@link PoiType}
     *
     * @param typeKey The {@link ResourceKey} of the {@link PoiType} to append the states to
     * @param states  The {@link BlockState}s to append to the PoI type
     */
    public void addStatesToPoi(ResourceKey<PoiType> typeKey, Set<BlockState> states) {
        this.registrar.accept(typeKey, states);
    }
}
