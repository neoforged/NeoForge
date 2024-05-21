/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import com.mojang.datafixers.util.Either;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import org.jetbrains.annotations.Nullable;

public interface IOwnedSpawner {
    /**
     * Returns the block entity or entity which owns this spawner object.
     * <p>
     * For a {@link BaseSpawner}, this is the {@link MobSpawnerBlockEntity} or {@link MinecartSpawner}.
     * <p>
     * For a {@link TrialSpawner}, this is the {@link TrialSpawnerBlockEntity}.
     */
    @Nullable
    Either<BlockEntity, Entity> getOwner();
}
