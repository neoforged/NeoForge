/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.debug.misc;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GetEnchantmentLevelEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Tests {@link GetEnchantmentLevelEvent} by changing the level of fortune to 100 when holding an iron pickaxe named "Fortuna"
 */
@Mod(GetEnchantmentLevelEventTest.MOD_ID)
public class GetEnchantmentLevelEventTest
{
    private static final boolean ENABLED = true;
    static final String MOD_ID = "get_ench_level_test";

    public GetEnchantmentLevelEventTest()
    {
        if (ENABLED)
        {
            MinecraftForge.EVENT_BUS.addListener(this::enchLevels);
        }
    }

    private void enchLevels(GetEnchantmentLevelEvent e)
    {
        ItemStack s = e.getStack();
        if (s.getItem() == Items.IRON_PICKAXE && s.hasCustomHoverName() && s.getHoverName().getString().equalsIgnoreCase("fortuna"))
        {
            e.getEnchantments().put(Enchantments.BLOCK_FORTUNE, 100);
        }
    }
}
