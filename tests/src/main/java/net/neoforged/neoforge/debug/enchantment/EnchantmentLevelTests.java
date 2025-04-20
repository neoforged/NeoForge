/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.enchantment;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEnchantItemEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = EnchantmentLevelTests.GROUP)
public class EnchantmentLevelTests {
    public static final String GROUP = "enchantment";

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
            var enchants = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            ItemStack stack = new ItemStack(Items.IRON_SWORD);

            helper.assertTrue(stack.getEnchantmentLevel(enchants.getOrThrow(Enchantments.FIRE_ASPECT)) == 0, "Fire Aspect level was not zero");
            helper.assertTrue(stack.getEnchantmentLevel(enchants.getOrThrow(Enchantments.SHARPNESS)) == 1, "Sharpness level was not one");

            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put("boost_fire_aspect", new CompoundTag())); // Creates the sub-compound "boost_fire_aspect" which will trigger the event listener above.
            stack.enchant(enchants.getOrThrow(Enchantments.SHARPNESS), 5);

            helper.assertTrue(stack.getEnchantmentLevel(enchants.getOrThrow(Enchantments.FIRE_ASPECT)) == 1, "Fire Aspect level was not one");
            helper.assertTrue(stack.getEnchantmentLevel(enchants.getOrThrow(Enchantments.SHARPNESS)) == 6, "Sharpness level was not six");

            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if the PlayerEnchantedItemEvent fired.")
    static void playerEnchantItemTest(final DynamicTest test, final RegistrationHelper reg) {
        test.eventListeners().forge().addListener((PlayerEnchantItemEvent event) -> {
            event.getEnchantedItem().setDamageValue(1); //change a value we can reference in our test sequence
        });
        final BlockPos pos = new BlockPos(1, 2, 1);
        test.onGameTest(helper -> helper.startSequence(helper::makeMockPlayer)
                //ensure the player has enough experience to perform an enchantment
                .thenExecute(player -> player.experienceLevel = 30)
                //place a table into the world to hold our container
                .thenExecute(player -> helper.setBlock(pos, Blocks.ENCHANTING_TABLE))
                //open the menu container on the player
                .thenExecute(player -> player.containerMenu = Objects.requireNonNull(Objects.requireNonNull(
                        Objects.requireNonNull(helper.getBlockEntity(pos, EnchantingTableBlockEntity.class)).getBlockState()
                                .getMenuProvider(player.level(), helper.absolutePos(pos)))
                        .createMenu(1, player.getInventory(), player)))
                //simulate putting an iron sword in the first slot
                .thenExecute(player -> player.containerMenu.setItem(0, player.containerMenu.getStateId(), new ItemStack(Items.IRON_SWORD)))
                //simulate putting the lapis into the second slot
                .thenExecute(player -> player.containerMenu.setItem(1, player.containerMenu.getStateId(), new ItemStack(Items.LAPIS_LAZULI)))
                //ensure the lapis count is enough to pay for any enchanting costs
                .thenExecute(player -> player.containerMenu.getSlot(1).getItem().setCount(64))
                //simulate clicking the button on the screen.
                .thenExecute(player -> player.containerMenu.clickMenuButton(player, 1))
                //verify the event listener has set the damage value of our item to one
                .thenWaitUntil(player -> helper.assertTrue(player.containerMenu.getSlot(0).getItem().getDamageValue() == 1, "Enchanted item damage not set by the event"))
                .thenSucceed());
    }
}
