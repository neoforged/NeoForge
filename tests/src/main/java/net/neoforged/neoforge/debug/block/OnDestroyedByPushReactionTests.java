package net.neoforged.neoforge.debug.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;
import net.neoforged.testframework.registration.DeferredBlockBuilder;
import net.neoforged.testframework.registration.RegistrationHelper;

import java.util.concurrent.atomic.AtomicBoolean;

@ForEachTest(groups = BlockTests.GROUP + ".on_destroyed_by_push_reaction")
public class OnDestroyedByPushReactionTests {
    private static DeferredBlockBuilder<? extends Block> registerDestroyOnPistonMoveWithCallback(final RegistrationHelper reg, Runnable callback) {
        return reg.blocks()
                .registerBlock(
                        "destroy_on_piston_move",
                        properties -> new DestroyedByPushReactionListeningBlock(properties, callback),
                        BlockBehaviour.Properties.of()
                                .pushReaction(PushReaction.DESTROY))
                .withDefaultWhiteModel()
                .withBlockItem()
                .withLang("Destroy on piston move");
    }

    private static DeferredBlockBuilder<? extends Block> registerPushOnPistonMoveWithCallback(final RegistrationHelper reg, Runnable callback) {
        return reg.blocks()
                .registerBlock(
                        "push_on_piston_move",
                        properties -> new DestroyedByPushReactionListeningBlock(properties, callback),
                        BlockBehaviour.Properties.of()
                                .pushReaction(PushReaction.PUSH_ONLY))
                .withDefaultWhiteModel()
                .withBlockItem()
                .withLang("Push on piston move");
    }

