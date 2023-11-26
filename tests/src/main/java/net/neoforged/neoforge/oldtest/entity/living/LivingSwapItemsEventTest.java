/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.entity.living;

import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("living_swap_items_event_test")
public class LivingSwapItemsEventTest {
    public static final boolean ENABLE = true;
    public static final Logger LOGGER = LogManager.getLogger();

    public LivingSwapItemsEventTest() {
        if (ENABLE)
            NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void livingSwapItems(LivingSwapItemsEvent.Hands event) {
        LOGGER.info("{} swapping hands. To offhand: {}, to main hand: {}", event.getEntity(), event.getItemSwappedToOffHand(), event.getItemSwappedToMainHand());
        if (event.getEntity().getMainHandItem().is(Items.DIAMOND))
            event.setCanceled(true);
    }
}
