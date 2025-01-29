/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
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
                Capabilities.ItemHandler.BLOCK,
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
    @TestHolder(description = "Test cauldron interactions via the fluid handler capability")
    public static void testCauldronCapability(ExtendedGameTestHelper helper) {
        var cauldronPos = new BlockPos(1, 1, 1);

        MutableInt invalidationCount = new MutableInt();
        var capCache = BlockCapabilityCache.create(
                Capabilities.FluidHandler.BLOCK,
                helper.getLevel(),
                helper.absolutePos(cauldronPos),
                Direction.UP,
                () -> true,
                invalidationCount::increment);

        // Capability should be absent
        helper.assertTrue(capCache.getCapability() == null, "Expected no capability");

        // Should invalidate once when setting the block
        helper.setBlock(cauldronPos, Blocks.CAULDRON);

        var wrapper = capCache.getCapability();
        helper.assertTrue(wrapper != null, "Expected fluid handler");
        helper.assertTrue(invalidationCount.intValue() == 1, "Expected 1 invalidation only");

        helper.assertTrue(wrapper.size() == 1, "Got %d tanks".formatted(wrapper.size()));

        var waterResource = Fluids.WATER.defaultResource;
        var lavaResource = Fluids.LAVA.defaultResource;

        // Simulate filling with water, and it should only accept 1 bucket
        helper.assertValueEqual(wrapper.insert(waterResource, FluidType.BUCKET_VOLUME * 2, TransferAction.SIMULATE), FluidType.BUCKET_VOLUME, "Should only allow 1 bucket to be inserted.");
        // Can't fill with less than 1000 though...
        helper.assertValueEqual(wrapper.insert(waterResource, FluidType.BUCKET_VOLUME - 1, TransferAction.SIMULATE), 0, "Needs at least 1 bucket and will return 1 bucket. Fill result should match");
        helper.assertBlockPresent(Blocks.CAULDRON, cauldronPos);

        // Excecute tests
        helper.assertValueEqual(wrapper.insert(waterResource, FluidType.BUCKET_VOLUME * 2, TransferAction.EXECUTE), FluidType.BUCKET_VOLUME, "Should only allow 1 bucket to be inserted.");
        helper.assertBlockState(cauldronPos, state -> state.is(Blocks.WATER_CAULDRON) && state.getValue(LayeredCauldronBlock.LEVEL) == 3, () -> "Expected level 3 cauldron");
        helper.assertValueEqual(wrapper.getResource(0), waterResource, "Expected water");
        helper.assertValueEqual(wrapper.getAmount(0), FluidType.BUCKET_VOLUME, "Should match a bucket");

        // Try to empty as well
        helper.assertValueEqual(wrapper.extract(lavaResource, FluidType.BUCKET_VOLUME, TransferAction.EXECUTE), 0, "Cannot drain lava");
        helper.assertFalse(wrapper.getResource(0).isEmpty(), "Water should still be present");
        helper.assertValueEqual(wrapper.extract(waterResource, FluidType.BUCKET_VOLUME - 1, TransferAction.EXECUTE), 0, "Cannot drain less than a bucket");
        helper.assertFalse(wrapper.getResource(0).isEmpty(), "Water should still be present");
        helper.assertValueEqual(wrapper.extract(waterResource, FluidType.BUCKET_VOLUME, TransferAction.EXECUTE), FluidType.BUCKET_VOLUME, "One bucket should be drained");
        helper.assertTrue(wrapper.getResource(0).isEmpty(), "Expected empty handler");

        helper.assertBlockPresent(Blocks.CAULDRON, cauldronPos);

        // Try lava cauldron
        helper.setBlock(cauldronPos, Blocks.LAVA_CAULDRON);
        helper.assertValueEqual(wrapper.getResource(0), lavaResource, "Expected 1000 lava");

        // Try partial water filling
        helper.setBlock(cauldronPos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 2));
        helper.assertValueEqual(wrapper.getResource(0), waterResource, "Expected water");
        helper.assertValueEqual(wrapper.getAmount(0), 666, "Should match");
        helper.assertValueEqual(wrapper.extract(waterResource, FluidType.BUCKET_VOLUME, TransferAction.EXECUTE), 0, "Expected no water drain from partial cauldron");
        helper.assertValueEqual(wrapper.insert(waterResource, FluidType.BUCKET_VOLUME, TransferAction.EXECUTE), 0, "Expected no water fill to partial cauldron");

        // None of this should have invalidated the capability
        helper.assertTrue(invalidationCount.intValue() == 1, "Expected 1 invalidation only after the whole test");
        // But if we change the block to a non-cauldron, it should invalidate
        helper.destroyBlock(cauldronPos);
        helper.assertTrue(invalidationCount.intValue() == 2, "Expected a second invalidation after cauldron destruction");

        helper.succeed();
    }
}
