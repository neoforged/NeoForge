/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod("farmland_trample_test")
@Mod.EventBusSubscriber
public class FarmlandTrampleEventTest {
    @SubscribeEvent
    public static void onFarmlandTrampled(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getEntity().isCrouching())
            event.setCanceled(true);
    }
}