    @GameTest
    @TestHolder(description = {
            "Tests the Neo-added onDestroyedByPushReaction method.",
            "Tests if the method is called when a block is destroyed by an extending piston head.",
            "This test adds a block that is destroyed when pushed by a piston."
    })
    public static void extendingPistonHead(final DynamicTest test, final RegistrationHelper reg) {
        AtomicBoolean blockMethodWasInvoked = new AtomicBoolean(false);
        final var destroyOnPistonMove = registerDestroyOnPistonMoveWithCallback(reg, () -> blockMethodWasInvoked.set(true));

        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(2, 4, 2)
                .placeFloorLever(1, 1, 1, false)

                .set(1, 0, 0, Blocks.PISTON.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.UP))
                .set(1, 1, 0, destroyOnPistonMove.get().defaultBlockState()));

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.assertFalse(
                        blockMethodWasInvoked.get(),
                        "onDestroyedByPushReaction was invoked before test sequence began. Was the AtomicBoolean correctly initialised?"))
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.PISTON, 1, 1, 0)) // The piston should exist
                .thenWaitUntil(0, () -> helper.assertBlockPresent(destroyOnPistonMove.get(), 1, 2, 0)) // Destroy block should exist

                .thenExecute(() -> helper.pullLever(1, 2, 1))
                .thenIdle(10)

                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.PISTON, 1, 1, 0)) // The piston should still exist
                .thenWaitUntil(0, () -> helper.assertBlockProperty(new BlockPos(1, 1, 0), PistonBaseBlock.EXTENDED, true)) // The piston should've extended
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.PISTON_HEAD, 1, 2, 0)) // The piston's head should've extended
                .thenWaitUntil(0, () -> helper.assertBlockNotPresent(destroyOnPistonMove.get(), 1, 3, 0)) // Destroy block should not have been pushed

                .thenExecute(() -> helper.assertTrue(
                        blockMethodWasInvoked.get(),
                        "onDestroyedByPushReaction was not invoked although the piston extended."
                ))

                .thenSucceed());
    }

    @GameTest
    @TestHolder(description = {
            "Tests the Neo-added onDestroyedByPushReaction method.",
            "Tests if the method is called when a block is destroyed by an extending connected block.",
            "This test adds a block that is destroyed when pushed by a piston."
    })
    public static void extendingConnectedBlock(final DynamicTest test, final RegistrationHelper reg) {
        AtomicBoolean blockMethodWasInvoked = new AtomicBoolean(false);
        final var destroyOnPistonMove = registerDestroyOnPistonMoveWithCallback(reg, () -> blockMethodWasInvoked.set(true));

        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(2, 5, 2)
                .placeFloorLever(1, 1, 1, false)

                .set(1, 0, 0, Blocks.PISTON.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.UP))
                .set(1, 1, 0, Blocks.SLIME_BLOCK.defaultBlockState())
                .set(0, 1, 0, Blocks.COBBLESTONE.defaultBlockState())
                .set(0, 2, 0, destroyOnPistonMove.get().defaultBlockState())
        );

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.assertFalse(
                        blockMethodWasInvoked.get(),
                        "onDestroyedByPushReaction was invoked before test sequence began. Was the AtomicBoolean correctly initialised?"))
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.PISTON, 1, 1, 0)) // The piston should exist
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.SLIME_BLOCK, 1, 2, 0)) // Slime block should exist
                .thenWaitUntil(0, () -> helper.assertBlockState(new BlockPos(0, 2, 0), state -> state.canStickTo(Blocks.SLIME_BLOCK.defaultBlockState()), () -> "Block should exist & be able to stick to slime block"))
                .thenWaitUntil(0, () -> helper.assertBlockPresent(destroyOnPistonMove.get(), 0, 3, 0)) // Destroy block should exist

                .thenExecute(() -> helper.pullLever(1, 2, 1))
                .thenIdle(10)

                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.PISTON, 1, 1, 0)) // The piston should still exist
                .thenWaitUntil(0, () -> helper.assertBlockProperty(new BlockPos(1, 1, 0), PistonBaseBlock.EXTENDED, true)) // The piston should've extended
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.PISTON_HEAD, 1, 2, 0)) // The piston's head should've extended
                .thenWaitUntil(0, () -> helper.assertBlockNotPresent(destroyOnPistonMove.get(), 0, 4, 0)) // Destroy block should not have been pushed

                .thenExecute(() -> helper.assertTrue(
                        blockMethodWasInvoked.get(),
                        "onDestroyedByPushReaction was not invoked although the piston extended."
                ))

                .thenSucceed());
    }

    @GameTest
    @TestHolder(description = {
            "Tests the Neo-added onDestroyedByPushReaction method.",
            "Tests if the method is called when a block is destroyed by an retracting connected block.",
            "This test adds a block that is destroyed when pushed by a piston."
    })
    public static void retractingConnectedBlock(final DynamicTest test, final RegistrationHelper reg) {
        AtomicBoolean blockMethodWasInvoked = new AtomicBoolean(false);
        final var destroyOnPistonMove = registerDestroyOnPistonMoveWithCallback(reg, () -> blockMethodWasInvoked.set(true));

        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(2, 5, 2)
                .placeFloorLever(1, 1, 1, true)

                .set(1, 0, 0, Blocks.STICKY_PISTON.defaultBlockState()
                        .setValue(DirectionalBlock.FACING, Direction.UP)
                        .setValue(PistonBaseBlock.EXTENDED, true))
                .set(1, 1, 0, Blocks.PISTON_HEAD.defaultBlockState()
                        .setValue(DirectionalBlock.FACING, Direction.UP)
                        .setValue(BlockStateProperties.PISTON_TYPE, PistonType.STICKY))
                .set(1, 2, 0, Blocks.SLIME_BLOCK.defaultBlockState())
                .set(0, 2, 0, Blocks.COBBLESTONE.defaultBlockState())
                .set(0, 1, 0, destroyOnPistonMove.get().defaultBlockState())
        );

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.assertFalse(
                        blockMethodWasInvoked.get(),
                        "onDestroyedByPushReaction was invoked before test sequence began. Was the AtomicBoolean correctly initialised?"))
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.STICKY_PISTON, 1, 1, 0)) // The piston should exist
                .thenWaitUntil(0, () -> helper.assertBlockProperty(new BlockPos(1, 1, 0), PistonBaseBlock.EXTENDED, true)) // The piston should be extended
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.PISTON_HEAD, 1, 2, 0)) // The piston's head should be extended
                .thenWaitUntil(0, () -> helper.assertBlockProperty(new BlockPos(1, 2, 0), BlockStateProperties.PISTON_TYPE, PistonType.STICKY)) // The piston's head should be sticky
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.SLIME_BLOCK, 1, 3, 0)) // Slime block should exist
                .thenWaitUntil(0, () -> helper.assertBlockState(new BlockPos(0, 3, 0), state -> state.canStickTo(Blocks.SLIME_BLOCK.defaultBlockState()), () -> "Block should exist & be able to stick to slime block"))
                .thenWaitUntil(0, () -> helper.assertBlockPresent(destroyOnPistonMove.get(), 0, 2, 0)) // Destroy block should exist

                .thenExecute(() -> helper.pullLever(1, 2, 1))
                .thenIdle(10)

                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.STICKY_PISTON, 1, 1, 0)) // The piston should still exist
                .thenWaitUntil(0, () -> helper.assertBlockProperty(new BlockPos(1, 1, 0), PistonBaseBlock.EXTENDED, false)) // The piston should've retracted
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.SLIME_BLOCK, 1, 2, 0)) // The slime block should have been retracted
                .thenWaitUntil(0, () -> helper.assertBlockNotPresent(destroyOnPistonMove.get(), 0, 1, 0)) // Destroy block should not have been pushed

                .thenExecute(() -> helper.assertTrue(
                        blockMethodWasInvoked.get(),
                        "onDestroyedByPushReaction was not invoked although the piston retracted."
                ))

                .thenSucceed());
    }

    @GameTest
    @TestHolder(description = {
            "Tests the Neo-added onDestroyedByPushReaction method.",
            "Asserts that the method is not called when a block is pushed by a piston and left intact.",
            "This test adds a block that is pushed when pushed by a piston."
    })
    public static void pushOnlyReaction(final DynamicTest test, final RegistrationHelper reg) {
        AtomicBoolean blockMethodWasInvoked = new AtomicBoolean(false);
        final var pushOnPistonMove = registerPushOnPistonMoveWithCallback(reg, () -> blockMethodWasInvoked.set(true));

        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(2, 4, 2)
                .placeFloorLever(1, 1, 1, false)

                .set(1, 0, 0, Blocks.PISTON.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.UP))
                .set(1, 1, 0, pushOnPistonMove.get().defaultBlockState()));

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.assertFalse(
                        blockMethodWasInvoked.get(),
                        "onDestroyedByPushReaction was invoked before test sequence began. Was the AtomicBoolean correctly initialised?"))
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.PISTON, 1, 1, 0)) // The piston should exist
                .thenWaitUntil(0, () -> helper.assertBlockPresent(pushOnPistonMove.get(), 1, 2, 0)) // Push block should exist

                .thenExecute(() -> helper.pullLever(1, 2, 1))
                .thenIdle(10)

                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.PISTON, 1, 1, 0)) // The piston should still exist
                .thenWaitUntil(0, () -> helper.assertBlockProperty(new BlockPos(1, 1, 0), PistonBaseBlock.EXTENDED, true)) // The piston should've extended
                .thenWaitUntil(0, () -> helper.assertBlockPresent(Blocks.PISTON_HEAD, 1, 2, 0)) // The piston's head should've extended
                .thenWaitUntil(0, () -> helper.assertBlockPresent(pushOnPistonMove.get(), 1, 3, 0)) // Push block should have been pushed

                .thenExecute(() -> helper.assertFalse(
                        blockMethodWasInvoked.get(),
                        "onDestroyedByPushReaction was invoked although the block was not destroyed."
                ))

                .thenSucceed());
    }

    @GameTest
    @TestHolder(description = {
            "Tests the Neo-added onDestroyedByPushReaction method.",
            "Asserts that the method is not called when a block is destroyed.",
            "This test adds a block that is destroyed when pushed by a piston."
    })
    public static void playerDestroy(final DynamicTest test, final RegistrationHelper reg) {
        AtomicBoolean blockMethodWasInvoked = new AtomicBoolean(false);
        final var destroyOnPistonMove = registerDestroyOnPistonMoveWithCallback(reg, () -> blockMethodWasInvoked.set(true));

        test.registerGameTestTemplate(() -> StructureTemplateBuilder.withSize(1, 1, 1)
                .set(0, 0, 0, destroyOnPistonMove.get().defaultBlockState()));

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.assertFalse(
                        blockMethodWasInvoked.get(),
                        "onDestroyedByPushReaction was invoked before test sequence began. Was the AtomicBoolean correctly initialised?"))
                .thenWaitUntil(0, () -> helper.assertBlockPresent(destroyOnPistonMove.get(), 0, 1, 0)) // Destroy block should exist

                .thenExecute(() -> helper.destroyBlock(new BlockPos(0, 1, 0)))
                .thenIdle(10)

                .thenWaitUntil(0, () -> helper.assertBlockNotPresent(destroyOnPistonMove.get(), 0, 1, 0)) // Push block should have been destroyed

                .thenExecute(() -> helper.assertFalse(
                        blockMethodWasInvoked.get(),
                        "onDestroyedByPushReaction was invoked although the block was not destroyed by a piston."
                ))

                .thenSucceed());
    }

    private static class DestroyedByPushReactionListeningBlock extends Block {
        private final Runnable onDestroyedByPushReactionCallback;

        public DestroyedByPushReactionListeningBlock(Properties p_49795_, Runnable onDestroyedByPushReactionCallback) {
            super(p_49795_);
            this.onDestroyedByPushReactionCallback = onDestroyedByPushReactionCallback;
        }

        @Override
        public void onDestroyedByPushReaction(BlockState state, Level level, BlockPos pos, Direction pushDirection, FluidState fluid) {
            super.onDestroyedByPushReaction(state, level, pos, pushDirection, fluid);
            this.onDestroyedByPushReactionCallback.run();
        }
    }
}
