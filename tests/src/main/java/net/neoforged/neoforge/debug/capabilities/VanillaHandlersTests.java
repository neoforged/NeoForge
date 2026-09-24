/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
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
        if (invalidationCount.intValue() != 0)
            helper.fail("Should not have been invalidated yet", composterPos);

        // The cache should only be invalidated once until it is queried again
        helper.setBlock(composterPos, Blocks.COMPOSTER.defaultBlockState());
        if (invalidationCount.intValue() != 1)
            helper.fail("Should have invalidated once");

        helper.setBlock(composterPos, Blocks.AIR.defaultBlockState());
        if (invalidationCount.intValue() != 1) // capability not re-queried, so no invalidation
            helper.fail("Should have invalidated once");

        helper.setBlock(composterPos, Blocks.COMPOSTER.defaultBlockState());
        if (invalidationCount.intValue() != 1) // capability not re-queried, so no invalidation
            helper.fail("Should have invalidated once");

        // Should be ok to query now
        if (capCache.getCapability() == null)
            helper.fail("Expected capability", composterPos);
        if (invalidationCount.intValue() != 1)
            helper.fail("Should have invalidated once");

        // Should be notified of disappearance if the composter is removed
        helper.setBlock(composterPos, Blocks.AIR.defaultBlockState());

        if (invalidationCount.intValue() != 2)
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

        var nonCompostable = ItemResource.of(new ItemStack(Blocks.BARRIER, 1));
        if (nonCompostable.has(DataComponents.COMPOSTABLE))
            helper.fail("Assumption failed: expected " + nonCompostable + " to be non-compostable");

        // Of particular note to be tested here is the 'null' side; see #2572
        // "IItemHandler for null side of Composter allows items without compost value to be inserted"
        // TODO: change test back to null + all directions once supported by the ComposterWrapper
        var sides = new Direction[] { Direction.UP, Direction.DOWN };

        for (Direction side : sides) {
            var capability = helper.requireCapability(Capabilities.Item.BLOCK, composterPos, side);
            try (Transaction tx = Transaction.openRoot()) {
                var result = capability.insert(0, nonCompostable, 1, tx);
                if (result > 0) {
                    helper.fail("Expected failure to insert non-compostable item for side " + side);
                }
            }
        }

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Test cauldron interactions via the fluid handler capability")
    public static void testCauldronCapability(ExtendedGameTestHelper helper) {
        var cauldronPos = new BlockPos(1, 1, 1);
        FluidResource water = FluidResource.of(Fluids.WATER);
        FluidResource lava = FluidResource.of(Fluids.LAVA);

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
        helper.assertTrue(invalidationCount.intValue() == 1, "Expected 1 invalidation only");

        helper.assertTrue(fluidHandler.size() == 1, "Got %d tanks".formatted(fluidHandler.size()));

        // Simulate filling with water
        try (Transaction tx = Transaction.openRoot()) {
            var fillResult = fluidHandler.insert(water, 2000, tx);
            helper.assertTrue(fillResult == 1000, "Filled " + fillResult);
        }
        helper.assertBlockPresent(Blocks.CAULDRON, cauldronPos);
        try (Transaction tx = Transaction.openRoot()) {
            // Can't fill with less than 1000 though...
            helper.assertTrue(fluidHandler.insert(water, 999, tx) == 0, "Expected 0 fill result");
        }

        // Action!
        try (Transaction tx = Transaction.openRoot()) {
            var fillResult = fluidHandler.insert(water, 2000, tx);
            helper.assertTrue(fillResult == 1000, "Filled " + fillResult);
            tx.commit();
        }
        helper.assertBlockState(cauldronPos, state -> state.is(Blocks.WATER_CAULDRON) && state.getValue(LayeredCauldronBlock.LEVEL) == 3, _ -> Component.literal("Expected level 3 cauldron"));

        helper.assertTrue(fluidHandler.getResource(0).is(Fluids.WATER) && fluidHandler.getAmountAsInt(0) == 1000, "Expected 1000 water");

        // Try to empty as well
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertTrue(fluidHandler.extract(lava, 1000, tx) == 0, "Cannot drain lava");
            helper.assertTrue(fluidHandler.extract(water, 999, tx) == 0, "Cannot drain less than 1000 water");
        }
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertTrue(fluidHandler.extract(water, 1000, tx) == 1000, "Expected drain of 1000 water");
            tx.commit();
        }

        helper.assertBlockPresent(Blocks.CAULDRON, cauldronPos);
        helper.assertTrue(fluidHandler.getResource(0).isEmpty(), "Expected empty handler");

        // Try lava cauldron
        helper.setBlock(cauldronPos, Blocks.LAVA_CAULDRON);
        helper.assertTrue(fluidHandler.getResource(0).is(Fluids.LAVA) && fluidHandler.getAmountAsInt(0) == 1000, "Expected 1000 lava");
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertTrue(fluidHandler.extract(lava, 1000, tx) == 1000, "Expected drain of 1000 lava");
            tx.commit();
        }
        helper.assertBlockPresent(Blocks.CAULDRON, cauldronPos);

        // Try partial water filling
        helper.setBlock(cauldronPos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 2));
        helper.assertTrue(fluidHandler.getResource(0).is(Fluids.WATER) && fluidHandler.getAmountAsInt(0) == 666, "Expected 666 water");
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertTrue(fluidHandler.extract(water, 1000, tx) == 0, "Expected no water drain from partial cauldron");
            helper.assertTrue(fluidHandler.insert(water, 1000, tx) == 0, "Expected no water fill to partial cauldron");
        }

        // None of this should have invalidated the capability
        helper.assertTrue(invalidationCount.intValue() == 1, "Expected 1 invalidation only after the whole test");
        // But if we change the block to a non-cauldron, it should invalidate
        helper.destroyBlock(cauldronPos);
        helper.assertTrue(invalidationCount.intValue() == 2, "Expected a second invalidation after cauldron destruction");

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Test that item capabilities are exposed on all base vanilla types")
    public static void testItemCapabilitiesExposed(ExtendedGameTestHelper helper) {
        var targetPos = new BlockPos(1, 1, 1);
        var absoluteTarget = helper.absolutePos(targetPos);

        //Blocks
        for (Map.Entry<ResourceKey<Block>, Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
            Identifier blockId = entry.getKey().identifier();
            if (!blockId.getNamespace().equals(Identifier.DEFAULT_NAMESPACE))
                continue;
            Block block = entry.getValue();
            helper.setBlock(targetPos, block.defaultBlockState());
            boolean expectsCapability = false;
            if (block instanceof WorldlyContainerHolder) {
                expectsCapability = true;
            } else if (block instanceof EntityBlock) {
                BlockEntity blockEntity = helper.getLevel().getBlockEntity(absoluteTarget);
                if (blockEntity instanceof Container) {
                    expectsCapability = true;
                }
            }
            if (expectsCapability) {
                boolean hasCapability = false;
                if (helper.getCapability(Capabilities.Item.BLOCK, targetPos, null) == null) {
                    for (Direction side : Direction.values()) {
                        if (helper.getCapability(Capabilities.Item.BLOCK, targetPos, side) != null) {
                            hasCapability = true;
                            break;
                        }
                    }
                } else {
                    hasCapability = true;
                }
                helper.assertTrue(hasCapability, "Expected " + blockId + " to have an item handler capability exposed on at least one side");
            }
        }

        //Entities
        for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            Identifier entityId = entry.getKey().identifier();
            if (!entityId.getNamespace().equals(Identifier.DEFAULT_NAMESPACE))
                continue;
            //Like GameTestEntityBuilder#spawn
            Entity entity = entry.getValue().create(helper.getLevel(), EntitySpawnReason.STRUCTURE);
            if (entity instanceof ContainerEntity) {
                helper.assertNotNull(Capabilities.Item.ENTITY.getCapability(entity, null), "Expected entity type " + entityId + " that is a ContainerEntity to expose a capability");
            }
        }

        //Items
        for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            Identifier itemId = entry.getKey().identifier();
            if (!itemId.getNamespace().equals(Identifier.DEFAULT_NAMESPACE))
                continue;
            ItemStack stack = new ItemStack(entry.getValue());
            if (stack.has(DataComponents.CONTAINER) && stack.getMaxStackSize() == 1) {
                helper.assertNotNull(Capabilities.Item.ITEM.getCapability(stack, ItemAccess.forStack(stack)), "Expected item " + itemId + " that has a container component to expose a capability");
            }
        }
        helper.succeed();
    }
}
