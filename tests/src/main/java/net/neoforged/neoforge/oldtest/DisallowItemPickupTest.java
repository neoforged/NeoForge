/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.item.ItemAllowPickupEvent;

/**
 * Basic test mod to test the new {@linkplain ItemAllowPickupEvent}.
 * <p>
 * When this test is enabled the following should be true
 * <ul>
 * <li>Items ontop of {@linkplain Tags.Blocks#STONES} should not be allowed to be picked up</li>
 * </ul>
 */
@Mod(DisallowItemPickupTest.ID)
public final class DisallowItemPickupTest {
    public static final String ID = "disallow_item_pickup_test";
    private static final boolean ENABLED = true;

    public DisallowItemPickupTest() {
        NeoForge.EVENT_BUS.addListener(ItemAllowPickupEvent.class, event -> {
            // deny item pickups when item is on #stone
            if (ENABLED && event.getEntity().getBlockStateOn().is(Tags.Blocks.STONES))
                event.setCanceled(true);
        });
    }
}
