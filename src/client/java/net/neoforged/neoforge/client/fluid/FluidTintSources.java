/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.fluid;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

public final class FluidTintSources {
    @Nullable
    public static FluidTintSource of(@Nullable BlockTintSource tintSource) {
        if (tintSource == null) {
            return null;
        }
        if (tintSource instanceof FluidTintSource fluidTintSource) {
            return fluidTintSource;
        }
        return new FluidTintSource() {
            @Override
            public int color(BlockState state) {
                return tintSource.color(state);
            }

            @Override
            public int color(FluidState state) {
                return this.color(state.createLegacyBlock());
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return tintSource.colorInWorld(state, level, pos);
            }

            @Override
            public int colorInWorld(FluidState fluidState, BlockState blockState, BlockAndTintGetter level, BlockPos pos) {
                return this.colorInWorld(blockState, level, pos);
            }

            @Override
            public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return tintSource.colorAsTerrainParticle(state, level, pos);
            }
        };
    }

    public static FluidTintSource constant(int color) {
        return constant(color, color);
    }

    public static FluidTintSource constant(int fluidColor, int blockColor) {
        return new FluidTintSource() {
            @Override
            public int color(FluidState state) {
                return fluidColor;
            }

            @Override
            public int color(BlockState state) {
                return blockColor;
            }
        };
    }

    public static FluidTintSource water() {
        return new FluidTintSource() {
            @Override
            public int color(BlockState state) {
                return -1;
            }

            @Override
            public int color(FluidState state) {
                return 0xFF3F76E4;
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return BiomeColors.getAverageWaterColor(level, pos);
            }

            @Override
            public int colorInWorld(FluidState fluidState, BlockState blockState, BlockAndTintGetter level, BlockPos pos) {
                return BiomeColors.getAverageWaterColor(level, pos);
            }
        };
    }

    private FluidTintSources() {}
}
