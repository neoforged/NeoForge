package net.neoforged.neoforge.debug.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.AlterGroundEvent;
import net.neoforged.neoforge.event.level.SaplingGrowTreeEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;

@ForEachTest(groups = {LevelTests.GROUP + ".event", "event"})
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
                            "Not enough redstone blocks have been placed!"
                    ))
                    .thenExecute(() -> helper.assertTrue(
                            helper.blocksBetween(0, 0, 0, 10, 1, 10)
                                    .noneMatch(pos -> helper.getLevel().getBlockState(pos).is(Blocks.PODZOL)),
                            "Podzol was still placed!"
                    ))
                    .thenSucceed();
        });
    }
}
