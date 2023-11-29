/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.level.AlterGroundEvent;
import net.neoforged.neoforge.event.level.SaplingGrowTreeEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;

@ForEachTest(groups = { LevelTests.GROUP + ".event", "event" })
public class LevelEventTests {
    @GameTest
    @EmptyTemplate(value = "9x9x9", floor = true)
    @TestHolder(description = "Tests if the sapling grow tree event is fired, replacing spruce with birch")
    static void saplingGrowTreeEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final SaplingGrowTreeEvent event) -> {
            if (event.getFeature() != null && event.getFeature().is(TreeFeatures.SPRUCE)) {
                event.setFeature(TreeFeatures.BIRCH_BEES_005);
            }
            test.pass();
        });

        test.onGameTest(helper -> {
            final Player player = helper.makeMockPlayer();

            helper.startSequence()
                    .thenExecute(() -> helper.setBlock(4, 1, 4, Blocks.DIRT))
                    .thenExecute(() -> helper.setBlock(4, 2, 4, Blocks.SPRUCE_SAPLING))
                    .thenExecuteFor(10, () -> helper.useOn(new BlockPos(4, 2, 4), Items.BONE_MEAL.getDefaultInstance(), player, Direction.UP))
                    .thenExecute(() -> helper.assertBlockPresent(Blocks.BIRCH_LOG, 4, 2, 4))
                    .thenSucceed();
        });
    }

    @GameTest
    @TestHolder
    static void alterGroundEvent(final DynamicTest test) {
        test.registerGameTestTemplate(StructureTemplateBuilder.withSize(10, 32, 10)
                .fill(0, 0, 0, 10, 1, 10, Blocks.DIRT.defaultBlockState())
                .set(4, 1, 4, Blocks.SPRUCE_SAPLING.defaultBlockState())
                .set(5, 1, 4, Blocks.SPRUCE_SAPLING.defaultBlockState())
                .set(4, 1, 5, Blocks.SPRUCE_SAPLING.defaultBlockState())
                .set(5, 1, 5, Blocks.SPRUCE_SAPLING.defaultBlockState()));

        test.eventListeners().forge().addListener((final AlterGroundEvent event) -> {
            final AlterGroundEvent.StateProvider old = event.getStateProvider();
            event.setStateProvider((rand, pos) -> {
                final BlockState state = old.getState(rand, pos);
                return state.is(Blocks.PODZOL) ? Blocks.REDSTONE_BLOCK.defaultBlockState() : state;
            });
        });

        test.onGameTest(helper -> {
            final Player player = helper.makeMockPlayer();

            helper.startSequence()
                    .thenExecuteFor(5, () -> helper.useOn(new BlockPos(4, 2, 4), Items.BONE_MEAL.getDefaultInstance(), player, Direction.UP))
                    .thenExecute(() -> helper.assertTrue(
                            helper.blocksBetween(0, 0, 0, 10, 1, 10)
                                    .filter(pos -> helper.getLevel().getBlockState(pos).is(Blocks.REDSTONE_BLOCK))
                                    .count() > 20,
                            "Not enough redstone blocks have been placed!"))
                    .thenExecute(() -> helper.assertTrue(
                            helper.blocksBetween(0, 0, 0, 10, 1, 10)
                                    .noneMatch(pos -> helper.getLevel().getBlockState(pos).is(Blocks.PODZOL)),
                            "Podzol was still placed!"))
                    .thenSucceed();
        });
    }

    /**
     * Tests {@link VanillaGameEvent} by listening for and printing out any uses of shears in the overworld.
     */
    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests the vanilla game event by hurting entities that are sheared in the overworld")
    static void vanillaGameEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final VanillaGameEvent event) -> {
            if (event.getVanillaEvent() == GameEvent.SHEAR && event.getLevel().dimension() == Level.OVERWORLD) {
                final var entities = event.getLevel().getEntitiesOfClass(Entity.class, new AABB(BlockPos.containing(event.getEventPosition())), e -> e instanceof Shearable);
                entities.get(0).hurt(event.getLevel().damageSources().generic(), event.getCause() == null ? 1 : 3);
                test.pass();
            }
        });

        test.onGameTest(helper -> {
            final Sheep sheep = helper.spawnWithNoFreeWill(EntityType.SHEEP, new BlockPos(1, 2, 1));
            sheep.setColor(DyeColor.BLACK);
            sheep.setSheared(false);

            // Prepare a dispenser to shear the sheep in the second phase
            helper.setBlock(1, 1, 1, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP));
            helper.requireBlockEntity(1, 1, 1, DispenserBlockEntity.class).setItem(0, Items.SHEARS.getDefaultInstance());

            helper.startSequence()
                    .thenIdle(5)
                    .thenExecute(() -> Items.SHEARS.getDefaultInstance().interactLivingEntity(
                            helper.makeMockPlayer(), sheep, InteractionHand.MAIN_HAND)) // Make a player shear the sheep
                    .thenExecute(() -> helper.assertItemEntityPresent(Items.BLACK_WOOL, new BlockPos(1, 2, 1), 2)) // Make sure wool was dropped
                    .thenExecute(() -> helper.assertEntityProperty(sheep, Sheep::getHealth, "health", 8f - 3f)) // player did it, so hurt by 3

                    .thenExecuteAfter(5, () -> {
                        // Prepare the sheep; reset its color and its state
                        sheep.setColor(DyeColor.BLUE);
                        sheep.setSheared(false);
                    })
                    .thenIdle(5)

                    // Power the dispenser
                    .thenExecute(() -> helper.setBlock(2, 1, 1, Blocks.REDSTONE_BLOCK))
                    .thenIdle(1)
                    .thenExecute(() -> helper.assertItemEntityPresent(Items.BLUE_WOOL, new BlockPos(1, 2, 1), 2)) // Make sure wool was dropped
                    .thenExecute(() -> helper.assertEntityProperty(sheep, Sheep::getHealth, "health", (8f - 3f) - 1f)) // dispenser did it, so hurt by 1

                    .thenIdle(5)
                    .thenExecute(() -> helper.killAllEntitiesOfClass(Sheep.class))
                    .thenExecute(() -> helper.killAllEntitiesOfClass(ItemEntity.class))

                    .thenSucceed();
        });
    }
}
