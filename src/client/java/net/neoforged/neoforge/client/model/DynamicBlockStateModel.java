/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import java.util.List;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.BlockStateModelExtension;

/**
 * Convenience interface for block state models that wish to support the NeoForge-added context
 * in {@link BlockStateModelExtension#collectParts(BlockAndTintGetter, BlockPos, BlockState, RandomSource, List)}.
 */
public interface DynamicBlockStateModel extends BlockStateModel {
    @Override
    @Deprecated
    default void collectParts(RandomSource random, List<BlockModelPart> parts) {
        collectParts(EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), random, parts);
    }

    // Force this to be overriden otherwise this introduces a default cycle between the two overloads.
    @Override
    void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts);
}
