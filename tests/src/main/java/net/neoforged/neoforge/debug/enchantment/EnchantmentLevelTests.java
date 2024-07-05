/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.enchantment;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
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
            ItemEnchantments.Mutable enchants = e.getEnchantments();

            // Increase the level of sharpness by 1 in all cases.
            e.getHolder(Enchantments.SHARPNESS).ifPresent(holder -> {
                if (e.isTargetting(holder)) {
                    enchants.set(holder, enchants.getLevel(holder) + 1);
                }
            });

            // Increase the level of fire aspect by 1 if the stack contains specific NBT.
            e.getHolder(Enchantments.FIRE_ASPECT).ifPresent(holder -> {
                if (e.isTargetting(holder)) {
                    if (e.getStack().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains("boost_fire_aspect")) {
                        enchants.set(holder, enchants.getLevel(holder) + 1);
                    }
                }
            });
        });

        test.onGameTest(helper -> {
            var enchants = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            ItemStack stack = new ItemStack(Items.IRON_SWORD);

            helper.assertTrue(stack.getEnchantmentLevel(enchants.getHolderOrThrow(Enchantments.FIRE_ASPECT)) == 0, "Fire Aspect level was not zero");
            helper.assertTrue(stack.getEnchantmentLevel(enchants.getHolderOrThrow(Enchantments.SHARPNESS)) == 1, "Sharpness level was not one");

            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put("boost_fire_aspect", new CompoundTag())); // Creates the sub-compound "boost_fire_aspect" which will trigger the event listener above.
            stack.enchant(enchants.getHolderOrThrow(Enchantments.SHARPNESS), 5);

            helper.assertTrue(stack.getEnchantmentLevel(enchants.getHolderOrThrow(Enchantments.FIRE_ASPECT)) == 1, "Fire Aspect level was not one");
            helper.assertTrue(stack.getEnchantmentLevel(enchants.getHolderOrThrow(Enchantments.SHARPNESS)) == 6, "Sharpness level was not six");

            helper.succeed();
        });
    }
}
