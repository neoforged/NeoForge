/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.enchantment;

import java.util.Map;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = EnchantmentLevelTests.GROUP)
public class EnchantmentLevelTests {
    public static final String GROUP = "enchantment.level";

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests whether the GetEnchantmentLevelEvent can properly modify enchantment levels.")
    static void getEnchLevelEvent(final DynamicTest test, final RegistrationHelper reg) {
        test.eventListeners().forge().addListener((GetEnchantmentLevelEvent e) -> {
            Map<Enchantment, Integer> enchants = e.getEnchantments();

            // Increase the level of sharpness by 1 in all cases.
            if (e.isTargetting(Enchantments.SHARPNESS)) {
                enchants.put(Enchantments.SHARPNESS, enchants.getOrDefault(Enchantments.SHARPNESS, 0) + 1);
            }

            // Increase the level of fire aspect by 1 if the stack contains specific NBT.
            if (e.isTargetting(Enchantments.FIRE_ASPECT)) {
                if (e.getStack().getTagElement("boost_fire_aspect") != null) {
                    enchants.put(Enchantments.FIRE_ASPECT, enchants.getOrDefault(Enchantments.FIRE_ASPECT, 0) + 1);
                }
            }
        });

        test.onGameTest(helper -> {

            ItemStack stack = new ItemStack(Items.IRON_SWORD);

            helper.assertTrue(stack.getEnchantmentLevel(Enchantments.FIRE_ASPECT) == 0, "Fire Aspect level was not zero");
            helper.assertTrue(stack.getEnchantmentLevel(Enchantments.SHARPNESS) == 1, "Sharpness level was not one");

            stack.getOrCreateTagElement("boost_fire_aspect"); // Creates the sub-compound "boost_fire_aspect" which will trigger the event listener above.
            stack.enchant(Enchantments.SHARPNESS, 5);

            helper.assertTrue(stack.getEnchantmentLevel(Enchantments.FIRE_ASPECT) == 1, "Fire Aspect level was not one");
            helper.assertTrue(stack.getEnchantmentLevel(Enchantments.SHARPNESS) == 6, "Sharpness level was not six");

            helper.succeed();
        });
    }
}
