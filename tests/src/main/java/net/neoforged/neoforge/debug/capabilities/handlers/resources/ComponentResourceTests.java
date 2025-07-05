/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers.resources;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.contexts.PlayerItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.contexts.StackItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.fluids.FluidResourceContainerContents;
import net.neoforged.neoforge.transfer.handlers.templates.fluids.ItemContextFluidHandler;
import net.neoforged.neoforge.transfer.handlers.templates.items.ItemResourceContainerContents;
import net.neoforged.neoforge.transfer.handlers.templates.resources.ResourceContainerContents;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

// Tests component storages on ItemStacks as well as the handler logic for those components. Also validates the Codec and Stream codec
@ForEachTest(groups = ResourceHandlerTestSetup.GROUP_ID, idPrefix = "resource.handler.component.")
public class ComponentResourceTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests queries on a consumable handler and then insertion/extractions with transactions")
    public static void testFluidHandlerItemStack(ExtendedGameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, Items.APPLE.getDefaultInstance());
        IItemContext context = PlayerItemContext.ofHand(player, InteractionHand.MAIN_HAND);
        int capacity = 2 * FluidType.BUCKET_VOLUME;

        ItemContextFluidHandler.Consumable handler = new ItemContextFluidHandler.Consumable(context, ResourceHandlerTestSetup.Content.SINGLE_FLUID_CONTENT.get(), capacity);

        if (handler.size() != 1)
            helper.fail("Expected a single tank");

        if (handler.getCapacity(0, FluidResource.EMPTY) != capacity)
            helper.fail("Expected tank capacity of " + capacity);

        if (handler.getAmount(0) != 0)
            helper.fail("Expected empty tank");
        try (Transaction tx = TransactionManager.open(null)) {
            int inserted = handler.insert(0, FluidResource.of(Fluids.WATER), FluidType.BUCKET_VOLUME, tx);
            if (inserted != FluidType.BUCKET_VOLUME)
                helper.fail("Expected to be able to fill a bucket of water");
            tx.commit();
        }

        if (!player.getMainHandItem().has(ResourceHandlerTestSetup.Content.SINGLE_FLUID_CONTENT))
            helper.fail("Expected fluid stack component");

        if (!ResourceHandlerUtil.resourceAndCountMatches(handler, 0, FluidResource.of(Fluids.WATER), FluidType.BUCKET_VOLUME))
            helper.fail("Expected a bucket of water");

        try (Transaction tx = TransactionManager.open(null)) {
            int extracted = handler.extract(0, FluidResource.of(Fluids.WATER), FluidType.BUCKET_VOLUME, tx);
            if (extracted != FluidType.BUCKET_VOLUME)
                helper.fail("Expected to drain a bucket of water");
            tx.commit();
        }

        if (!handler.getResource(0).isEmpty())
            helper.fail("Expected empty tank");

        ResourceStack<FluidResource> component = player.getMainHandItem().get(ResourceHandlerTestSetup.Content.SINGLE_FLUID_CONTENT);

        if (component != null && !component.isEmpty())
            helper.fail("Fluid stack component was found. The item was supposed to be consumed");

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that fluids can be inserted into and extracted from")
    public static void testFluidStorageItemStack(ExtendedGameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, Items.APPLE.getDefaultInstance().copyWithCount(4));
        IItemContext context = PlayerItemContext.ofHand(player, InteractionHand.MAIN_HAND);

        IResourceHandler<ItemResource> playerCap = player.getCapability(Capabilities.ItemHandler.ENTITY);
        helper.assertNotNull(playerCap, "IResourceHandler<ItemResource> must be present on player");
        assert playerCap != null;
        IResourceHandler<FluidResource> fluidHandler = context.getCapability(Capabilities.FluidHandler.ITEM);
        helper.assertNotNull(fluidHandler, "IResourceHandler<FluidResource> must be present on item");
        assert fluidHandler != null;

        try (Transaction tx = TransactionManager.open(null)) {
            int amount = fluidHandler.insert(FluidResource.of(Fluids.LAVA), 80000, tx);
            helper.assertValueEqual(amount, ResourceHandlerTestSetup.TANK_CAPACITY * ResourceHandlerTestSetup.TANK_COUNT * 4, "lava");
            helper.assertValueEqual(fluidHandler.extract(FluidResource.of(Fluids.LAVA), 3000, tx), 3000, "lava");

            helper.assertValueEqual(fluidHandler.insert(FluidResource.of(Fluids.LAVA), 100, tx), 0, "lava");
        }

        try (Transaction tx = TransactionManager.open(null)) {
            int amount = fluidHandler.insert(FluidResource.of(Fluids.LAVA), 80000, tx);
            helper.assertValueEqual(amount, ResourceHandlerTestSetup.TANK_CAPACITY * ResourceHandlerTestSetup.TANK_COUNT * 4, "lava");
            helper.assertValueEqual(fluidHandler.extract(FluidResource.of(Fluids.LAVA), 3000, tx), 3000, "lava");

            helper.assertValueEqual(fluidHandler.insert(FluidResource.of(Fluids.LAVA), 100, tx), 0, "lava");
            tx.commit();
        }

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Shulker insertion & extraction with transactions in item form")
    public static void shulkerTest(ExtendedGameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, Items.SHULKER_BOX.getDefaultInstance());
        IItemContext context = PlayerItemContext.ofHand(player, InteractionHand.MAIN_HAND);

        IResourceHandler<ItemResource> shulkerHandler = helper.requireNotNull(context.getCapability(Capabilities.ItemHandler.ITEM), "Shulker boxes should have an item cap");

        ItemResource diamondResource = ItemResource.of(Items.DIAMOND);
        ItemResource applResource = ItemResource.of(Items.APPLE);

        transactionOnShulker(helper, shulkerHandler, TransferAction.SIMULATE, diamondResource);
        transactionOnShulker(helper, shulkerHandler, TransferAction.EXECUTE, diamondResource);
        transactionOnShulker(helper, shulkerHandler, TransferAction.EXECUTE, applResource);

        helper.succeed();
    }

    private static void transactionOnShulker(ExtendedGameTestHelper helper, IResourceHandler<ItemResource> shulkerHandler, TransferAction action, ItemResource resource) {
        // ensure this is even since the test splits it into 2 to validate inserting to existing stacks works
        int insertedConst = 100;
        int extractedConst = 20;
        int remaining = insertedConst - extractedConst;
        try (Transaction tx = TransactionManager.open(null)) {
            int inserted = shulkerHandler.insert(resource, insertedConst, tx);
            helper.assertValueEqual(insertedConst / 2, inserted / 2, "Inserted should match");
            helper.assertValueEqual(insertedConst / 2, inserted / 2, "Inserted should match");
            helper.assertValueEqual(insertedConst, ResourceHandlerUtil.getAmount(shulkerHandler, resource), "Current Amount should match");
            int extracted = shulkerHandler.extract(resource, extractedConst, tx);
            helper.assertValueEqual(extracted, extractedConst, "Extracted should match");
            helper.assertValueEqual(remaining, ResourceHandlerUtil.getAmount(shulkerHandler, resource), "Current Amount should match");
            action.commit(tx);
        }
        helper.assertValueEqual(ResourceHandlerUtil.getAmount(shulkerHandler, resource), action.isExecuting() ? remaining : 0, "Current Amount should match");
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that ItemStorage Components work and don't create accidental duplications")
    public static void testItemStorageItemStack(ExtendedGameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, Items.APPLE.getDefaultInstance().copyWithCount(4));
        IItemContext context = PlayerItemContext.ofHand(player, InteractionHand.MAIN_HAND);
        IResourceHandler<ItemResource> storageCap = context.getCapability(Capabilities.ItemHandler.ITEM);
        if (storageCap == null) {
            helper.fail("Storage Capability was missing on item");
            return;
        }
        BlockPos pos = ResourceHandlerTestSetup.setupLevelEnvironment(helper);
        IResourceHandler<ItemResource> blockHandler = helper.requireCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);

        ItemResource diamondResource = ItemResource.of(Items.DIAMOND);
        try (Transaction tx = TransactionManager.open(null)) {
            //Because of the way the context filling works, it is attempting to fill or group similar actions together.
            //This means that only 2 "apples" will be filled with diamonds, despite sending 200 more diamond to it.
            ItemStack appleClone = player.getInventory().getItem(0).copy();
            //holds 100 stacks each.
            int amount = storageCap.insert(diamondResource, 13000, tx);
            helper.assertValueEqual(amount, 12800, "diamond");

            ItemResource applesWithContents = ItemResource.of(player.getInventory().getItem(1));
            blockHandler.insert(applesWithContents, 2, tx);
        }
        helper.assertTrue(ResourceHandlerUtil.isEmpty(storageCap), "handler");

        try (Transaction tx = TransactionManager.open(null)) {

            //holds 100 stacks each.
            int amount = storageCap.insert(diamondResource, 13000, tx);
            helper.assertValueEqual(12800, amount, "diamond");

            ItemResource applesWithContents = ItemResource.of(player.getInventory().getItem(1));
            blockHandler.insert(applesWithContents, 2, tx);
            tx.commit();
        }

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that ItemStorage Components work and don't create accidental duplications")
    public static void testComponent(ExtendedGameTestHelper helper) {
        ItemStack itemStack = new ItemStack(Items.APPLE);
        StackItemContext context = new StackItemContext(itemStack);
        helper.assertTrue(itemStack.isComponentsPatchEmpty(), "there should be no changes");
        ItemContextFluidHandler.SwapEmpty appleHandler = new ItemContextFluidHandler.SwapEmpty(
                context,
                ResourceHandlerTestSetup.Content.SINGLE_FLUID_CONTENT.get(),
                10, ItemResource.of(Items.APPLE));

        try (Transaction tx = TransactionManager.open(null)) {
            appleHandler.insert(FluidResource.of(Fluids.LAVA), 10, tx);
            try (Transaction subTx = TransactionManager.open(tx)) {
                appleHandler.extract(FluidResource.of(Fluids.LAVA), 5, subTx);
            }

            helper.assertValueEqual(appleHandler.getAmount(0), 10, "the sub transaction should have reverted");
            tx.commit();
        }

        helper.assertFalse(itemStack.isComponentsPatchEmpty(), "there should be changes");

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that Codec for resources work along side component storage")
    public static void testCodec(ExtendedGameTestHelper helper) {
        FriendlyByteBufUtil.writeCustomData(buf -> {
            ResourceContainerContents<ItemResource> itemContents = ItemResourceContainerContents.EMPTY;
            ResourceContainerContents<FluidResource> fluidContents = FluidResourceContainerContents.EMPTY;

            ItemResource resource = ItemResource.of(Items.APPLE)
                    .with(ResourceHandlerTestSetup.Content.ITEM_RESOURCE_CONTAINER_CONTENTS, itemContents)
                    .with(ResourceHandlerTestSetup.Content.FLUID_STORAGE_COMPONENT, fluidContents);
            //this should cross ItemResource, FluidResource, & ResourceStack stream codecs
            ItemResource.STREAM_CODEC.encode(buf, resource);
            ItemResource result = ItemResource.STREAM_CODEC.decode(buf);
            helper.assertValueEqual(result, resource, "decoded resource");
        }, helper.getLevel().registryAccess());
        helper.succeed();
    }
}
