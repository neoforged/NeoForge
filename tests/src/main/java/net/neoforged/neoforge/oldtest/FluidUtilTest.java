/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.transfer.fluids.FluidUtil;
import net.neoforged.neoforge.transfer.fluids.templates.AttachmentFluidStorage;
import net.neoforged.neoforge.transfer.fluids.templates.ItemFluidStorage;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.apache.logging.log4j.LogManager;

import java.util.function.Supplier;

/**
 * Various tests for {@link FluidUtil}, that run when the mod is loaded.
 * If one of the tests fails, an expection will be thrown, and mod loading will fail with an error.
 * If all tests pass, the mod will load successfully.
 */
@Mod(FluidUtilTest.MODID)
public class FluidUtilTest {
    public static final String MODID = "fluid_util_test";

    public FluidUtilTest(IEventBus modEventBus) {
        modEventBus.addListener(FluidUtilTest::runTests);
    }

    private static void runTests(FMLLoadCompleteEvent commonSetupEvent) {
        // test_tryEmptyContainerAndStow_stackable();

        LogManager.getLogger().info("FluidUtilTest ok!");
    }


    /**
     * Ensures that tryEmptyContainerAndStow doesn't duplicate fluids in the target when the container is stackable.
     * Regression test for <a href="https://github.com/MinecraftForge/MinecraftForge/issues/6796">issue #6796</a>.
     */
    /*
    @GameTest
    private static void test_tryEmptyContainerAndStow_stackable(DynamicTest test, RegistrationHelper reg) {
        var sourceStack = new ItemStack(Items.WATER_BUCKET, 2);
        var attachmentType = reg.attachments().register("fluid", AttachmentType.builder(() -> SimpleFluidContent.EMPTY).serialize(SimpleFluidContent.CODEC)::build);
        test.onGameTest(helper -> {
            BlockEntity blockEntity = helper.getBlockEntity(BlockPos.ZERO, FurnaceBlockEntity.class);
            var targetTank = new AttachmentFluidStorage(blockEntity, attachmentType, 1000);
            var overflowInventory = new ItemStackHandler(1);

            // Simulate first: it's not supposed to modify anything!
            var simulateResult = FluidUtil.tryEmptyContainerAndStow(sourceStack, targetTank, overflowInventory, 1000, null, false);
            if (!simulateResult.isSuccess())
                throw new AssertionError("Failed to transfer.");
            checkItemStack(simulateResult.getResult(), Items.WATER_BUCKET, 1);
            // Tank and inv shouldn't be modified for simulate
            checkItemStack(sourceStack, Items.WATER_BUCKET, 2);
            checkFluidStack(targetTank.getFluid(), Fluids.EMPTY, 0);
            checkItemStack(overflowInventory.getStackInSlot(0), Items.AIR, 0);

            // Now test with execute
            var executeResult = FluidUtil.tryEmptyContainerAndStow(sourceStack, targetTank, overflowInventory, 1000, null, true);
            if (!executeResult.isSuccess())
                throw new AssertionError("Failed to transfer.");
            checkItemStack(executeResult.getResult(), Items.WATER_BUCKET, 1);
            checkFluidStack(targetTank.getFluid(), Fluids.WATER, 1000);
            checkItemStack(overflowInventory.getStackInSlot(0), Items.BUCKET, 1);
        });
    }
     */

    private static void checkItemStack(ItemStack stack, Item item, int count) {
        if (stack.getItem() != item)
            throw new AssertionError("Expected item " + BuiltInRegistries.ITEM.getKey(item) + ", got: " + BuiltInRegistries.ITEM.getKey(stack.getItem()));
        if (stack.getCount() != count)
            throw new AssertionError("Expected count " + count + ", got: " + stack.getCount());
    }

    private static void checkFluidStack(FluidStack stack, Fluid fluid, int amount) {
        if (!stack.is(fluid))
            throw new AssertionError("Expected fluid " + BuiltInRegistries.FLUID.getKey(fluid) + ", got: " + BuiltInRegistries.FLUID.getKey(stack.getFluid()));
        if (stack.getAmount() != amount)
            throw new AssertionError("Expected amount " + amount + ", got: " + stack.getAmount());
    }
}
