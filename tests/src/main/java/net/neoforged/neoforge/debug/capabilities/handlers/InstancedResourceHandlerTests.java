/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.templates.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.InfiniteResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.VoidResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = ResourceHandlerTestSetup.GROUP_ID, idPrefix = "resource.handler.instanced.")
public class InstancedResourceHandlerTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Handles endless handler tests")
    public static void validateCapability(ExtendedGameTestHelper helper) {
        var pos = ResourceHandlerTestSetup.setupLevelEnvironment(helper);
        var input = helper.requireCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.NORTH);
        var output = helper.requireCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.SOUTH);
        var both = helper.requireCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);

        var fluid = helper.requireCapability(Capabilities.FluidHandler.BLOCK, pos, null);
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Handles empty handler tests")
    public static void emptyHandlers(ExtendedGameTestHelper helper) {
        //EMPTY no operation handlers
        testEmptyHandler(helper, EmptyResourceHandler.instance(), ItemResource.EMPTY);
        testEmptyHandler(helper, EmptyResourceHandler.instance(), FluidResource.EMPTY);
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Handles void handler tests")
    public static void voidHandlers(ExtendedGameTestHelper helper) {
        //VoidResourceHandlers destroys resources but doesn't allow extraction
        testVoidResource(helper, VoidResourceHandler.ITEM, ItemResource.EMPTY);
        testVoidResource(helper, VoidResourceHandler.FLUID, FluidResource.EMPTY);

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Handles endless handler tests")
    public static void endlessHandlers(ExtendedGameTestHelper helper) {
        //InfiniteResourceHandlers creates infinite of a specified resource, but doesn't allow insertion
        testEndlessResource(helper, Fluids.WATER.defaultResource());
        testEndlessResource(helper, Blocks.COBBLESTONE.asItem().defaultResource());
        helper.succeed();
    }

    private static <T extends IResource> void testVoidResource(ExtendedGameTestHelper helper, VoidResourceHandler<T> handler, T emptyResource) {
        helper.assertValueEqual(handler.size(), 1, "Size should be");
        helper.assertFalse(handler.allowsExtraction(), "Extraction should be not allowed");
        helper.assertFalse(handler.allowsExtraction(0), "Extraction should be not allowed");
        helper.assertFalse(handler.allowsExtraction(1337), "Extraction should be not allowed");

        helper.assertTrue(handler.allowsInsertion(), "Insertion should be allowed");
        helper.assertTrue(handler.allowsInsertion(0), "Insertion should be allowed");
        helper.assertTrue(handler.allowsInsertion(1337), "Insertion should be allowed");

        helper.assertTrue(ResourceHandlerUtil.isValid(handler, emptyResource), "Every resource should match");

        helper.assertValueEqual(handler.getCapacity(0, emptyResource), ResourceHandlerUtil.MAX, "Capacity should match");
        helper.assertValueEqual(handler.getCapacity(1, emptyResource), ResourceHandlerUtil.MAX, "Capacity should match");

        helper.assertValueEqual(handler.getResource(0), emptyResource, "Resource should match");
        helper.assertValueEqual(handler.getResource(1), emptyResource, "Resource should match");
        try (var transaction = Transaction.open(TransactionContext.ROOT)) {
            helper.assertValueEqual(handler.insert(0, emptyResource, ResourceHandlerUtil.MAX, transaction), ResourceHandlerUtil.MAX, "Insertion should match");
            helper.assertValueEqual(handler.insert(emptyResource, ResourceHandlerUtil.MAX, transaction), ResourceHandlerUtil.MAX, "Insertion should match");

            helper.assertValueEqual(handler.extract(0, emptyResource, 1, transaction), 0, "Extraction should match");
            helper.assertValueEqual(handler.extract(emptyResource, 1, transaction), 0, "Extraction should match");
        }
    }

    private static <T extends IResource> void testEndlessResource(ExtendedGameTestHelper helper, T resource) {
        InfiniteResourceHandler<T> handler = new InfiniteResourceHandler<>(resource);
        helper.assertValueEqual(handler.size(), 1, "Size should be");
        helper.assertTrue(handler.allowsExtraction(), "Extraction should be allowed");
        helper.assertTrue(handler.allowsExtraction(0), "Extraction should be allowed");
        helper.assertTrue(handler.allowsExtraction(1337), "Extraction should be allowed");

        helper.assertFalse(handler.allowsInsertion(), "Insertion should not be allowed");
        helper.assertFalse(handler.allowsInsertion(0), "Insertion should not be allowed");
        helper.assertFalse(handler.allowsInsertion(1337), "Insertion should not be allowed");

        helper.assertTrue(handler.isValid(0, resource), "Resource should match");

        helper.assertValueEqual(handler.getCapacity(0, resource), ResourceHandlerUtil.MAX, "Capacity should match");
        helper.assertValueEqual(handler.getResource(0), resource, "Resource should match");
        try (var transaction = Transaction.open(TransactionContext.ROOT)) {
            helper.assertValueEqual(handler.insert(0, resource, 1, transaction), 0, "Insertion should match");
            helper.assertValueEqual(handler.insert(resource, 1, transaction), 0, "Insertion should match");

            helper.assertValueEqual(handler.extract(0, resource, 1, transaction), 1, "Extraction should match");
            helper.assertValueEqual(handler.extract(resource, 1, transaction), 1, "Extraction should match");
        }
    }

    private static <T extends IResource> void testEmptyHandler(ExtendedGameTestHelper helper, EmptyResourceHandler<T> handler, T emptyResource) {
        helper.assertValueEqual(handler.size(), 0, "Empty should no-op");
        helper.assertFalse(handler.allowsExtraction(), "Empty should no-op");
        helper.assertFalse(handler.allowsExtraction(0), "Empty should no-op");
        helper.assertFalse(handler.allowsInsertion(), "Empty should no-op");
        helper.assertFalse(handler.allowsInsertion(0), "Empty should no-op");
        helper.assertFalse(handler.isValid(0, emptyResource), "Empty should no-op, but should return empty");
        helper.assertValueEqual(handler.getCapacity(0, emptyResource), 0, "Empty should no-op");
        helper.assertValueEqual(handler.getAmount(0), 0, "Empty should no-op");

        try (var transaction = Transaction.open(TransactionContext.ROOT)) {
            helper.assertValueEqual(handler.insert(0, emptyResource, 1, transaction), 0, "Empty should no-op");
            helper.assertValueEqual(handler.insert(emptyResource, 1, transaction), 0, "Empty should no-op");
            helper.assertValueEqual(handler.extract(0, emptyResource, 1, transaction), 0, "Empty should no-op");
            helper.assertValueEqual(handler.extract(emptyResource, 1, transaction), 0, "Empty should no-op");
        }
    }
}
