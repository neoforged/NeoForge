/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@Mod(FinalizeSpawnTest.ID)
public class FinalizeSpawnTest {
    public static final String ID = "finalize_spawn_fix_test";
    public static final boolean ENABLED = false;

    public FinalizeSpawnTest() {
        NeoForge.EVENT_BUS.addListener(FinalizeSpawnEvent.class, event -> {
            var entityType = EntityType.WANDERING_TRADER;
            var entity = event.getEntity();

            // testing with wandering trader to validate the following
            // - ambient sounds dont play for cancelled mob spawns
            // - llamas dont spawn for cancelled trader spawns
            // but this test is not specific to wandering traders
            if (ENABLED && entity.getType() == entityType) {
                // some debug message to say entity spawn was cancelled
                event.getLevel().players().forEach(player -> player.displayClientMessage(Component.literal("Cancelled %s spawn @ %s".formatted(entityType.getDescription().getString(), entity.position())), true));
                event.setSpawnCancelled(true);
            }
        });
    }
}
