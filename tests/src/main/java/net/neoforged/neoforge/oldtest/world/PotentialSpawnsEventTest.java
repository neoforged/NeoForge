/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * <p>This tests net.minecraftforge.event.world.World.LevelEvent.PotentialSpawns. If ENABLED is set to true,
 * this test mod uses the PotentialSpawns event to prevent mobs in the MONSTER mob category from spawning if the
 * game difficulty is set to anything other than hard.</p>
 */
@Mod("potentialspawnsevent_test")
public class PotentialSpawnsEventTest {
    public static final boolean ENABLED = false;

    public PotentialSpawnsEventTest() {
        if (ENABLED) {
            NeoForge.EVENT_BUS.addListener(PotentialSpawnsEventTest::onlySpawnHostileMobs);
        }
    }

    public static void onlySpawnHostileMobs(LevelEvent.PotentialSpawns event) {
        LevelAccessor level = event.getLevel();
        BlockPos pos = event.getPos();
        Difficulty difficulty = level.getCurrentDifficultyAt(pos).getDifficulty();
        MobCategory category = event.getMobCategory();

        if (category == MobCategory.MONSTER && difficulty != Difficulty.HARD) {
            event.setCanceled(true);
        }
    }
}
