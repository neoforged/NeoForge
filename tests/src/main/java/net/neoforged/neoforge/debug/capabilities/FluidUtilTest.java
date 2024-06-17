/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.fluids.FluidUtil;
import net.neoforged.neoforge.transfer.fluids.templates.AttachmentFluidStorage;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Various tests for {@link FluidUtil}, that run when the mod is loaded.
 * If one of the tests fails, an expection will be thrown, and mod loading will fail with an error.
 * If all tests pass, the mod will load successfully.
 */
@ForEachTest(groups = "capabilities.fluid.util")
public class FluidUtilTest {
    public static final String MODID = "fluid_util_test";
    private static final RegistrationHelper HELPER = RegistrationHelper.create("item_fluid_util_tests");
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = HELPER.attachments();

    private static final Supplier<AttachmentType<SimpleFluidContent>> FLUID_COMPONENT = ATTACHMENTS.register("test_fluid", AttachmentType.builder(() -> SimpleFluidContent.EMPTY)::build);

    @OnInit
    static void init(final TestFramework framework) {
        ATTACHMENTS.register(framework.modEventBus());
        framework.modEventBus().addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, BlockEntityType.FURNACE, (holder, ctx) -> {
                return new AttachmentFluidStorage(holder, FLUID_COMPONENT.get(), FluidType.BUCKET_VOLUME);
            });
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that FluidUtil#tryPickupFluid works correctly")
    private static void test_tryPickupFluid(DynamicTest test, RegistrationHelper reg) {
        test.onGameTest(helper -> {
            BlockPos pos = helper.relativePos(BlockPos.ZERO);
            Player player = helper.makeMockPlayer();
            // test pickup of water and ensure exchange shrinks main stack and puts overflow in inventory
            resetInventory(player, new ItemStack(Items.BUCKET, 2));
            resetWater(helper, pos);

            FluidUtil.tryPickupFluid(player, InteractionHand.MAIN_HAND, helper.getLevel(), pos);

            checkInventory(player, Items.BUCKET, 1, Items.WATER_BUCKET);
            checkIfWaterWasPickedUp(helper, pos);

            // test pickup of waterlogged block and ensure exchange shrinks main stack and puts overflow in inventory
            resetInventory(player, new ItemStack(Items.BUCKET, 1));
            resetWaterloggedBlock(helper, pos);

            FluidUtil.tryPickupFluid(player, InteractionHand.MAIN_HAND, helper.getLevel(), pos);

            checkInventory(player, Items.WATER_BUCKET, 1, null);
            checkIfWaterWasPickedUp(helper, pos);
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that FluidUtil#tryPlaceFluid works correctly")
    private static void test_tryPlaceFluid(DynamicTest test, RegistrationHelper reg) {
        test.onGameTest(helper -> {
            BlockPos pos = helper.relativePos(BlockPos.ZERO);
            Player player = helper.makeMockPlayer();
            // test placing water and ensure exchange shrinks main stack and puts overflow in inventory
            resetInventory(player, new ItemStack(Items.WATER_BUCKET, 2));
            resetAir(helper, pos);

            FluidUtil.tryPlaceFluid(player, InteractionHand.MAIN_HAND, helper.getLevel(), pos);

            checkInventory(player, Items.WATER_BUCKET, 1, Items.BUCKET);
            checkIfWaterWasPlaced(helper, pos);

            // test placing waterlogged block and ensure exchange shrinks main stack
            resetInventory(player, new ItemStack(Items.WATER_BUCKET, 1));
            resetBlock(helper, pos);

            FluidUtil.tryPlaceFluid(player, InteractionHand.MAIN_HAND, helper.getLevel(), pos);

            checkInventory(player, Items.BUCKET, 1, null);
            checkIfWaterWasPlaced(helper, pos);
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that FluidUtil#interactWithFluidHandler works correctly")
    private static void test_interactFluidHandler(DynamicTest test, RegistrationHelper reg) {
        var attachmentType = reg.attachments().registerSimpleAttachment("fluid", () -> SimpleFluidContent.EMPTY);
        test.onGameTest(helper -> {
            BlockPos pos = helper.relativePos(BlockPos.ZERO);
            Player player = helper.makeMockPlayer();
            // test inserting water and ensure exchange replaces main stack with empty container
            resetInventory(player, new ItemStack(Items.WATER_BUCKET, 1));
            resetTank(helper, pos, 0);

            FluidUtil.interactWithFluidHandler(player, InteractionHand.MAIN_HAND, helper.getLevel(), pos, null);

            checkInventory(player, Items.BUCKET, 0, null);
            checkTank(helper, pos, 1000);

            // test placing waterlogged block and ensure exchange replaces main stack with full container
            FluidUtil.tryPickupFluid(player, InteractionHand.MAIN_HAND, helper.getLevel(), pos);

            checkInventory(player, Items.WATER_BUCKET, 1, null);
            checkTank(helper, pos, 0);

            // test extracting water and ensure exchange shrinks main stack and puts overflow in inventory
            resetInventory(player, new ItemStack(Items.BUCKET, 2));
            resetTank(helper, pos, 1000);

            FluidUtil.interactWithFluidHandler(player, InteractionHand.MAIN_HAND, helper.getLevel(), pos, null);

            checkInventory(player, Items.BUCKET, 1, Items.WATER_BUCKET);
            checkTank(helper, pos, 0);

            // test extracting water and ensure exchange shrinks main stack and puts overflow in inventory
            resetInventory(player, new ItemStack(Items.WATER_BUCKET, 2));

            FluidUtil.interactWithFluidHandler(player, InteractionHand.MAIN_HAND, helper.getLevel(), pos, null);

            checkInventory(player, Items.WATER_BUCKET, 1, Items.BUCKET);
            checkTank(helper, pos, 1000);
        });
    }

    public static void resetWater(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Blocks.WATER.defaultBlockState());
    }

    public static void resetWaterloggedBlock(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true));
    }

    public static void resetAir(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Blocks.AIR.defaultBlockState());
    }

    public static void resetBlock(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Blocks.SANDSTONE_STAIRS.defaultBlockState());
    }

    public static void checkIfWaterWasPickedUp(GameTestHelper helper, BlockPos pos) {
        if (helper.getLevel().getFluidState(pos).getType() != Fluids.EMPTY) throw new AssertionError("Failed to pick up water.");
    }

    public static void checkIfWaterWasPlaced(GameTestHelper helper, BlockPos pos) {
        if (helper.getLevel().getFluidState(pos).getType() != Fluids.WATER) throw new AssertionError("Failed to place water.");
    }

    public static void resetTank(GameTestHelper helper, BlockPos pos, int amount) {
        helper.setBlock(pos, Blocks.FURNACE.defaultBlockState());
        IResourceHandler<FluidResource> handler = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
        if (handler == null) throw new AssertionError("Failed to get fluid handler.");
        if (amount > 0) {
            handler.insert(Fluids.WATER.defaultResource, amount, TransferAction.EXECUTE);
        }
    }

    public static void checkTank(GameTestHelper helper, BlockPos pos, int amount) {
        IResourceHandler<FluidResource> handler = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
        if (handler == null) throw new AssertionError("Failed to get fluid handler.");
        if (handler.getAmount(0) != amount) throw new AssertionError("Expected " + amount + " fluid in tank, got " + handler.getAmount(0));
    }

    public static void resetInventory(Player player, ItemStack stack) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            player.getInventory().setItem(i, ItemStack.EMPTY);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
    }

    //checks if main hand has `mainCount` mainItems(s) and inventory has 1 inventoryItem. If it doesn't, throw
    public static void checkInventory(Player player, Item mainItem, int mainCount, @Nullable Item inventoryItem) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack inventory = ItemStack.EMPTY;
        if (inventoryItem == null) {
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty()) {
                    inventory = stack;
                    break;
                }
            }
        }
        if (!mainHand.is(mainItem) || mainHand.getCount() != mainCount || (inventoryItem != null && (!inventory.is(inventoryItem) || inventory.getCount() != 1))) {
            throw new AssertionError("Expected " + mainCount + " " + mainItem + " in hand and 1 " + inventoryItem + " in inventory, got: " + mainHand + " and " + inventory);
        }
    }
}
