/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;

/**
 * Intended for mods to help mark if a modded item allows for placing modded plants of any kind or size.
 * Also allows Villagers to properly plant items with this interface on and villagerCanPlantItem returning true.
 * <p></p>
 * People trying to plant modded items should check if item implements this interface.
 * Then check for true from canPlacePlantAtPosition first before calling spawnPlantAtPosition.
 * Implementers of this interface would ideally call canSurvive on their plant block in canPlacePlantAtPosition.
 * <p></p>
 * (Note: Vanilla plantable items are BlockItem where you can get their states directly and call canSurvive)
 */
public interface SpecialPlantable {
    /**
     * Checks location if this item can spawn a plant with the given direction attachment point.
     * 
     * @return If plant can spawn
     */
    boolean canPlacePlantAtPosition(ItemStack itemStack, LevelReader level, BlockPos pos, @Nullable Direction direction);

    /**
     * Spawns the plant with the given direction attachment point at location.
     * Ideally called after canPlacePlantAtPosition returns true.
     */
    void spawnPlantAtPosition(ItemStack itemStack, LevelAccessor level, BlockPos pos, @Nullable Direction direction);

    /**
     * Whether Villagers can pick up this item and plant it down on any block that extends FarmBlock.
     */
    default boolean villagerCanPlantItem(Villager villager) {
        return false;
    };
}
