/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.entity;

import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

@Mod(EntityTeleportTest.MODID)
public class EntityTeleportTest {
    public static final String MODID = "entity_teleport_test";

    @EventBusSubscriber(modid = EntityTeleportTest.MODID)
    public static class TeleportEvents {
        @SubscribeEvent
        static void onTeleportCommand(EntityTeleportEvent.TeleportCommand event) {
            if (event.getEntity() instanceof Pig && event.getTargetLevel().dimension() != Level.END) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        static void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl event) {
            if (event.getEntity() instanceof Player && event.getTargetLevel().dimension() != Level.OVERWORLD) {
                event.setCanceled(true);
            }
        }
    }
}
