/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers.resources;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.FluidUtil;
import net.neoforged.neoforge.transfer.ResourceFilters;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.templates.InfiniteResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.VoidResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.contexts.PlayerItemContext;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;
import org.jetbrains.annotations.Nullable;

/**
 * Various tests for {@link FluidUtil}, that run when the mod is loaded.
 * If one of the tests fails, an expection will be thrown, and mod loading will fail with an error.
 * If all tests pass, the mod will load successfully.
 */
@ForEachTest(groups = ResourceHandlerTestSetup.GROUP_ID, idPrefix = "resource.handler.fluid_util.")
public class FluidUtilTest {
    private static void setFluid(ExtendedGameTestHelper helper, BlockPos blockPos, ResourceStack<FluidResource> resourceStack) {
        var handler = helper.requireCapability(Capabilities.FluidHandler.BLOCK, blockPos, null);
        ResourceHandlerUtil.move(handler, VoidResourceHandler.FLUID, ResourceFilters.any(), Integer.MAX_VALUE, TransactionContext.ROOT);
        ResourceHandlerUtil.move(new InfiniteResourceHandler<>(resourceStack.resource()), handler, ResourceFilters.any(), resourceStack.amount(), TransactionContext.ROOT);

//        if (handler instanceof ResourceContainerToHandlerAdapter<FluidResource> modifiable) {
//            modifiable.set(0, resourceStack.resource(), resourceStack.amount());
//        }
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that FluidUtil#tryPickupFluid works correctly")
    private static void pickupFluid(ExtendedGameTestHelper helper) {
        var posOfWater = helper.relativePos(new BlockPos(1, 0, 0));
        var player = helper.makeMockPlayer();
        var endlessWaterSource = new InfiniteResourceHandler<>(Fluids.WATER.getDefaultResource());

        // test pickup of water and ensure exchange shrinks main stack and puts overflow in inventory
        resetInventory(player, new ItemStack(Items.BUCKET, 2));
        resetWater(helper, posOfWater);

        helper.assertBlockPresent(Blocks.WATER, posOfWater);
        helper.assertTrue(FluidUtil.tryPickupFluidAsPlayer(player, InteractionHand.MAIN_HAND, helper.getLevel(), helper.absolutePos(posOfWater)), "Fluid should be picked up");
        helper.assertBlockNotPresent(Blocks.WATER, posOfWater);
        checkInventory(helper, player, Items.BUCKET, 1, Items.WATER_BUCKET, 1);

        helper.assertFalse(FluidUtil.tryPickupFluidAsPlayer(player, InteractionHand.MAIN_HAND, helper.getLevel(), helper.absolutePos(posOfWater)), "No fluid to pick up");
        resetWater(helper, posOfWater);
        helper.assertTrue(FluidUtil.tryPickupFluidAsPlayer(player, InteractionHand.MAIN_HAND, helper.getLevel(), helper.absolutePos(posOfWater)), "Fluid should be picked up");
        checkInventory(helper, player, Items.WATER_BUCKET, 1, Items.WATER_BUCKET, 2);

        resetInventory(player, new ItemStack(Items.BUCKET, 5));
        var capability = PlayerItemContext.ofHand(player, InteractionHand.MAIN_HAND).getCapability(Capabilities.FluidHandler.ITEM);
        assert capability != null;
        FluidUtil.moveFluidWithSound(player.getCommandSenderWorld(), player.position(), SoundActions.BUCKET_FILL, endlessWaterSource, capability, Integer.MAX_VALUE);
        checkInventory(helper, player, Items.WATER_BUCKET, 1, Items.WATER_BUCKET, 5);

        helper.setBlock(posOfWater, Blocks.AIR);
        FluidUtil.tryPlaceFluidAsPlayer(player, InteractionHand.MAIN_HAND, helper.getLevel(), helper.absolutePos(posOfWater));
        helper.assertTrue(helper.getBlockState(posOfWater).getFluidState().is(Fluids.WATER), "Should be water here");

        resetInventory(player, new ItemStack(Items.WATER_BUCKET, 1));
        helper.setBlock(posOfWater, Blocks.STONE_SLAB);
        FluidUtil.tryPlaceFluidAsPlayer(player, InteractionHand.MAIN_HAND, helper.getLevel(), helper.absolutePos(posOfWater));
        helper.assertTrue(helper.getBlockState(posOfWater).getFluidState().is(Fluids.WATER), "Should be water here with slab");
        FluidUtil.tryPickupFluidAsPlayer(player, InteractionHand.MAIN_HAND, helper.getLevel(), helper.absolutePos(posOfWater));
        helper.assertFalse(helper.getBlockState(posOfWater).getFluidState().is(Fluids.WATER), "Should not be water here with slab");
        helper.assertBlockPresent(Blocks.STONE_SLAB, posOfWater);
        helper.assertTrue(FluidStack.isSameFluid(FluidUtil.getFirstFluidStackContained(new ItemStack(Items.WATER_BUCKET, 1)), new FluidStack(Fluids.WATER, 1000)), "Water is expected to be in the bucket.");

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Test fluid handler interactions with items in hand")
    private static void handlerInteractionWithItem(ExtendedGameTestHelper helper) {
        var pos = ResourceHandlerTestSetup.setupLevelEnvironment(helper);
        var player = helper.makeMockPlayer();
        var waterOf1BucketAmount = ResourceStack.of(Fluids.WATER.getDefaultResource(), FluidType.BUCKET_VOLUME);

        var handler = helper.requireCapability(Capabilities.FluidHandler.BLOCK, pos, null);

        //It can store 4 buckets, but we are setting to 1
        setFluid(helper, pos, waterOf1BucketAmount);
        helper.assertValueEqual(handler.getAmount(0), waterOf1BucketAmount.amount(), "fluid amount in index `0`");

        setFluid(helper, pos, FluidResource.EMPTY_STACK);
        helper.assertValueEqual(handler.getAmount(0), 0, "fluid amount in index `0`");
        helper.assertValueEqual(handler.getResource(0), FluidResource.EMPTY, "fluid in index `0`");

        setFluid(helper, pos, waterOf1BucketAmount);
        resetInventory(player, new ItemStack(Items.BUCKET, 1));

        int startingAmount;
        try (var transaction = TransactionManager.open(TransactionContext.ROOT)) {
            startingAmount = ResourceHandlerUtil.extract(handler, Fluids.WATER.getDefaultResource(), Integer.MAX_VALUE, transaction);
        }

        helper.assertTrue(FluidUtil.interactWithFluidHandler(player, InteractionHand.MAIN_HAND, handler), "Should pick up fluid");
        checkInventory(helper, player, Items.WATER_BUCKET, 1, Items.WATER_BUCKET, 1);
        try (var transaction = TransactionManager.open(TransactionContext.ROOT)) {
            helper.assertValueEqual(startingAmount - FluidType.BUCKET_VOLUME, ResourceHandlerUtil.extract(handler, Fluids.WATER.getDefaultResource(), Integer.MAX_VALUE, transaction), "fluid amount");
        }

        helper.assertTrue(FluidUtil.interactWithFluidHandler(player, InteractionHand.MAIN_HAND, handler), "Should dispense of fluid");
        checkInventory(helper, player, Items.BUCKET, 1, Items.BUCKET, 1);
        try (var transaction = TransactionManager.open(TransactionContext.ROOT)) {
            helper.assertValueEqual(startingAmount, ResourceHandlerUtil.extract(handler, Fluids.WATER.getDefaultResource(), Integer.MAX_VALUE, transaction), "fluid amount");
        }
        helper.succeed();
    }

    public static void resetWater(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Blocks.WATER);
    }

    public static void resetInventory(Player player, ItemStack stack) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            player.getInventory().setItem(i, ItemStack.EMPTY);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
    }

    //checks if main hand has `mainCount` mainItems(s) and inventory has 1 inventoryItem. If it doesn't, throw
    public static void checkInventory(ExtendedGameTestHelper helper, Player player, Item mainItem, int mainCount, @Nullable Item inventoryItem, int inventoryCount) {
        ItemStack mainHand = player.getMainHandItem();
        if (inventoryItem != null) {
            boolean hadItem = false;
            int count = 0;
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (stack.is(inventoryItem)) {
                    hadItem = true;
                    count += stack.getCount();
                }
            }
            if (!hadItem)
                helper.fail("We expected inventory to have" + inventoryItem);
            if (count != inventoryCount)
                helper.fail("We expected inventory item count to be " + inventoryCount + " it was " + count);
        }

        if (!mainHand.is(mainItem))
            helper.fail("We expected main item to be " + mainItem + " it was " + mainHand.getItem().getDescriptionId());
        if (mainCount != mainHand.getCount())
            helper.fail("We expected main item count to be " + mainCount + " it was " + mainHand.getCount());
    }
}
