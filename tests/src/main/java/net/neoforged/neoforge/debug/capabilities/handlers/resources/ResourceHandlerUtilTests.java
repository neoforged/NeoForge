/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers.resources;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.templates.InfiniteResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.VoidResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = ResourceHandlerTestSetup.GROUP_ID, idPrefix = "resource.handler.util.")
public class ResourceHandlerUtilTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests the general uses of ResourceHandlerUtil. Not necessarily Exhaustive")
    public static void generalUse(ExtendedGameTestHelper helper) {
        var src = ResourceHandlerTestSetup.setupLevelEnvironment(helper);
        var dst = ResourceHandlerTestSetup.setupLevelEnvironmentSecond(helper);

        if (!(helper.requireCapability(Capabilities.ItemHandler.BLOCK, src, Direction.UP) instanceof IResourceHandlerModifiable<ItemResource> srcHandler)) {
            throw helper.assertionException("The returned capability was not a Modifiable resource handler");
        }

        if (!(helper.requireCapability(Capabilities.ItemHandler.BLOCK, dst, Direction.UP) instanceof IResourceHandlerModifiable<ItemResource> dstHandler)) {
            throw helper.assertionException("The returned capability was not a Modifiable resource handler");
        }

        var workingStack = new ResourceStack<>(Blocks.COBBLESTONE.asItem().defaultResource(), 5000);

        srcHandler.set(0, ItemResource.EMPTY, 0);
        helper.assertTrue(ResourceHandlerUtil.isEmpty(srcHandler), "The inv was not empty");
        helper.assertFalse(ResourceHandlerUtil.isFull(srcHandler), "The inv should be empty");
        srcHandler.set(0, workingStack.resource(), workingStack.amount());
        helper.assertTrue(ResourceHandlerUtil.resourceAndCountMatches(srcHandler, 0, workingStack.resource(), workingStack.amount()), "Cobblestone in the inv did not match");

        helper.assertTrue(ResourceHandlerUtil.move(srcHandler, dstHandler, workingStack.amount(), TransactionContext.ROOT) == 0, "Nothing should have moved");

        srcHandler.set(10, workingStack.resource(), workingStack.amount());

        var amountMoved = ResourceHandlerUtil.move(srcHandler, VoidResourceHandler.ITEM, workingStack.amount(), TransactionContext.ROOT);
        helper.assertTrue(workingStack.amount() == amountMoved, "Did not move everything. Should have moved all 5000 cobble to it (to void), moved " + amountMoved);

        var infiniteStackHandler = new InfiniteResourceHandler<>(workingStack.resource());
        var amountTest = ResourceHandlerUtil.move(infiniteStackHandler, dstHandler, workingStack.amount(), TransactionContext.ROOT);
        helper.assertValueEqual(amountTest, 10 * workingStack.resource().getMaxStackSize(), "the destination to hold 10 stacks. That evaluates");

        dstHandler.set(10, workingStack.resource(), workingStack.amount());
        try (var transaction = Transaction.open(TransactionContext.ROOT)) {
            helper.assertValueEqual(ResourceHandlerUtil.move(dstHandler, VoidResourceHandler.ITEM, itemResource -> itemResource.is(Items.STICK), 100, transaction), 0, "Nothing should move");
            helper.assertValueEqual(ResourceHandlerUtil.move(dstHandler, VoidResourceHandler.ITEM, itemResource -> itemResource.is(Blocks.COBBLESTONE.asItem()), 100, transaction), 100, "amount to move");
        }
        helper.assertTrue(ResourceHandlerUtil.hasResource(dstHandler, workingStack.resource()), "The dst handler should have cobble");
        helper.assertFalse(ResourceHandlerUtil.hasResource(dstHandler, Items.STICK.defaultResource()), "The dst handler should have no sticks");

        for (var i = 0; i < dstHandler.size(); i++) {
            dstHandler.set(i, ItemResource.EMPTY, 0);
        }

        helper.assertValueEqual(ResourceHandlerUtil.insertIndexForced(dstHandler, Items.APPLE.defaultResource(), 123, null), 123, "apples inserted");
        for (var i = 0; i < dstHandler.size(); i++) {
            dstHandler.set(i, Items.APPLE.defaultResource(), 99);
        }

        var full = ResourceHandlerUtil.isFull(dstHandler);
        helper.assertTrue(full, "Dst handler should be full");
        helper.assertValueEqual(ResourceHandlerUtil.extractAny(dstHandler, 400, ItemResource.EMPTY_STACK, TransactionContext.ROOT), ItemResource.of(Items.APPLE).withAmount(400), "extracted");
        helper.assertFalse(ResourceHandlerUtil.isFull(dstHandler), "Dst handler should not be full");
        for (var i = 0; i < dstHandler.size(); i++) {
            dstHandler.set(i, ItemResource.EMPTY, 0);
        }
        for (var i = 0; i < dstHandler.size(); i++) {
            dstHandler.set(i, ItemResource.EMPTY, 0);
        }

        ResourceHandlerUtil.insertStacking(dstHandler, Items.APPLE.defaultResource(), 400, TransactionContext.ROOT);

        dstHandler.set(0, Items.HONEY_BOTTLE.defaultResource(), 3000);
        dstHandler.set(1, ItemResource.EMPTY, 0);
        helper.succeed();
    }
}
