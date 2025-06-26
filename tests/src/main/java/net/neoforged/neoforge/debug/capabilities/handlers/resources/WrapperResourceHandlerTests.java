/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers.resources;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ItemUtil;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = ResourceHandlerTestSetup.GROUP_ID, idPrefix = "resource.handler.wrappers.")
public class WrapperResourceHandlerTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that FluidUtil#tryPickupFluid works correctly")
    private static void combined(ExtendedGameTestHelper helper) {
        var player = helper.makeMockPlayer();

        IResourceHandler<ItemResource> cap = player.getCapability(Capabilities.ItemHandler.ENTITY);
        helper.assertFalse(cap == null, "Player capability should be present");
        assert cap != null : "Player capability should be present"; // Mostly just makes the compiler understand the previous method

        helper.assertValueEqual(ResourceHandlerUtil.insertStacking(cap, ItemResource.of(Items.APPLE), 400, null), 400, "apples");
        var chestInserted = 0;
        var chest2Inserted = 0;
        try (var tx = TransactionManager.open(null)) {
            chestInserted = cap.insert(38, ItemResource.of(Items.DIAMOND_CHESTPLATE).with(DataComponents.DAMAGE, 20), 2, tx);
            chest2Inserted = cap.insert(39, ItemResource.of(Items.DIAMOND_CHESTPLATE), 2, tx);
            tx.commit();
        }
        helper.assertValueEqual(chestInserted, 1, "armor insert");
        helper.assertValueEqual(chest2Inserted, 0, "armor insert");
        var amount = ItemUtil.extractResourceStack(cap, item -> item.is(Items.DIAMOND_CHESTPLATE), 2, null);
        helper.assertValueEqual(amount.amount(), 1, "armor extract");
//        if (cap instanceof PlayerInventoryHandler wrapper) {
//            ResourceHandlerUtil.insertIndexForced(wrapper.armorHandler, Items.DIAMOND_BOOTS.defaultResource(), 1300, TransferAction.EXECUTE, TransactionContext.EMPTY);
//            ResourceHandlerUtil.insertIndexForced(wrapper.armorHandler, Items.NETHERITE_HELMET.defaultResource(), 1300, TransferAction.EXECUTE, TransactionContext.EMPTY);
//            ResourceHandlerUtil.extractAny(wrapper.mainHandHandler,  1000, TransferAction.EXECUTE, ItemResource.EMPTY_STACK, TransactionContext.EMPTY);
//        }

        helper.succeed();
    }
}
