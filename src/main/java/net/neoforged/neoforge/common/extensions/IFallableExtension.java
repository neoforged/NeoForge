/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Fallable;

public interface IFallableExtension {
    /**
     * Called in {@link FallingBlockEntity#tick()} after vanilla processing on both server and client.
     * <p>
     * This is not called in the tick where the entity lands, see {@link Fallable}.
     * 
     * @param level           The current level.
     * @param currentPosition The current position of the entity as a {@link BlockPos}.
     * @param entity          The falling entity.
     */
    default void fallingTick(Level level, BlockPos currentPosition, FallingBlockEntity entity) {}
}
