/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.gametest;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unchecked")
public interface TemplateBuilderHelper<T extends TemplateBuilderHelper<T>> {
    T set(int x, int y, int z, BlockState state, @Nullable CompoundTag nbt);

    default T fill(int x, int y, int z, int toX, int toY, int toZ, Block block) {
        return fill(x, y, z, toX, toY, toZ, block.defaultBlockState());
    }

    default T fill(int x, int y, int z, int toX, int toY, int toZ, BlockState state) {
        return fill(x, y, z, toX, toY, toZ, state, null);
    }

    default T fill(int x, int y, int z, int toX, int toY, int toZ, BlockState state, @Nullable CompoundTag nbt) {
        for (int x1 = x; x1 <= toX; x1++) {
            for (int y1 = y; y1 <= toY; y1++) {
                for (int z1 = z; z1 <= toZ; z1++) {
                    set(x1, y1, z1, state, nbt);
                }
            }
        }
        return (T) this;
    }

    default T set(int x, int y, int z, BlockState state) {
        return set(x, y, z, state, null);
    }

    default T placeFloorLever(int x, int y, int z, boolean powered) {
        set(x, y, z, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR).setValue(LeverBlock.POWERED, powered), null);
        set(x, y - 1, z, Blocks.STONE.defaultBlockState(), null);
        return (T) this;
    }

    default T placeSustainedWater(int x, int y, int z, BlockState surrounding) {
        set(x, y, z, Blocks.WATER.defaultBlockState(), null);
        return placeWaterConfinement(x, y, z, surrounding, true);
    }

    default T placeWaterConfinement(int x, int y, int z, BlockState surrounding, boolean bottom) {
        if (bottom) {
            set(x, y - 1, z, surrounding, null);
        }
        set(x, y, z + 1, surrounding, null);
        set(x, y, z - 1, surrounding, null);
        set(x + 1, y, z, surrounding, null);
        set(x - 1, y, z, surrounding, null);
        return (T) this;
    }
}
