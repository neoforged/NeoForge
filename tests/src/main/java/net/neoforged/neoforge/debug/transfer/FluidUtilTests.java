/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.transfer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "transfer.fluidutil")
public class FluidUtilTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Test picking up water with FluidUtil.tryPickupFluid")
    public static void testWaterBlockPickup(ExtendedGameTestHelper helper) {
        testWaterPickup(
                helper,
                Blocks.WATER.defaultBlockState(),
                Blocks.AIR.defaultBlockState());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Test picking up from waterlogged slab with FluidUtil.tryPickupFluid")
    public static void testWaterloggedSlabPickup(ExtendedGameTestHelper helper) {
        testWaterPickup(
                helper,
                Blocks.STONE_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true),
                Blocks.STONE_SLAB.defaultBlockState());
    }

    private static void testWaterPickup(ExtendedGameTestHelper helper, BlockState initialState, BlockState finalState) {
        var waterPos = new BlockPos(1, 0, 0);
        helper.setBlock(waterPos, initialState);

        // Survival because ItemAccess.forPlayerInteraction behaves differently in creative mode
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET, 2));
        var handHandler = ItemAccess.forPlayerInteraction(player, InteractionHand.MAIN_HAND)
                .oneByOne()
                .getCapability(Capabilities.Fluid.ITEM);

        var pickupResult = FluidUtil.tryPickupFluid(
                handHandler,
                player,
                helper.getLevel(),
                helper.absolutePos(waterPos),
                null);
        helper.assertValueEqual(Fluids.WATER, pickupResult.getFluid(), "picked up fluid");
        helper.assertValueEqual(FluidType.BUCKET_VOLUME, pickupResult.getAmount(), "picked up amount");
        helper.assertBlockState(waterPos, finalState);

        var mainHandItem = player.getMainHandItem();
        helper.assertValueEqual(Items.BUCKET, mainHandItem.getItem(), "main hand item");
        helper.assertValueEqual(1, mainHandItem.getCount(), "main hand item count");

        // Filled bucket should go into slot 1 (main hand is 0 here)
        var filledBucket = player.getInventory().getItem(1);
        helper.assertValueEqual(Items.WATER_BUCKET, filledBucket.getItem(), "slot 1 item");
        helper.assertValueEqual(1, filledBucket.getCount(), "slot 1 item count");

        // A second pickup attempt should fail
        var secondPickupResult = FluidUtil.tryPickupFluid(
                handHandler,
                player,
                helper.getLevel(),
                helper.absolutePos(waterPos),
                null);
        helper.assertTrue(secondPickupResult.isEmpty(), "second pickup result is empty");
        // Block state should not have changed
        helper.assertBlockState(waterPos, finalState);

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Test placing water with FluidUtil.tryPlaceFluid")
    public static void testWaterBlockPlacement(ExtendedGameTestHelper helper) {
        testWaterPlacement(
                helper,
                Blocks.AIR.defaultBlockState(),
                Blocks.WATER.defaultBlockState());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Test placing water into a slab with FluidUtil.tryPlaceFluid")
    public static void testWaterloggedSlabPlacement(ExtendedGameTestHelper helper) {
        testWaterPlacement(
                helper,
                Blocks.STONE_SLAB.defaultBlockState(),
                Blocks.STONE_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true));
    }

    private static void testWaterPlacement(ExtendedGameTestHelper helper, BlockState initialState, BlockState finalState) {
        var waterPos = new BlockPos(1, 0, 0);
        helper.setBlock(waterPos, initialState);

        // Survival because ItemAccess.forPlayerInteraction behaves differently in creative mode
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        var handHandler = ItemAccess.forPlayerInteraction(player, InteractionHand.MAIN_HAND)
                .oneByOne()
                .getCapability(Capabilities.Fluid.ITEM);

        var placementResult = FluidUtil.tryPlaceFluid(
                handHandler,
                player,
                helper.getLevel(),
                InteractionHand.MAIN_HAND,
                helper.absolutePos(waterPos));
        helper.assertValueEqual(Fluids.WATER, placementResult.getFluid(), "placed fluid");
        helper.assertValueEqual(FluidType.BUCKET_VOLUME, placementResult.getAmount(), "placed amount");
        helper.assertBlockState(waterPos, finalState);

        // Bucket should have been emptied
        var mainHandItem = player.getMainHandItem();
        helper.assertValueEqual(Items.BUCKET, mainHandItem.getItem(), "main hand item");
        helper.assertValueEqual(1, mainHandItem.getCount(), "main hand item count");

        // A second placement attempt should fail since the item in hand is now an empty bucket
        var secondPlacementResult = FluidUtil.tryPlaceFluid(
                handHandler,
                player,
                helper.getLevel(),
                InteractionHand.MAIN_HAND,
                helper.absolutePos(waterPos));
        helper.assertTrue(secondPlacementResult.isEmpty(), "second placement result is empty");
        // Block state should not have changed
        helper.assertBlockState(waterPos, finalState);

        // But another placement with a full bucket in hand should succeed, since placing additional water into a water block is allowed
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        var thirdPlacementResult = FluidUtil.tryPlaceFluid(
                handHandler,
                player,
                helper.getLevel(),
                InteractionHand.MAIN_HAND,
                helper.absolutePos(waterPos));
        helper.assertTrue(secondPlacementResult.isEmpty(), "third placement result is empty");
        helper.assertValueEqual(Fluids.WATER, thirdPlacementResult.getFluid(), "third placed fluid");
        helper.assertValueEqual(FluidType.BUCKET_VOLUME, thirdPlacementResult.getAmount(), "third placed amount");
        helper.assertBlockState(waterPos, finalState);

        // Bucket should have been emptied
        mainHandItem = player.getMainHandItem();
        helper.assertValueEqual(Items.BUCKET, mainHandItem.getItem(), "main hand item");
        helper.assertValueEqual(1, mainHandItem.getCount(), "main hand item count");

        // Block state should not have changed
        helper.assertBlockState(waterPos, finalState);

        helper.succeed();
    }
}
