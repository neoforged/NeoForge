/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Manager for light values controlled by dynamic data in {@link BlockEntity}s.
 */
public interface AuxiliaryLightManager {
    /**
     * Set the light value at the given position to the given value
     */
    void setLightAt(BlockPos pos, int value);

    /**
     * Remove the light value at the given position
     */
    default void removeLightAt(BlockPos pos) {
        setLightAt(pos, 0);
    }

    /**
     * {@return the light value at the given position or 0 if none is present}
     */
    int getLightAt(BlockPos pos);
}
