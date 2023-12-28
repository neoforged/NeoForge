/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.item;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = { ItemTests.GROUP + ".event", "event" })
public class ItemEventTests {
    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the ItemAttributeModifierEvent allows modifying attributes")
    static void itemAttributeModifier(final DynamicTest test) {
        test.eventListeners().forge().addListener((final ItemAttributeModifierEvent event) -> {
            if (event.getSlotType() == EquipmentSlot.MAINHAND && event.getItemStack().getItem() == Items.APPLE) {
                event.addModifier(Attributes.ARMOR, new AttributeModifier(test.createModId(), 10f, AttributeModifier.Operation.ADDITION));
            } else if (event.getSlotType() == EquipmentSlot.CHEST && event.getItemStack().is(Items.GOLDEN_CHESTPLATE)) {
                event.clearModifiers();
            }
        });

        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(player -> player.setItemSlot(EquipmentSlot.CHEST, Items.GOLDEN_CHESTPLATE.getDefaultInstance()))
                .thenExecuteAfter(1, player -> helper.assertEntityProperty(
                        player,
                        LivingEntity::getArmorValue,
                        "armor",
                        0)) // Expect the chestplate to give no armor

                .thenExecute(player -> player.setItemSlot(EquipmentSlot.MAINHAND, Items.APPLE.getDefaultInstance()))
                .thenExecuteAfter(1, player -> helper.assertEntityProperty(
                        player,
                        LivingEntity::getArmorValue,
                        "armor",
                        10)) // 10 armor from the apple in main hand
                .thenSucceed());
    }
}
