/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.living;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = { LivingEntityTests.GROUP + ".event", "event" })
public class LivingEntityEventTests {
    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the living swap items event is fired")
    static void livingSwapItems(final DynamicTest test) {
        test.eventListeners().forge().addListener((final LivingSwapItemsEvent.Hands event) -> {
            if (event.getEntity() instanceof Allay) {
                event.setItemSwappedToMainHand(new ItemStack(Items.CHEST));
            }
            test.pass();
        });

        test.onGameTest(helper -> {
            final Allay allay = helper.spawnWithNoFreeWill(EntityType.ALLAY, 1, 2, 1);
            allay.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ACACIA_BOAT));
            allay.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.APPLE));

            allay.handleEntityEvent((byte) 55);

            helper.assertEntityProperty(allay, p -> p.getItemInHand(InteractionHand.MAIN_HAND), "main hand item", new ItemStack(Items.CHEST), ItemStack::isSameItem);
            helper.assertEntityProperty(allay, p -> p.getItemInHand(InteractionHand.OFF_HAND), "off-hand item", new ItemStack(Items.ACACIA_BOAT), ItemStack::isSameItem);

            helper.runAfterDelay(5, () -> {
                helper.killAllEntitiesOfClass(Allay.class);
                helper.succeed();
            });
        });
    }
}
