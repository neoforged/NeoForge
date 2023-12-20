/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IBlockCapabilityProvider<T, C> {
    /**
     * Returns the capability, or {@code null} if not available.
     *
     * <p><b>If a previously returned capability is not valid anymore, or if a new capability is available,
     * {@link Level#invalidateCapabilities(BlockPos)} MUST be called to notify the caches (see below).</b>
     *
     * <p>Capabilities are automatically invalidated by NeoForge in the following cases:
     * <ul>
     * <li>Chunk loads and unloads.</li>
     * <li>Block entity loads and unloads.</li>
     * <li>Block entity placement and destruction.</li>
     * </ul>
     * In ALL other cases, it is the <b>responsibility of the modder</b> to call {@link Level#invalidateCapabilities(BlockPos)}.
     * For example:
     * <ul>
     * <li>If the configuration of a block entity changes.</li>
     * <li>If a plain block is placed or changes state, by overriding {@link BlockBehaviour#onPlace(BlockState, Level, BlockPos, BlockState, boolean) onPlace}.
     * Be careful that if you don't invalidate for every state change, you should not capture the {@code state} parameter because the state might change!</li>
     * <li>If a plain block is removed, by overriding {@link BlockBehaviour#onRemove(BlockState, Level, BlockPos, BlockState, boolean) onRemove}.</li>
     * </ul>
     *
     * @param level       The level.
     * @param pos         The position.
     * @param state       The block state.
     * @param blockEntity The block entity, if present. {@code null} means that there is no block entity at the target position.
     * @param context     Extra context, capability-dependent.
     */
    @Nullable
    T getCapability(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, C context);
}
