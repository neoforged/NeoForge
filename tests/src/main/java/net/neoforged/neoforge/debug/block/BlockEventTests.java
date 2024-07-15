/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;

@ForEachTest(groups = { BlockTests.GROUP + ".event", "event" })
public class BlockEventTests {
    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the BlockDropsEvent prevents dropping items and experience when cancelled.")
    public static void blockDropsEventCancel(final DynamicTest test) {
        test.whenEnabled(listeners -> listeners.forge().addListener((final BlockDropsEvent event) -> {
            // Make Nether Quartz drop nothing when broken with an Iron Pickaxe.
            if (event.getState().getBlock() == Blocks.NETHER_QUARTZ_ORE) {
                // Set the xp drop to a nonzero value to test the positive case
                event.setDroppedExperience(10);

                if (event.getTool().is(Items.IRON_PICKAXE)) {
                    event.setCanceled(true);
                }
            }
            test.pass();
        }));

        BlockPos pos = new BlockPos(1, 1, 1);

        test.onGameTest(helper -> helper
                .startSequence() // Test cancellation with an iron pickaxe. No drops should spawn.
                .thenExecute(() -> helper.setBlock(pos, Blocks.NETHER_QUARTZ_ORE))
                .thenExecute(() -> helper.breakBlock(pos, new ItemStack(Items.IRON_PICKAXE), helper.makeMockPlayer(GameType.SURVIVAL)))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.NETHER_QUARTZ_ORE, pos))
                .thenExecute(() -> helper.assertItemEntityNotPresent(Items.QUARTZ))
                .thenExecute(() -> helper.assertEntityNotPresent(EntityType.EXPERIENCE_ORB))
                .thenIdle(5) // Test that breaking the block normally functions as expected.
                .thenExecute(() -> helper.setBlock(pos, Blocks.NETHER_QUARTZ_ORE))
                .thenExecute(() -> helper.breakBlock(pos, new ItemStack(Items.DIAMOND_PICKAXE), helper.makeMockPlayer(GameType.SURVIVAL)))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.NETHER_QUARTZ_ORE, pos))
                .thenExecute(() -> helper.assertItemEntityPresent(Items.QUARTZ))
                .thenExecute(() -> helper.assertEntityPresent(EntityType.EXPERIENCE_ORB))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the BlockDropsEvent can modify dropped experience.")
    public static void blockDropsEventExperience(final DynamicTest test) {
        test.whenEnabled(listeners -> listeners.forge().addListener((final BlockDropsEvent event) -> {
            if (event.getState().getBlock() == Blocks.EMERALD_BLOCK) {
                // Make emerald blocks drop experience, which doesn't normally occur.
                event.setDroppedExperience(150);
            }
            test.pass();
        }));

        BlockPos pos = new BlockPos(1, 1, 1);

        test.onGameTest(helper -> helper
                .startSequence() // Check that experience orbs show up when breaking an emerald block.
                .thenExecute(() -> helper.setBlock(pos, Blocks.EMERALD_BLOCK))
                .thenExecute(() -> helper.breakBlock(pos, new ItemStack(Items.DIAMOND_PICKAXE), helper.makeMockPlayer(GameType.SURVIVAL)))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.EMERALD_BLOCK, pos))
                .thenExecute(() -> helper.assertItemEntityPresent(Items.EMERALD_BLOCK))
                .thenExecute(() -> helper.assertEntityPresent(EntityType.EXPERIENCE_ORB))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the BlockDropsEvent can move dropped items.")
    public static void blockDropsEventMovement(final DynamicTest test) {
        test.whenEnabled(listeners -> listeners.forge().addListener((final BlockDropsEvent event) -> {
            if (event.getState().getBlock() == Blocks.IRON_BLOCK) {
                // Move the item entity by (-2, -2), which should shift it from relative (2, 1, 2) to (0, 1, 0).
                event.getDrops().forEach(i -> i.setPos(i.position().subtract(2, 0, 2)));
            }
            test.pass();
        }));

        BlockPos pos = new BlockPos(2, 1, 2);
        BlockPos newPos = new BlockPos(0, 1, 0);

        test.onGameTest(helper -> helper
                .startSequence() // Check that experience orbs show up when breaking an emerald block.
                .thenExecute(() -> helper.setBlock(pos, Blocks.IRON_BLOCK))
                .thenExecute(() -> helper.breakBlock(pos, new ItemStack(Items.DIAMOND_PICKAXE), helper.makeMockPlayer(GameType.SURVIVAL)))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.IRON_BLOCK, pos))
                .thenExecute(() -> helper.assertTrue(helper.getEntities(EntityType.ITEM, newPos, 0).size() == 1, "Failed to detect moved iron block"))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the entity place event is fired")
    public static void entityPlacedEvent(final DynamicTest test) {
        test.whenEnabled(listeners -> listeners.forge()
                .addListener((final BlockEvent.EntityPlaceEvent event) -> {
                    if (event.getPlacedBlock().getBlock() == Blocks.CHEST && event.getPlacedAgainst().getBlock() != Blocks.DIAMOND_BLOCK) {
                        event.setCanceled(true);
                    }
                    test.pass();
                }));
        test.onGameTest(helper -> helper
                .startSequence()
                .thenExecute(() -> helper.setBlock(new BlockPos(1, 1, 1), Blocks.BAMBOO_BLOCK))
                .thenExecute(() -> new ItemStack(Items.CHEST).useOn(new UseOnContext(
                        helper.getLevel(), helper.makeMockPlayer(), InteractionHand.MAIN_HAND, new ItemStack(Items.CHEST),
                        new BlockHitResult(helper.absoluteVec(new Vec3(1, 2, 1)), Direction.UP, helper.absolutePos(new BlockPos(1, 2, 1)), false))))
                .thenIdle(3)
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.CHEST, new BlockPos(1, 1, 1)))

                .thenIdle(3)

                .thenExecute(() -> helper.setBlock(new BlockPos(1, 1, 1), Blocks.DIAMOND_BLOCK))
                .thenExecute(() -> new ItemStack(Items.CHEST).useOn(new UseOnContext(
                        helper.getLevel(), helper.makeMockPlayer(), InteractionHand.MAIN_HAND, new ItemStack(Items.CHEST),
                        new BlockHitResult(helper.absoluteVec(new Vec3(1, 2, 1)), Direction.UP, helper.absolutePos(new BlockPos(1, 2, 1)), false))))
                .thenIdle(3)
                .thenExecute(() -> helper.assertBlockPresent(Blocks.CHEST, new BlockPos(1, 2, 1)))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the block modification event is fired")
    public static void blockModificationEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final BlockEvent.BlockToolModificationEvent event) -> {
            if (event.getItemAbility() == ItemAbilities.AXE_STRIP) {
                if (event.getLevel().getBlockState(event.getContext().getClickedPos()).is(Blocks.ACACIA_LOG)) {
                    event.setCanceled(true);
                } else if (event.getFinalState().is(Blocks.DIAMOND_BLOCK) && event.getContext().getClickedFace() == Direction.UP) {
                    event.setFinalState(Blocks.EMERALD_BLOCK.defaultBlockState());
                }
            }
            test.pass();
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.setBlock(new BlockPos(1, 1, 1), Blocks.ACACIA_LOG))
                .thenExecute(() -> helper.useOn(new BlockPos(1, 1, 1), Items.DIAMOND_AXE.getDefaultInstance(), helper.makeMockPlayer(), Direction.UP))
                .thenExecuteAfter(1, () -> helper.assertBlockPresent(Blocks.ACACIA_LOG, new BlockPos(1, 1, 1)))

                .thenIdle(3)

                .thenExecute(() -> helper.setBlock(new BlockPos(1, 2, 1), Blocks.DIAMOND_BLOCK))
                .thenExecute(() -> helper.useOn(new BlockPos(1, 2, 1), Items.DIAMOND_AXE.getDefaultInstance(), helper.makeMockPlayer(), Direction.UP))
                .thenExecuteAfter(1, () -> helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(1, 2, 1)))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the neighbor notify event is fired")
    public static void neighborNotifyEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final BlockEvent.NeighborNotifyEvent event) -> {
            if (event.getState().getBlock() == Blocks.COMPARATOR) {
                event.setCanceled(true);
            }
            test.pass();
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.setBlock(new BlockPos(1, 2, 0), Blocks.COMPOSTER))
                .thenExecute(() -> helper.setBlock(new BlockPos(1, 2, 1), Blocks.COMPARATOR))
                .thenExecute(() -> helper.setBlock(new BlockPos(1, 2, 2), Blocks.REDSTONE_LAMP))

                .thenExecute(() -> helper.useBlock(new BlockPos(1, 2, 0), helper.makeMockPlayer(), Items.ACACIA_LEAVES.getDefaultInstance()))
                .thenExecuteAfter(5, () -> helper.assertBlockProperty(new BlockPos(1, 2, 2), RedstoneLampBlock.LIT, false)) // We haven't triggered a neighbour update (yet)

                .thenExecuteAfter(1, () -> helper.getLevel().setBlock(helper.absolutePos(new BlockPos(1, 3, 2)), Blocks.IRON_BLOCK.defaultBlockState(), 11)) // Now we should trigger an update
                .thenExecuteAfter(5, () -> helper.assertBlockProperty(new BlockPos(1, 2, 2), RedstoneLampBlock.LIT, true))
                .thenSucceed());
    }

    @GameTest(timeoutTicks = 150)
    @TestHolder(description = "Tests if the farmland trample event is fired")
    public static void farmlandTrampleEvent(final DynamicTest test) {
        test.registerGameTestTemplate(StructureTemplateBuilder.withSize(3, 4, 3)
                .placeSustainedWater(1, 1, 1, Blocks.FARMLAND.defaultBlockState()));

        test.eventListeners().forge().addListener((final BlockEvent.FarmlandTrampleEvent event) -> {
            if (event.getEntity().getType() != EntityType.GOAT) {
                event.setCanceled(true);
            }
            test.pass();
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.spawnWithNoFreeWill(EntityType.SHEEP, new BlockPos(0, 5, 1).getCenter()))
                .thenExecuteAfter(40, () -> helper.assertBlockPresent(Blocks.FARMLAND, new BlockPos(0, 2, 1)))
                .thenExecute(() -> helper.killAllEntitiesOfClass(Sheep.class))
                .thenIdle(20)

                .thenExecute(() -> helper.spawnWithNoFreeWill(EntityType.GOAT, new BlockPos(1, 5, 0).getCenter()))
                .thenExecuteAfter(40, () -> helper.assertBlockPresent(Blocks.DIRT, new BlockPos(1, 2, 0)))
                .thenExecute(() -> helper.killAllEntitiesOfClass(Goat.class))
                .thenSucceed());
    }
}
