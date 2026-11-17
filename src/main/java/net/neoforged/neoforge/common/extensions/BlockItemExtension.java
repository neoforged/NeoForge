/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface BlockItemExtension {
    /// Called after the block was placed by this block item and after [Block#setPlacedBy(Level, BlockPos, BlockState, LivingEntity, ItemStack)] was called.
    ///
    /// @param context The place context used to place the block
    /// @param level   The level the block was placed in
    /// @param pos     The position the block was placed at
    /// @param state   The state of the placed block
    /// @param placer  The player who placed the block, if available
    /// @param stack   The stack used to place the block
    default void onBlockPlacedBy(BlockPlaceContext context, Level level, BlockPos pos, BlockState state, @Nullable Player placer, ItemStack stack) {}
}
