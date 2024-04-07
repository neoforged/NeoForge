/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ToolActions;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.eventtest.internal.TestsMod;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;

@ForEachTest(groups = { BlockTests.GROUP + ".event", "event" })
public class BlockEventTests {
    @GameTest(template = TestsMod.TEMPLATE_3x3)
    @TestHolder(description = "Tests if the BlockDestroyed event is fired and works properly.")
    public static void blockDestroyedEvent(final DynamicTest test) {
        test.whenEnabled(listeners -> listeners.forge().addListener((final BlockDropsEvent event) -> {
            if (event.getState().getBlock() == Blocks.DIRT && !event.getTool().is(ItemTags.SHOVELS)) {
                event.setCanceled(true); // Make dirt not drop unless it was broken by a shovel to test cancellation as a whole.
            }
            if (event.getState().getBlock() == Blocks.NETHER_QUARTZ_ORE && event.getTool().is(Items.IRON_PICKAXE)) {
                event.setDropXpWhenCancelled(true);
                event.setCanceled(true); // Make Nether Quartz only drop XP and no items when broken with an Iron Pickaxe.
            }
            test.pass();
        }));

        BlockPos pos = new BlockPos(1, 1, 1);

        test.onGameTest(helper -> helper
                .startSequence() // Dirt Test
                .thenExecute(() -> helper.setBlock(pos, Blocks.DIRT))
                .thenExecute(() -> helper.breakBlock(pos, new ItemStack(Items.DIAMOND_HOE), helper.makeMockSurvivalPlayer()))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.DIRT, pos))
                .thenExecute(() -> helper.assertItemEntityNotPresent(Items.DIRT))
                .thenIdle(5) // Quartz Test
                .thenExecute(() -> helper.setBlock(pos, Blocks.NETHER_QUARTZ_ORE))
                .thenExecute(() -> helper.breakBlock(pos, new ItemStack(Items.IRON_PICKAXE), helper.makeMockSurvivalPlayer()))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.NETHER_QUARTZ_ORE, pos))
                .thenExecute(() -> helper.assertItemEntityNotPresent(Items.QUARTZ))
                .thenExecute(() -> helper.assertEntityPresent(EntityType.EXPERIENCE_ORB))
                .thenSucceed());
    }

    @GameTest(template = TestsMod.TEMPLATE_3x3)
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

    @GameTest(template = TestsMod.TEMPLATE_3x3)
    @TestHolder(description = "Tests if the block modification event is fired")
    public static void blockModificationEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final BlockEvent.BlockToolModificationEvent event) -> {
            if (event.getToolAction() == ToolActions.AXE_STRIP) {
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
