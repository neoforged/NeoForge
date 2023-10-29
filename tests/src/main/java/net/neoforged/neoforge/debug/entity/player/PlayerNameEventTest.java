/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.player;

import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod("player_name_event_test")
@Mod.EventBusSubscriber()
public class PlayerNameEventTest {
    private static final boolean ENABLE = false;

    @SubscribeEvent
    public static void onPlayerNameEvent(PlayerEvent.NameFormat event) {
        if (!ENABLE) return;
        event.setDisplayname(Component.literal("Test Name"));
    }
}
