/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.debug.entity.player;

import net.minecraftforge.event.entity.player.PlayerSpawnPhantomsEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("player_spawn_phantoms_event_test")
@Mod.EventBusSubscriber()
public class PlayerSpawnPhantomsEventTest
{

    private static final boolean ENABLE = false;

    @SubscribeEvent
    public static void onPhantomsSpawn(PlayerSpawnPhantomsEvent event)
    {
        if (!ENABLE) return;
        event.setResult(Event.Result.DENY);
    }

}
