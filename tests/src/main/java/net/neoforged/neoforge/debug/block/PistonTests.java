/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = BlockTests.GROUP + ".piston")
public class PistonTests {
    @GameTest
    @TestHolder(description = {
            "This test blocks pistons from moving cobblestone at all except indirectly.",
            "This test adds a block that moves upwards when pushed by a piston.",
            "This test mod makes black wool pushed by a piston drop after being pushed."
    })
    static void pistonEvent(final DynamicTest test, final RegistrationHelper reg) {
        final var shiftOnPistonMove = reg.blocks().registerSimpleBlock("shift_on_piston_move", BlockBehaviour.Properties.of())
                .withDefaultWhiteModel()
                .withBlockItem()
                .withLang("Shift on piston move");

        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(3, 5, 3)
                .placeFloorLever(1, 1, 1, false)
                .set(1, 0, 2, Blocks.PISTON.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.UP))
                .set(1, 1, 2, Blocks.BLACK_WOOL.defaultBlockState())
                .set(1, 2, 2, shiftOnPistonMove.get().defaultBlockState())

                .set(2, 0, 1, Blocks.STICKY_PISTON.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.UP))
                .set(2, 2, 1, Blocks.COBBLESTONE.defaultBlockState())

                .set(1, 0, 0, Blocks.PISTON.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.UP))
                .set(1, 1, 0, Blocks.COBBLESTONE.defaultBlockState()));

        test.eventListeners().forge().addListener((final PistonEvent.Pre event) -> {
            if (!(event.getLevel() instanceof Level level)) return;

            if (event.getPistonMoveType() == PistonEvent.PistonMoveType.EXTEND) {
                final PistonStructureResolver pistonHelper = Objects.requireNonNull(event.getStructureHelper());

                if (pistonHelper.resolve()) {
                    for (BlockPos newPos : pistonHelper.getToPush()) {
                        final BlockState state = event.getLevel().getBlockState(newPos);
                        if (state.getBlock() == Blocks.BLACK_WOOL) {
                            Block.dropResources(state, level, newPos);
                            level.setBlockAndUpdate(newPos, Blocks.AIR.defaultBlockState());
                        }
                    }
                }

                // Make the block move up and out of the way so long as it won't replace the piston
                final BlockPos pushedBlockPos = event.getFaceOffsetPos().relative(event.getDirection());
                if (level.getBlockState(pushedBlockPos).is(shiftOnPistonMove.get()) && event.getDirection() != Direction.DOWN) {
                    level.setBlockAndUpdate(pushedBlockPos, Blocks.AIR.defaultBlockState());
                    level.setBlockAndUpdate(pushedBlockPos.above(), shiftOnPistonMove.get().defaultBlockState());
                }

                // Block pushing cobblestone (directly, indirectly works)
                event.setCanceled(event.getLevel().getBlockState(event.getFaceOffsetPos()).getBlock() == Blocks.COBBLESTONE);
            } else {
                final boolean isSticky = event.getLevel().getBlockState(event.getPos()).getBlock() == Blocks.STICKY_PISTON;

                // Offset twice to see if retraction will pull cobblestone
                event.setCanceled(event.getLevel().getBlockState(event.getFaceOffsetPos().relative(event.getDirection())).getBlock() == Blocks.COBBLESTONE && isSticky);
            }
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.pullLever(1, 2, 1))
                .thenIdle(10)

                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.PISTON_HEAD, 1, 2, 2)) // The piston should've extended
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.AIR, 1, 3, 2)) // This is where the shift block WOULD be
                .thenWaitUntil(0, () -> helper.assertBlockPresent(shiftOnPistonMove.get(), 1, 4, 2)) // Shift block should move upwards

                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.COBBLESTONE, 1, 2, 0))

                .thenIdle(20)
                .thenExecute(() -> helper.pullLever(1, 2, 1))
                .thenIdle(10)
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.COBBLESTONE, 2, 3, 1))
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.PISTON_HEAD, 2, 2, 1))

                .thenExecute(test::pass)
                .thenSucceed());
    }

    @GameTest
    @TestHolder(description = {
            "Tests if blue block is sticky, and red block is considered slime.",
            "Tests if blue block does not stick to red block.",
            "This test is GameTest-only!"
    })
    static void stickyBlocks(final DynamicTest test, final RegistrationHelper reg) {
        final var blueBlock = reg.blocks().register("blue_block", () -> new Block(Block.Properties.of()) {
            @Override
            public boolean isStickyBlock(BlockState state) {
                return true;
            }
        }).withBlockItem().withLang("Blue block").withDefaultWhiteModel().withColor(0x0000ff);

        final var redBlock = reg.blocks().register("red_block", () -> new Block(Block.Properties.of()) {
            @Override
            public boolean isStickyBlock(BlockState state) {
                return true;
            }

            @Override
            public boolean isSlimeBlock(BlockState state) {
                return true;
            }

            @Override
            public boolean canStickTo(BlockState state, BlockState other) {
                if (other.getBlock() == Blocks.SLIME_BLOCK) return false;
                return state.isStickyBlock() || other.isStickyBlock();
            }
        }).withBlockItem().withLang("Red block").withDefaultWhiteModel().withColor(0xff0000);

        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(2, 5, 1)
                .placeFloorLever(1, 1, 0, false)
                .set(0, 0, 0, Blocks.STICKY_PISTON.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.UP))
                .set(0, 1, 0, blueBlock.get().defaultBlockState())
                .set(0, 3, 0, redBlock.get().defaultBlockState())
                .set(0, 4, 0, Blocks.SLIME_BLOCK.defaultBlockState()));

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.pullLever(1, 2, 0))
                .thenIdle(5)
                .thenWaitUntil(0, () -> helper.assertBlockPresent(blueBlock.get(), 0, 3, 0))
                .thenExecute(() -> helper.pullLever(1, 2, 0))
                .thenIdle(5)
                .thenWaitUntil(0, () -> helper.assertBlockPresent(blueBlock.get(), 0, 2, 0))
                .thenWaitUntil(0, () -> helper.assertBlockPresent(redBlock.get(), 0, 3, 0))
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.SLIME_BLOCK, 0, 5, 0))
                .thenExecute(test::pass)
                .thenSucceed());
    }
}
