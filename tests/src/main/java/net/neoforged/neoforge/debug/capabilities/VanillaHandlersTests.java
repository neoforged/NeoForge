/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities;

import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;
import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;
import org.apache.commons.lang3.mutable.MutableInt;

@ForEachTest(groups = "capabilities.vanillahandlers")
public class VanillaHandlersTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that composter capabilities get invalidated correctly")
    public static void testComposterInvalidation(ExtendedGameTestHelper helper) {
        var composterPos = new BlockPos(1, 1, 1);

        MutableInt invalidationCount = new MutableInt();
        var capCache = BlockCapabilityCache.create(
                Capabilities.Item.BLOCK,
                helper.getLevel(),
                helper.absolutePos(composterPos),
                Direction.UP,
                () -> true,
                invalidationCount::increment);

        if (capCache.getCapability() != null)
            helper.fail("Expected no capability", composterPos);
        if (capCache.getCapability() != null) // check again just in case
            helper.fail("Expected no capability", composterPos);
        if (invalidationCount.getValue() != 0)
            helper.fail("Should not have been invalidated yet", composterPos);

        // The cache should only be invalidated once until it is queried again
        helper.setBlock(composterPos, Blocks.COMPOSTER.defaultBlockState());
        if (invalidationCount.getValue() != 1)
            helper.fail("Should have invalidated once");

        helper.setBlock(composterPos, Blocks.AIR.defaultBlockState());
        if (invalidationCount.getValue() != 1) // capability not re-queried, so no invalidation
            helper.fail("Should have invalidated once");

        helper.setBlock(composterPos, Blocks.COMPOSTER.defaultBlockState());
        if (invalidationCount.getValue() != 1) // capability not re-queried, so no invalidation
            helper.fail("Should have invalidated once");

        // Should be ok to query now
        if (capCache.getCapability() == null)
            helper.fail("Expected capability", composterPos);
        if (invalidationCount.getValue() != 1)
            helper.fail("Should have invalidated once");

        // Should be notified of disappearance if the composter is removed
        helper.setBlock(composterPos, Blocks.AIR.defaultBlockState());

        if (invalidationCount.getValue() != 2)
            helper.fail("Should have invalidated a second time");
        if (capCache.getCapability() != null)
            helper.fail("Expected no capability", composterPos);

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that non-compostable items cannot be inserted into a composter")
    public static void testComposterCannotAcceptNonCompostables(ExtendedGameTestHelper helper) {
        var composterPos = new BlockPos(1, 1, 1);

        helper.setBlock(composterPos, Blocks.COMPOSTER.defaultBlockState());

        var nonCompostable = new ItemStack(Blocks.BARRIER, 1);
        if (ComposterBlock.getValue(nonCompostable) > 0)
            helper.fail("Assumption failed: expected " + nonCompostable + " to be non-compostable");

        // Of particular note to be tested here is the 'null' side; see #2572
        // "IItemHandler for null side of Composter allows items without compost value to be inserted"
        // TODO: change test back to null + all directions once supported by the ComposterWrapper
        var sides = new Direction[] { Direction.UP, Direction.DOWN };

        for (Direction side : sides) {
            var capability = IItemHandler.of(helper.requireCapability(Capabilities.Item.BLOCK, composterPos, side));
            var result = capability.insertItem(0, nonCompostable, false);
            if (result.isEmpty())
                helper.fail("Expected failure to insert non-compostable item for side " + side);
        }

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Test cauldron interactions via the fluid handler capability")
    public static void testCauldronCapability(ExtendedGameTestHelper helper) {
        var cauldronPos = new BlockPos(1, 1, 1);

        MutableInt invalidationCount = new MutableInt();
        var capCache = BlockCapabilityCache.create(
                Capabilities.Fluid.BLOCK,
                helper.getLevel(),
                helper.absolutePos(cauldronPos),
                Direction.UP,
                () -> true,
                invalidationCount::increment);

        // Capability should be absent
        helper.assertTrue(capCache.getCapability() == null, "Expected no capability");

        // Should invalidate once when setting the block
        helper.setBlock(cauldronPos, Blocks.CAULDRON);
        var fluidHandler = capCache.getCapability();
        helper.assertNotNull(fluidHandler, "Expected fluid handler");
        // Note: this uses the legacy wrappers, testing the wrappers and that the new CauldronWrapper matches the old one.
        var wrapper = IFluidHandler.of(fluidHandler);
        helper.assertTrue(invalidationCount.intValue() == 1, "Expected 1 invalidation only");

        helper.assertTrue(wrapper.getTanks() == 1, "Got %d tanks".formatted(wrapper.getTanks()));

        // Simulate filling with water
        var fillResult = wrapper.fill(new FluidStack(Fluids.WATER, 2000), SIMULATE);
        helper.assertTrue(fillResult == 1000, "Filled " + fillResult);
        helper.assertBlockPresent(Blocks.CAULDRON, cauldronPos);
        // Can't fill with less than 1000 though...
        helper.assertTrue(wrapper.fill(new FluidStack(Fluids.WATER, 999), SIMULATE) == 0, "Expected 0 fill result");

        // Action!
        fillResult = wrapper.fill(new FluidStack(Fluids.WATER, 2000), EXECUTE);
        helper.assertTrue(fillResult == 1000, "Filled " + fillResult);
        helper.assertBlockState(cauldronPos, state -> state.is(Blocks.WATER_CAULDRON) && state.getValue(LayeredCauldronBlock.LEVEL) == 3, $ -> Component.literal("Expected level 3 cauldron"));

        helper.assertTrue(FluidStack.matches(wrapper.getFluidInTank(0), new FluidStack(Fluids.WATER, 1000)), "Expected 1000 water");

        // Try to empty as well
        helper.assertTrue(wrapper.drain(new FluidStack(Fluids.LAVA, 1000), EXECUTE).isEmpty(), "Cannot drain lava");
        helper.assertTrue(wrapper.drain(new FluidStack(Fluids.WATER, 999), EXECUTE).isEmpty(), "Cannot drain less than 1000 water");
        helper.assertTrue(FluidStack.matches(wrapper.drain(new FluidStack(Fluids.WATER, 1000), EXECUTE), new FluidStack(Fluids.WATER, 1000)), "Expected drain of 1000 water");

        helper.assertBlockPresent(Blocks.CAULDRON, cauldronPos);
        helper.assertTrue(wrapper.getFluidInTank(0).isEmpty(), "Expected empty handler");

        // Try lava cauldron
        helper.setBlock(cauldronPos, Blocks.LAVA_CAULDRON);
        helper.assertTrue(FluidStack.matches(wrapper.getFluidInTank(0), new FluidStack(Fluids.LAVA, 1000)), "Expected 1000 lava");
        helper.assertTrue(FluidStack.matches(wrapper.drain(1000, EXECUTE), new FluidStack(Fluids.LAVA, 1000)), "Expected drain of 1000 lava");
        helper.assertBlockPresent(Blocks.CAULDRON, cauldronPos);

        // Try partial water filling
        helper.setBlock(cauldronPos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 2));
        helper.assertTrue(FluidStack.matches(wrapper.getFluidInTank(0), new FluidStack(Fluids.WATER, 666)), "Expected 666 water");
        helper.assertTrue(wrapper.drain(1000, EXECUTE).isEmpty(), "Expected no water drain from partial cauldron");
        helper.assertTrue(wrapper.fill(new FluidStack(Fluids.WATER, 1000), EXECUTE) == 0, "Expected no water fill to partial cauldron");

        // None of this should have invalidated the capability
        helper.assertTrue(invalidationCount.intValue() == 1, "Expected 1 invalidation only after the whole test");
        // But if we change the block to a non-cauldron, it should invalidate
        helper.destroyBlock(cauldronPos);
        helper.assertTrue(invalidationCount.intValue() == 2, "Expected a second invalidation after cauldron destruction");

        helper.succeed();
    }
}
