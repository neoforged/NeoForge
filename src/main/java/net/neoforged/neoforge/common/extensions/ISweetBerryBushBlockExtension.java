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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.neoforged.neoforge.common.IHarvestable;
import org.jspecify.annotations.Nullable;

public interface ISweetBerryBushBlockExtension extends IHarvestable {
    private SweetBerryBushBlock self() {
        return (SweetBerryBushBlock) this;
    }

    /// define harvest result from interaction loot table
    @Override
    default List<ItemStack> getHarvestResult(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, @Nullable Entity user, ItemStack tool) {
        if (state.getValue(SweetBerryBushBlock.AGE) <= 1) return List.of();
        return IHarvestable.getHarvestResultFromInteractionLootTable(level, pos, state, user, tool, BuiltInLootTables.HARVEST_SWEET_BERRY_BUSH);
    }

    /// define block state changes based on interaction
    @Override
    default boolean updateCropAfterHarvest(ServerLevel level, RandomSource random, BlockPos pos, @Nullable Entity user, BlockState state) {
        if (user == null || !user.isSilent()) {
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + random.nextFloat() * 0.4F);
        }
        BlockState newState = state.setValue(SweetBerryBushBlock.AGE, 1);
        level.setBlock(pos, newState, 2);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(user, newState));
        return true;
    }
}
