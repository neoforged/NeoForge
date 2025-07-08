/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.crafting;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "crafting.event")
public class CraftingEventTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that ItemSmeltedEvent is fired correctly")
    static void itemSmeltedEventTest(final DynamicTest test) {
        AtomicInteger timesFired = new AtomicInteger(0);
        test.whenEnabled(listeners -> {
            listeners.forge().addListener((final PlayerEvent.ItemSmeltedEvent event) -> {
                timesFired.incrementAndGet();
                var removed = event.getAmountRemoved();
                if (removed != 32) {
                    test.fail("Test should be removing half of a stack, yet extracted a different amount");
                }
            });
        });
        test.onGameTest(helper -> {
            helper.setBlock(BlockPos.ZERO, Blocks.FURNACE);
            var be = helper.getBlockEntity(BlockPos.ZERO, FurnaceBlockEntity.class);
            helper.assertNotNull(be, "FurnaceBlockEntity was not found for furnace position");
            // Slot 2 is the result slot
            be.setItem(2, new ItemStack(Items.IRON_INGOT, 64));
            var player = helper.makeTickingMockServerPlayerInLevel(GameType.CREATIVE);
            player.openMenu(be);
            // Test that right-clicking half of the stack out of the FurnaceResultSlot functions as expected
            player.containerMenu.clicked(2, InputConstants.MOUSE_BUTTON_RIGHT, ClickType.PICKUP, player);
            helper.assertTrue(timesFired.getPlain() == 1, "Event was not fired the expected number of times for right-click pickup. Fired: " + timesFired.getPlain());
            player.containerMenu.setCarried(ItemStack.EMPTY);
            // Test that shift-left-clicking the rest of the stack out works (should only fire once, not twice)
            player.containerMenu.clicked(2, InputConstants.MOUSE_BUTTON_LEFT, ClickType.QUICK_MOVE, player);
            helper.assertTrue(timesFired.getPlain() == 2, "Event was not fired the expected number of times for shift-left-click quick-move. Fired: " + timesFired.getPlain());
            // The slot is now empty, this should not fire the event
            player.containerMenu.clicked(2, InputConstants.MOUSE_BUTTON_LEFT, ClickType.QUICK_MOVE, player);
            helper.assertTrue(timesFired.getPlain() == 2, "Event fired for an empty slot, which should not happen.");
            helper.succeed();
        });
    }
}
