/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers.resources;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ItemUtil;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.templates.InfiniteResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.VoidResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStackListHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
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

        if (!(helper.requireCapability(Capabilities.ItemHandler.BLOCK, src, Direction.UP) instanceof ResourceStackListHandler<ItemResource> srcHandler)) {
            throw helper.assertionException("The returned capability was not a Modifiable resource handler");
        }
        var inputHandler = helper.requireCapability(Capabilities.ItemHandler.BLOCK, src, Direction.NORTH); //[0,10)
        var outputHandler = helper.requireCapability(Capabilities.ItemHandler.BLOCK, dst, Direction.SOUTH);//[11,20)

        var workingStack = ItemResource.of(Blocks.COBBLESTONE).withAmount(5000);
        helper.assertTrue(ResourceHandlerUtil.isEmpty(srcHandler), "The inv was not empty");
        helper.assertFalse(ResourceHandlerUtil.isFull(srcHandler), "The inv should be empty");
        try (var transaction = TransactionManager.open(null)) {
            helper.assertValueEqual(srcHandler.insert(0, workingStack.resource(), workingStack.amount(), transaction), workingStack.amount(), "Amount set should be the same at " + workingStack.amount());
            transaction.commit();
        }

        helper.assertTrue(ResourceHandlerUtil.resourceAndCountMatches(srcHandler, 0, workingStack.resource(), workingStack.amount()), "Cobblestone in the inv did not match");

//        helper.assertTrue(ResourceHandlerUtil.move(inputHandler, inputHandler, ResourceFilters.any(), workingStack.amount(), null) == 0, "Nothing should have moved");

        try (var transaction = TransactionManager.open(null)) {
            helper.assertValueEqual(srcHandler.insert(10, workingStack.resource(), workingStack.amount(), transaction), workingStack.amount(), "Amount set should be the same at " + workingStack.amount());
        }

        var amountMoved = ResourceHandlerUtil.move(srcHandler, VoidResourceHandler.ITEM, resource -> true, workingStack.amount(), null);
        helper.assertTrue(workingStack.amount() == amountMoved, "Did not move everything. Should have moved all 5000 cobble to it (to void), moved " + amountMoved);

        var infiniteStackHandler = new InfiniteResourceHandler<>(workingStack);
        var amountTest = ResourceHandlerUtil.move(infiniteStackHandler, srcHandler, resource -> true, workingStack.amount() * 10, null);
        helper.assertValueEqual(amountTest, 10 * workingStack.amount(), "the destination to hold 10 stacks of 5000. That evaluates");

        //Reset
        try (var transaction = TransactionManager.open(null)) {
            srcHandler.extract(10, workingStack.resource(), Integer.MAX_VALUE, transaction);
            srcHandler.insert(10, workingStack.resource(), workingStack.amount(), transaction);
            transaction.commit();
        }

        //        outputHandler.set(10, workingStack.resource(), workingStack.amount());
        try (var transaction = TransactionManager.open(null)) {
            helper.assertValueEqual(ResourceHandlerUtil.move(srcHandler, VoidResourceHandler.ITEM, itemResource -> itemResource.is(Items.STICK), 100, transaction), 0, "Nothing should move");
            helper.assertValueEqual(ResourceHandlerUtil.move(srcHandler, VoidResourceHandler.ITEM, itemResource -> itemResource.is(Blocks.COBBLESTONE), 100, transaction), 100, "amount to move");
        }
        helper.assertTrue(ResourceHandlerUtil.hasExtractableResource(srcHandler, workingStack.resource()), "The dst handler should have cobble");
        helper.assertFalse(ResourceHandlerUtil.hasExtractableResource(srcHandler, ItemResource.of(Items.STICK)), "The dst handler should have no sticks");

        //reset (empty)
        ResourceHandlerUtil.move(outputHandler, VoidResourceHandler.ITEM, resource -> true, Integer.MAX_VALUE, null);

        helper.assertValueEqual(ResourceHandlerUtil.insertIndexForced(outputHandler, ItemResource.of(Items.APPLE), 123, null), 123, "apples inserted");
        //reset (empty+fill)
        ResourceHandlerUtil.move(outputHandler, VoidResourceHandler.ITEM, resource -> true, Integer.MAX_VALUE, null);
        ResourceHandlerUtil.move(new InfiniteResourceHandler<>(ItemResource.of(Items.APPLE)), outputHandler, resource -> true, Integer.MAX_VALUE, null);
        //        for (var i = 0; i < outputHandler.size(); i++) {
        //            outputHandler.set(i, Items.APPLE.defaultResource(), 99);
        //        }

        var full = ResourceHandlerUtil.isFull(outputHandler);
        helper.assertTrue(full, "Dst handler should be full");

        helper.assertValueEqual(ItemUtil.extractResourceStackFiltered(outputHandler, resource -> true, 400, null), ItemResource.of(Items.APPLE).withAmount(400), "extracted");
        helper.assertFalse(ResourceHandlerUtil.isFull(outputHandler), "Dst handler should not be full");

        ResourceHandlerUtil.move(outputHandler, VoidResourceHandler.ITEM, resource -> true, Integer.MAX_VALUE, null);
        //        for (var i = 0; i < outputHandler.size(); i++) {
        //            outputHandler.set(i, ItemResource.EMPTY, 0);
        //        }
        //        for (var i = 0; i < outputHandler.size(); i++) {
        //            outputHandler.set(i, ItemResource.EMPTY, 0);
        //        }

        ResourceHandlerUtil.insertStacking(outputHandler, ItemResource.of(Items.APPLE), 400, null);

        helper.succeed();
    }
}
