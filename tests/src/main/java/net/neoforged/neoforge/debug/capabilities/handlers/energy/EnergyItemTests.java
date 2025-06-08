/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers.energy;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyHandler;
import net.neoforged.neoforge.transfer.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.contexts.PlayerItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.energy.EnergyBufferAttachment;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = EnergyTestsSetup.GROUP_ID, idPrefix = "handler.energy.item.")
public class EnergyItemTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that EnergyComponentHandler can read and write from a data component as well as revert changes")
    public static void testItemEnergy(ExtendedGameTestHelper helper) {
        ItemStack stack = EnergyTestsSetup.BATTERY.toStack();
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        IItemContext context = PlayerItemContext.ofHand(player, InteractionHand.MAIN_HAND);
        var energy = context.getCapability(EnergyHandler.ITEM);
        helper.assertNotNull(energy, "Capability must be present");

        assert energy != null;
        var current = EnergyHandlerUtil.getAmount(energy);

        EnergyBufferAttachment.builder(0, 1000).build();

        long storedMax = EnergyTestsSetup.MAX_CAPACITY;
        helper.assertValueEqual(current, storedMax, "Default stored energy should be equal to the max capacity.");

        try (var transaction = Transaction.open(TransactionContext.ROOT)) {
            helper.assertValueEqual(energy.extract(EnergyTestsSetup.MAX_CAPACITY, transaction), EnergyTestsSetup.MAX_CAPACITY, "Extracted energy should be equal to the target value.");
            helper.assertValueEqual(EnergyHandlerUtil.getAmount(energy), 0L, "Post-extraction energy stored should be zero.");

            //The default builder is 1% of the max capacity, this is trivially set by `builder.maxInsertRate(targetValue)`
            helper.assertValueEqual(energy.insert(EnergyTestsSetup.MAX_CAPACITY, transaction), Mth.ceil(EnergyTestsSetup.MAX_CAPACITY * 0.01f), "Received energy should be equal to the target value.");
            helper.assertValueEqual(EnergyHandlerUtil.getAmount(energy), (long) Mth.ceil(EnergyTestsSetup.MAX_CAPACITY * 0.01f), "Post-insertion energy stored should be max insert.");
            //we skip committing to test the value reverts to full again
        }

        helper.assertValueEqual(EnergyHandlerUtil.getAmount(energy), storedMax, "Reverted energy stored should be max");

        helper.succeed();
    }
}
