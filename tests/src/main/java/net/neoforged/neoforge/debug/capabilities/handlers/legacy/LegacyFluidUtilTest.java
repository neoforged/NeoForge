/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers.legacy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.debug.capabilities.handlers.resources.ResourceHandlerTestSetup;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

/**
 * Various tests for {@link FluidUtil}, that run when the mod is loaded.
 * If one of the tests fails, an expection will be thrown, and mod loading will fail with an error.
 * If all tests pass, the mod will load successfully.
 */
@ForEachTest(groups = ResourceHandlerTestSetup.GROUP_ID, idPrefix = "resource.handler.legacy.fluid_util.")
public class LegacyFluidUtilTest {
    /**
     * Ensures that tryEmptyContainer doesn't change the target fluid handler when simulating.
     * Regression test for the root cause of <a href="https://github.com/MinecraftForge/MinecraftForge/issues/6796">issue #6796</a>.
     */
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that tryEmptyContainer doesn't change the target fluid handler when simulating.")
    private static void test_tryEmptyContainer(ExtendedGameTestHelper helper) {
        var sourceStack = new ItemStack(Items.WATER_BUCKET, 2);
        var targetTank = new FluidTank(10000);

        // Simulate is not supposed to modify anything
        var simulateResult = FluidUtil.tryEmptyContainer(sourceStack, targetTank, 1000, null, false);
        helper.assertTrue(simulateResult.isSuccess(), "Failed to transfer.");
        checkItemStack(helper, simulateResult.getResult(), Items.BUCKET, 1);
        // Tank and stack shouldn't be modified
        checkItemStack(helper, sourceStack, Items.WATER_BUCKET, 2);
        checkFluidStack(helper, targetTank.getFluid(), Fluids.EMPTY, 0);

        // Execute should modify
        var executeResult = FluidUtil.tryEmptyContainer(sourceStack, targetTank, 1000, null, true);
        helper.assertTrue(executeResult.isSuccess(), "Failed to transfer.");
        checkItemStack(helper, executeResult.getResult(), Items.BUCKET, 1);
        checkFluidStack(helper, targetTank.getFluid(), Fluids.WATER, 1000);
        checkItemStack(helper, sourceStack, Items.WATER_BUCKET, 2); // Apparently the stack is not supposed to be modified
        helper.succeed();
    }

    /**
     * Ensures that tryFillContainer doesn't change the target fluid handler when simulating.
     * Ant that the result of the simulated transfver is valid.
     * Regression test for the root cause of <a href="https://github.com/MinecraftForge/MinecraftForge/issues/6796">issue #6796</a>.
     */
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that tryFillContainer doesn't change the target fluid handler when simulating.")
    private static void test_tryFillContainer(ExtendedGameTestHelper helper) {
        var targetStack = new ItemStack(Items.BUCKET, 2);
        var sourceTank = new FluidTank(10000);
        sourceTank.setFluid(new FluidStack(Fluids.WATER, 5000));

        // Simulate is not supposed to modify anything
        var simulateResult = FluidUtil.tryFillContainer(targetStack, sourceTank, 1000, null, false);
        helper.assertTrue(simulateResult.isSuccess(), "Failed to transfer.");
        checkItemStack(helper, simulateResult.getResult(), Items.WATER_BUCKET, 1);
        // Tank and stack shouldn't be modified
        checkItemStack(helper, targetStack, Items.BUCKET, 2);
        checkFluidStack(helper, sourceTank.getFluid(), Fluids.WATER, 5000);

        // Execute should modify
        var executeResult = FluidUtil.tryFillContainer(targetStack, sourceTank, 1000, null, true);
        helper.assertTrue(executeResult.isSuccess(), "Failed to transfer.");
        checkItemStack(helper, executeResult.getResult(), Items.WATER_BUCKET, 1);
        checkFluidStack(helper, sourceTank.getFluid(), Fluids.WATER, 4000);
        checkItemStack(helper, targetStack, Items.BUCKET, 2);
        helper.succeed();
    }

    /**
     * Ensures that tryEmptyContainerAndStow doesn't duplicate fluids in the target when the container is stackable.
     * Regression test for <a href="https://github.com/MinecraftForge/MinecraftForge/issues/6796">issue #6796</a>.
     */
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Ensures that tryEmptyContainerAndStow doesn't duplicate fluids in the target when the container is stackable.")
    private static void test_tryEmptyContainerAndStow_stackable(ExtendedGameTestHelper helper) {
        var sourceStack = new ItemStack(Items.WATER_BUCKET, 2);
        var targetTank = new FluidTank(10000);
        var overflowInventory = new ItemStackHandler(1);

        // Simulate first: it's not supposed to modify anything!
        var simulateResult = FluidUtil.tryEmptyContainerAndStow(sourceStack, targetTank, overflowInventory, 1000, null, false);
        helper.assertTrue(simulateResult.isSuccess(), "Failed to transfer.");
        checkItemStack(helper, simulateResult.getResult(), Items.WATER_BUCKET, 1);
        // Tank and inv shouldn't be modified for simulate
        checkItemStack(helper, sourceStack, Items.WATER_BUCKET, 2);
        checkFluidStack(helper, targetTank.getFluid(), Fluids.EMPTY, 0);
        checkItemStack(helper, overflowInventory.getStackInSlot(0), Items.AIR, 0);

        // Now test with execute
        var executeResult = FluidUtil.tryEmptyContainerAndStow(sourceStack, targetTank, overflowInventory, 1000, null, true);
        helper.assertTrue(executeResult.isSuccess(), "Failed to transfer.");
        checkItemStack(helper, executeResult.getResult(), Items.WATER_BUCKET, 1);
        checkFluidStack(helper, targetTank.getFluid(), Fluids.WATER, 1000);
        checkItemStack(helper, overflowInventory.getStackInSlot(0), Items.BUCKET, 1);
        helper.succeed();
    }

    private static void checkItemStack(ExtendedGameTestHelper helper, ItemStack stack, Item item, int count) {
        helper.assertValueEqual(stack.getItem(), item, "Expected item " + BuiltInRegistries.ITEM.getKey(item) + ", got: " + BuiltInRegistries.ITEM.getKey(stack.getItem()));
        helper.assertValueEqual(stack.getCount(), count, "Expected count " + count + ", got: " + stack.getCount());
    }

    private static void checkFluidStack(ExtendedGameTestHelper helper, FluidStack stack, Fluid fluid, int amount) {
        helper.assertTrue(stack.is(fluid), "Expected fluid " + BuiltInRegistries.FLUID.getKey(fluid) + ", got: " + BuiltInRegistries.FLUID.getKey(stack.getFluid()));
        helper.assertValueEqual(stack.getAmount(), amount, "Expected amount " + amount + ", got: " + stack.getAmount());
    }
}
