/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers;

import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.templates.InfiniteResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.VoidResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

@ForEachTest(groups = ResourceHandlerTestSetup.GROUP_ID, idPrefix = "resource.handler.util.")
public class ResourceHandlerUtilTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests the general uses of ResourceHandlerUtil. Not necessarily Exhaustive")
    public static void generalUse(ExtendedGameTestHelper helper) {
        var src = ResourceHandlerTestSetup.setupLevelEnvironment(helper);
        var dst = ResourceHandlerTestSetup.setupLevelEnvironmentSecond(helper);

        if (!(helper.requireCapability(Capabilities.ItemHandler.BLOCK, src, Direction.UP) instanceof IResourceHandlerModifiable<ItemResource> srcHandler)) {
            throw new GameTestAssertException("The returned capability was not a Modifiable resource handler");
        }

        if (!(helper.requireCapability(Capabilities.ItemHandler.BLOCK, dst, Direction.UP) instanceof IResourceHandlerModifiable<ItemResource> dstHandler)) {
            throw new GameTestAssertException("The returned capability was not a Modifiable resource handler");
        }

        var workingStack = new ResourceStack<>(Blocks.COBBLESTONE.asItem().defaultResource(), 5000);

        srcHandler.set(0, ItemResource.NONE, 0);
        helper.assertTrue(ResourceHandlerUtil.isEmpty(srcHandler), "The inv was not empty");
        helper.assertFalse(ResourceHandlerUtil.isFull(srcHandler), "The inv should be empty");
        srcHandler.set(0, workingStack.resource(), workingStack.amount());
        helper.assertTrue(ResourceHandlerUtil.resourceAndCountMatches(srcHandler, 0, workingStack.resource(), workingStack.amount()), "Cobblestone in the inv did not match");
        helper.assertTrue(ResourceHandlerUtil.moveAny(srcHandler, dstHandler, workingStack.amount(), TransferAction.EXECUTE, ItemResource.NONE).isEmpty(), "Nothing should have moved");

        srcHandler.set(10, workingStack.resource(), workingStack.amount());

        var amountMoved = ResourceHandlerUtil.moveAny(srcHandler, VoidResourceHandler.ITEM, workingStack.amount(), TransferAction.EXECUTE, ItemResource.NONE);
        helper.assertTrue(amountMoved.equals(workingStack), "Did not move everything. Should have moved all 5000 cobble to it (to void)");

        var infiniteStackHandler = new InfiniteResourceHandler<>(workingStack.resource());
        var amountTest = ResourceHandlerUtil.moveAny(infiniteStackHandler, dstHandler, workingStack.amount(), TransferAction.EXECUTE, ItemResource.NONE);
        helper.assertValueEqual(amountTest, workingStack.withAmount(10 * workingStack.resource().getMaxStackSize()), "The destination should hold 10 stacks");

        dstHandler.set(10, workingStack.resource(), workingStack.amount());
        helper.assertValueEqual(ResourceHandlerUtil.moveFiltered(dstHandler, VoidResourceHandler.ITEM, itemResource -> itemResource.is(Items.STICK), 100, TransferAction.SIMULATE, ItemResource.NONE), ItemResource.EMPTY_STACK, "Nothing should move");
        helper.assertValueEqual(ResourceHandlerUtil.moveFiltered(dstHandler, VoidResourceHandler.ITEM, itemResource -> itemResource.is(Blocks.COBBLESTONE.asItem()), 100, TransferAction.SIMULATE, ItemResource.NONE), new ResourceStack<>(Blocks.COBBLESTONE.asItem().defaultResource(), 100), "amount to move");

        helper.assertTrue(ResourceHandlerUtil.hasResource(dstHandler, workingStack.resource()), "The dst handler should have cobble");
        helper.assertFalse(ResourceHandlerUtil.hasResource(dstHandler, Items.STICK.defaultResource()), "The dst handler should have no sticks");

        for (var i = 0; i < dstHandler.size(); i++) {
            dstHandler.set(i, ItemResource.NONE, 0);
        }

        helper.assertValueEqual(ResourceHandlerUtil.insertIndexForced(dstHandler, Items.APPLE.defaultResource(), 123, TransferAction.EXECUTE), 123, "apples inserted");
        for (var i = 0; i < dstHandler.size(); i++) {
            dstHandler.set(i, Items.APPLE.defaultResource(), 100);
        }

        helper.assertTrue(ResourceHandlerUtil.isFull(dstHandler), "Dst handler should be full");
        helper.assertValueEqual(ResourceHandlerUtil.extractAny(dstHandler, 400, TransferAction.EXECUTE, ItemResource.NONE), ItemResource.of(Items.APPLE).withAmount(400), "extracted");
        helper.assertFalse(ResourceHandlerUtil.isFull(dstHandler), "Dst handler should be full");
        for (var i = 0; i < dstHandler.size(); i++) {
            dstHandler.set(i, ItemResource.NONE, 0);
        }
        for (var i = 0; i < dstHandler.size(); i++) {
            dstHandler.set(i, ItemResource.NONE, 0);
        }
        ResourceHandlerUtil.insertStacking(dstHandler, Items.APPLE.defaultResource(), 400, TransferAction.EXECUTE);
        dstHandler.set(0, Items.HONEY_BOTTLE.defaultResource(), 3000);
        dstHandler.set(1, ItemResource.NONE, 0);
        helper.succeed();
    }
}
