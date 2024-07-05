/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.event.level.block.CreateFluidSourceEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;

@ForEachTest(groups = { FluidTests.GROUP + ".event", "event" })
public class FluidEventTests {
    @GameTest
    @TestHolder(description = "Tests if the CreateFluidSourceEvent is fired and allows modifying the result")
    static void createFluidSourceEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final CreateFluidSourceEvent event) -> {
            final BlockState state = event.getState();
            final FluidState fluidState = state.getFluidState();
            if (fluidState.getType().isSame(Fluids.WATER)) {
                event.setCanConvert(false);
                // Place andesite on top of the sources
                event.getLevel().setBlock(event.getPos().above(), Blocks.ANDESITE.defaultBlockState(), 3);
            } else if (fluidState.getType().isSame(Fluids.LAVA)) {
                event.setCanConvert(true);
            }
        });

        test.registerGameTestTemplate(StructureTemplateBuilder.withSize(5, 5, 5)
                // Layer 1 for water
                .fill(0, 0, 0, 4, 0, 4, Blocks.IRON_BLOCK)
                .set(2, 1, 0, Blocks.IRON_BLOCK.defaultBlockState())
                .set(2, 1, 4, Blocks.IRON_BLOCK.defaultBlockState())

                .fill(1, 1, 1, 1, 1, 3, Blocks.IRON_BLOCK)
                .fill(3, 1, 1, 3, 1, 3, Blocks.IRON_BLOCK)

                // Layer 2 for lava
                .fill(0, 3, 0, 4, 3, 4, Blocks.GOLD_BLOCK)
                .set(2, 4, 0, Blocks.GOLD_BLOCK.defaultBlockState())
                .set(2, 4, 4, Blocks.GOLD_BLOCK.defaultBlockState())

                .fill(1, 4, 1, 1, 4, 3, Blocks.GOLD_BLOCK)
                .fill(3, 4, 1, 3, 4, 3, Blocks.GOLD_BLOCK));

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> {
                    helper.setBlock(2, 2, 1, Blocks.WATER);
                    helper.setBlock(2, 2, 3, Blocks.WATER);
                })
                .thenWaitUntil(() -> helper.assertBlockPresent(Blocks.WATER, 2, 2, 2)) // Wait until the water spread
                .thenExecute(() -> helper.assertBlockPresent(Blocks.ANDESITE, 2, 3, 1))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.ANDESITE, 2, 3, 3))
                .thenExecute(() -> helper.assertBlockState(new BlockPos(2, 2, 2), state -> state.getFluidState().is(Fluids.FLOWING_WATER) && !state.getFluidState().isSource(), () -> "Water block was a source!"))

                .thenIdle(5)

                .thenExecute(() -> {
                    helper.setBlock(2, 5, 1, Blocks.LAVA);
                    helper.setBlock(2, 5, 3, Blocks.LAVA);
                })
                .thenWaitUntil(() -> helper.assertBlockPresent(Blocks.LAVA, 2, 5, 2)) // Wait until the lava spread
                .thenExecute(() -> helper.assertBlockState(new BlockPos(2, 5, 2),
                        state -> state.getFluidState().isSource() && state.getFluidState().is(Fluids.LAVA), () -> "Lava source wasn't created"))
                .thenSucceed());
    }
}
