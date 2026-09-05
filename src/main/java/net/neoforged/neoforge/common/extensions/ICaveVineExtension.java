/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.neoforged.neoforge.common.IHarvestable;
import org.jspecify.annotations.Nullable;

public interface ICaveVineExtension extends IHarvestable {
    private CaveVines self() {
        return (CaveVines) this;
    }

    /// define harvest result from interaction loot table
    @Override
    default List<ItemStack> getHarvestResult(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, @Nullable Entity user, ItemStack tool) {
        if (!CaveVines.hasGlowBerries(state)) return List.of();
        return IHarvestable.getHarvestResultFromInteractionLootTable(level, pos, state, user, tool, BuiltInLootTables.HARVEST_CAVE_VINE);
    }

    /// define block state changes based on interaction
    @Override
    default boolean updateCropAfterHarvest(ServerLevel level, RandomSource random, BlockPos pos, @Nullable Entity user, BlockState state) {
        if (user == null || !user.isSilent()) {
            float pitch = Mth.randomBetween(random, 0.8F, 1.2F);
            level.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, pitch);
        }
        BlockState newState = state.setValue(CaveVines.BERRIES, false);
        level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(user, newState));
        return true;
    }
}
