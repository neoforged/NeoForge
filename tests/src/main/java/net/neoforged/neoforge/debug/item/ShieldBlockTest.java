/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.ShieldBlockEvent;

/**
 * The ShieldBlockTest is the test mod for the ShieldBlockEvent.
 * If successful, this handler will trigger when an arrow is blocked by a player.
 * The event will give the arrow to the player, and make the player receive half of the damage (instead of zero damage).
 * Note this just gives them a normal arrow, retrieving the true arrow requires some reflection.
 */
@Mod(ShieldBlockTest.MOD_ID)
@Mod.EventBusSubscriber
public class ShieldBlockTest {
    static final String MOD_ID = "shield_block_event";

    @SubscribeEvent
    public static void shieldBlock(ShieldBlockEvent event) {
        if (event.getDamageSource().getDirectEntity() instanceof AbstractArrow arrow && event.getEntity() instanceof Player player) {
            player.getInventory().add(new ItemStack(Items.ARROW));
            event.setBlockedDamage(event.getOriginalBlockedDamage() / 2);
            arrow.discard();
        }
    }
}
