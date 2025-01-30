/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers;

import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.templates.contexts.PlayerContext;
import net.neoforged.neoforge.transfer.handlers.templates.fluids.ItemContextFluidHandler;
import net.neoforged.neoforge.transfer.handlers.templates.storage.ResourceStorageComponent;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

// Tests component storages on ItemStacks as well as the handler logic for those components. Also validates the Codec and Stream codec
@ForEachTest(groups = ResourceHandlerTestSetup.GROUP_ID, idPrefix = "resource.handler.component.")
public class ComponentResourceTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that FluidHandlerItemStack works")
    public static void testFluidHandlerItemStack(ExtendedGameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, Items.APPLE.getDefaultInstance());
        IItemContext context = PlayerContext.ofHand(player, InteractionHand.MAIN_HAND);
        int capacity = 2 * FluidType.BUCKET_VOLUME;

        var fluidHandler = new ItemContextFluidHandler(context, ResourceHandlerTestSetup.Content.SIMPLE_FLUID_CONTENT.get(), capacity);

        if (fluidHandler.size() != 1)
            helper.fail("Expected a single tank");

        if (fluidHandler.getCapacity(0) != capacity)
            helper.fail("Expected tank capacity of " + capacity);

        if (fluidHandler.getAmount(0) != 0)
            helper.fail("Expected empty tank");

        if (fluidHandler.insert(0, Fluids.WATER.defaultResource(), FluidType.BUCKET_VOLUME, TransferAction.EXECUTE) != FluidType.BUCKET_VOLUME)
            helper.fail("Expected to be able to fill a bucket of water");

        if (!player.getMainHandItem().has(ResourceHandlerTestSetup.Content.SIMPLE_FLUID_CONTENT))
            helper.fail("Expected fluid stack component");

        if (!ResourceHandlerUtil.resourceAndCountMatches(fluidHandler, 0, Fluids.WATER.defaultResource(), FluidType.BUCKET_VOLUME))
            helper.fail("Expected a bucket of water");

        var drained = fluidHandler.extract(0, Fluids.WATER.defaultResource(), FluidType.BUCKET_VOLUME, TransferAction.EXECUTE);
        if (drained != FluidType.BUCKET_VOLUME)
            helper.fail("Expected to drain a bucket of water");

        if (!ResourceHandlerUtil.isIndexEmpty(fluidHandler, 0))
            helper.fail("Expected empty tank");

        if (player.getMainHandItem().has(ResourceHandlerTestSetup.Content.SIMPLE_FLUID_CONTENT))
            helper.fail("Expected no fluid stack component");

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that FluidHandlerItemStack works")
    public static void testFluidStorageItemStack(ExtendedGameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, Items.APPLE.getDefaultInstance().copyWithCount(4));
        IItemContext context = PlayerContext.ofHand(player, InteractionHand.MAIN_HAND);
        int capacity = 2 * FluidType.BUCKET_VOLUME;
        var playerCap = player.getCapability(Capabilities.ItemHandler.ENTITY);

        var fluidHandler = playerCap.getResource(0).toStack().getCapability(Capabilities.FluidHandler.ITEM, context);
        var amount = fluidHandler.insert(Fluids.LAVA.defaultResource(), 80000, TransferAction.EXECUTE);
        var apples = player.getMainHandItem().copy();
        helper.assertValueEqual(amount, ResourceHandlerTestSetup.TANK_CAPACITY * ResourceHandlerTestSetup.TANK_COUNT * 4, "lava");
        helper.assertValueEqual(fluidHandler.extract(Fluids.LAVA.defaultResource(), 3000, TransferAction.EXECUTE), 3000, "lava");

        helper.assertValueEqual(fluidHandler.insert(Fluids.LAVA.defaultResource(), 100, TransferAction.EXECUTE), 0, "lava");

        //todo add apples check to make sure the writes didn't propagate back to the apple clone. This was done manually, in debugger, just not test
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that ItemStorage Components work and don't create accidental duplications")
    public static void testItemStorageItemStack(ExtendedGameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, Items.APPLE.getDefaultInstance().copyWithCount(4));
        IItemContext context = PlayerContext.ofHand(player, InteractionHand.MAIN_HAND);
        var storageCap = context.getCapability(Capabilities.ItemHandler.ITEM);
        if (storageCap == null) {
            helper.fail("Storage Capability was missing on item");
            return;
        }

        //Because of the way the context filling works, it is attempting to fill or group similar actions together.
        //This means that only 2 "apples" will be filled with diamonds, despite sending 200 more diamond to it.
        var appleClone = player.getInventory().getItem(0).copy();
        var amount = storageCap.insert(Items.DIAMOND.defaultResource(), 13000, TransferAction.EXECUTE);
        helper.assertValueEqual(amount, 12800, "diamond");

        //todo add apples check to make sure the writes didn't propagate back to the apple clone. This was done manually, in debugger, just not test
        var pos = ResourceHandlerTestSetup.setupLevelEnvironment(helper);
        if (!(helper.requireCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP) instanceof IResourceHandlerModifiable<ItemResource> blockHandler)) {
            throw new GameTestAssertException("The returned capability was not a Modifiable resource handler");
        }
        var applesWithContents = ItemResource.of(player.getInventory().getItem(1));
        blockHandler.insert(applesWithContents, 2, TransferAction.EXECUTE);

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that Codec for resources work along side component storage")
    public static void testCodec(ExtendedGameTestHelper helper) {
        FriendlyByteBufUtil.writeCustomData(buf -> {
            var itemContents = new ResourceStorageComponent<>(3, ItemResource.NONE).modify(0, Items.APPLE.defaultResource().with(DataComponents.DAMAGE, 20), 3);
            var fluidContents = new ResourceStorageComponent<>(3, FluidResource.NONE).modify(0, Fluids.LAVA.defaultResource(), 200);

            var resource = Items.APPLE.defaultResource().with(ResourceHandlerTestSetup.Content.ITEM_STORAGE_COMPONENT, itemContents).with(ResourceHandlerTestSetup.Content.FLUID_STORAGE_COMPONENT, fluidContents);
            //this should cross ItemResource, FluidResource, & ResourceStack stream codecs
            ItemResource.STREAM_CODEC.encode(buf, resource);
            var result = ItemResource.STREAM_CODEC.decode(buf);
            helper.assertValueEqual(result, resource, "decoded resource");
        }, helper.getLevel().registryAccess());
        helper.succeed();
    }
}
