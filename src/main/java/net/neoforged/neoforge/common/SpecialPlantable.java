/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;

/**
 * Intended for mods to help mark if a modded item allows for placing modded plants of any kind or size.
 * Also allows Villagers to properly plant items with this interface on if the Villager has this item in inventory.
 * <p></p>
 * People trying to plant modded items should check if item implements this interface.
 * Then check for true from canPlacePlantAtPosition first before calling spawnPlantAtPosition.
 * Implementers of this interface would ideally call canSurvive on their plant block in canPlacePlantAtPosition.
 * <p></p>
 * (Note: Vanilla plantable items are BlockItem where you can get their states directly and call canSurvive)
 */
public interface SpecialPlantable {
    Boolean canPlacePlantAtPosition(LevelReader level, BlockPos pos, @Nullable Direction direction);

    void spawnPlantAtPosition(LevelReader level, BlockPos pos, @Nullable Direction direction);
}
