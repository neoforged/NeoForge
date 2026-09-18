/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.fluid;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.jspecify.annotations.Nullable;

@ForEachTest(groups = FluidTests.GROUP)
public class FluidTests {
    public static final String GROUP = "level.fluid";

    @GameTest
    @EmptyTemplate("3x5x3")
    @TestHolder(description = "Tests that a lava bucket fills a lava-loggable block without replacing it or placing lava beside it")
    static void lavaBucketFluidlogging(final DynamicTest test, final RegistrationHelper reg) {
        final var container = reg.blocks().registerBlock("lava_loggable_block", LavaLoggableBlock::new);

        test.onGameTest(TestHelper.class, helper -> {
            final var pos = new BlockPos(1, 1, 1);
            helper.setBlock(pos, container.get());

            final var player = helper.makeMockPlayer(GameType.SURVIVAL);
            helper.assertBucketResult(helper.useBucket(pos, player, Items.LAVA_BUCKET), Items.BUCKET);
            helper.assertBlockState(pos, container.get().defaultBlockState().setValue(LavaLoggableBlock.LAVA_LOGGED, true));
            helper.assertValueEqual(Fluids.LAVA, helper.getBlockState(pos).getFluidState().getType(), "contained fluid");
            helper.assertBlockPresent(Blocks.AIR, pos.above());
            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate("3x5x3")
    @TestHolder(description = "Tests that buckets can waterlog a slab and pick the water back up without removing the slab")
    static void waterBucketFluidlogging(final TestHelper helper) {
        final var pos = new BlockPos(1, 1, 1);
        final var drySlab = Blocks.STONE_SLAB.defaultBlockState();
        helper.setBlock(pos, drySlab);
        final var player = helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL);

        helper.assertBucketResult(helper.useBucket(pos, player, Items.WATER_BUCKET), Items.BUCKET);
        helper.assertBlockState(pos, drySlab.setValue(BlockStateProperties.WATERLOGGED, true));
        helper.assertValueEqual(Fluids.WATER, helper.getBlockState(pos).getFluidState().getType(), "contained fluid");
        helper.assertBlockPresent(Blocks.AIR, pos.above());

        helper.assertBucketResult(helper.useBucket(pos, player, Items.BUCKET), Items.WATER_BUCKET);
        helper.assertBlockState(pos, drySlab);
        helper.assertTrue(helper.getBlockState(pos).getFluidState().isEmpty(), "The slab should no longer contain water");
        helper.assertBlockPresent(Blocks.AIR, pos.above());
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate("3x5x3")
    @TestHolder(description = "Tests that a lava bucket places lava beside a waterloggable slab without replacing the slab")
    static void rejectedLavaBucketFluidlogging(final TestHelper helper) {
        final var pos = new BlockPos(1, 1, 1);
        final var slab = Blocks.STONE_SLAB.defaultBlockState();
        helper.setBlock(pos, slab);

        helper.assertBucketResult(helper.useBucket(pos, helper.makeMockPlayer(GameType.SURVIVAL), Items.LAVA_BUCKET), Items.BUCKET);
        helper.assertBlockState(pos, slab);
        helper.assertBlockState(pos.above(), Blocks.LAVA.defaultBlockState());
        helper.succeed();
    }

    @GameTest
    @EmptyTemplate("3x5x3")
    @TestHolder(description = "Tests that a water bucket places water beside a container that only accepts lava")
    static void rejectedWaterBucketFluidlogging(final DynamicTest test, final RegistrationHelper reg) {
        final var container = reg.blocks().registerBlock("lava_loggable_block", LavaLoggableBlock::new);

        test.onGameTest(TestHelper.class, helper -> {
            final var pos = new BlockPos(1, 1, 1);
            helper.setBlock(pos, container.get());

            helper.assertBucketResult(helper.useBucket(pos, helper.makeMockPlayer(GameType.SURVIVAL), Items.WATER_BUCKET), Items.BUCKET);
            helper.assertBlockState(pos, container.get().defaultBlockState());
            helper.assertBlockState(pos.above(), Blocks.WATER.defaultBlockState());
            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate("3x5x3")
    @TestHolder(description = "Tests that an already lava-logged container is preserved and additional lava is placed beside it")
    static void filledLavaBucketFluidlogging(final DynamicTest test, final RegistrationHelper reg) {
        final var container = reg.blocks().registerBlock("lava_loggable_block", LavaLoggableBlock::new);

        test.onGameTest(TestHelper.class, helper -> {
            final var pos = new BlockPos(1, 1, 1);
            final var filled = container.get().defaultBlockState().setValue(LavaLoggableBlock.LAVA_LOGGED, true);
            helper.setBlock(pos, filled);

            helper.assertBucketResult(helper.useBucket(pos, helper.makeMockPlayer(GameType.SURVIVAL), Items.LAVA_BUCKET), Items.BUCKET);
            helper.assertBlockState(pos, filled);
            helper.assertBlockState(pos.above(), Blocks.LAVA.defaultBlockState());
            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate("3x5x3")
    @TestHolder(description = "Tests that sneaking bypasses fluidlogging and places lava beside the accepting container")
    static void sneakingLavaBucketFluidlogging(final DynamicTest test, final RegistrationHelper reg) {
        final var container = reg.blocks().registerBlock("lava_loggable_block", LavaLoggableBlock::new);

        test.onGameTest(TestHelper.class, helper -> {
            final var pos = new BlockPos(1, 1, 1);
            helper.setBlock(pos, container.get());
            final var player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setShiftKeyDown(true);

            helper.assertBucketResult(helper.useBucket(pos, player, Items.LAVA_BUCKET), Items.BUCKET);
            helper.assertBlockState(pos, container.get().defaultBlockState());
            helper.assertBlockState(pos.above(), Blocks.LAVA.defaultBlockState());
            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate("3x5x3")
    @TestHolder(description = "Tests that a custom bucket can opt out of fluidlogging by overriding canBlockContainFluid")
    static void customBucketFluidlogging(final DynamicTest test, final RegistrationHelper reg) {
        final var container = reg.blocks().registerBlock("lava_loggable_block", LavaLoggableBlock::new);
        final var bucket = reg.items().registerItem("no_fluidlogging_bucket", props -> new BucketItem(Fluids.LAVA, props.stacksTo(1)) {
            @Override
            protected boolean canBlockContainFluid(@Nullable Player player, Level level, BlockPos pos, BlockState clicked) {
                return false;
            }
        });

        test.onGameTest(TestHelper.class, helper -> {
            final var pos = new BlockPos(1, 1, 1);
            helper.setBlock(pos, container.get());

            helper.assertBucketResult(helper.useBucket(pos, helper.makeMockPlayer(GameType.SURVIVAL), bucket.get()), Items.BUCKET);
            helper.assertBlockState(pos, container.get().defaultBlockState());
            helper.assertBlockState(pos.above(), Blocks.LAVA.defaultBlockState());
            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate("3x5x3")
    @TestHolder(description = "Tests that a dispenser can lava-log a container without a player or block hit result")
    static void dispenserLavaFluidlogging(final DynamicTest test, final RegistrationHelper reg) {
        final var container = reg.blocks().registerBlock("lava_loggable_block", LavaLoggableBlock::new);

        test.onGameTest(TestHelper.class, helper -> {
            final var dispenserPos = new BlockPos(1, 1, 1);
            final var containerPos = dispenserPos.above();
            helper.setBlock(containerPos, container.get());
            helper.setBlock(dispenserPos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP));
            helper.getBlockEntity(dispenserPos, DispenserBlockEntity.class).setItem(0, Items.LAVA_BUCKET.getDefaultInstance());

            helper.startSequence()
                    .thenExecute(() -> helper.pulseRedstone(dispenserPos.west(), 2))
                    .thenExecuteAfter(5, () -> {
                        helper.assertBlockState(containerPos, container.get().defaultBlockState().setValue(LavaLoggableBlock.LAVA_LOGGED, true));
                        helper.assertContainerContainsSingle(dispenserPos, Items.BUCKET);
                        helper.assertItemEntityNotPresent(Items.LAVA_BUCKET);
                        helper.assertBlockPresent(Blocks.AIR, containerPos.above());
                    })
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate("3x5x3")
    @TestHolder(description = "Tests that a dispenser ejects a full water bucket when the target container only accepts lava")
    static void rejectedDispenserFluidlogging(final DynamicTest test, final RegistrationHelper reg) {
        final var container = reg.blocks().registerBlock("lava_loggable_block", LavaLoggableBlock::new);

        test.onGameTest(TestHelper.class, helper -> {
            final var dispenserPos = new BlockPos(1, 1, 1);
            final var containerPos = dispenserPos.above();
            helper.setBlock(containerPos, container.get());
            helper.setBlock(dispenserPos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP));
            helper.getBlockEntity(dispenserPos, DispenserBlockEntity.class).setItem(0, Items.WATER_BUCKET.getDefaultInstance());

            helper.startSequence()
                    .thenExecute(() -> helper.pulseRedstone(dispenserPos.west(), 2))
                    .thenExecuteAfter(5, () -> {
                        helper.assertBlockState(containerPos, container.get().defaultBlockState());
                        helper.assertContainerEmpty(dispenserPos);
                        helper.assertItemEntityCountIs(Items.WATER_BUCKET, containerPos, 2, 1);
                        helper.assertBlockPresent(Blocks.AIR, containerPos.above());
                    })
                    .thenSucceed();
        });
    }

    private static class TestHelper extends ExtendedGameTestHelper {
        public TestHelper(GameTestInfo info) {
            super(info);
        }

        InteractionResult useBucket(BlockPos pos, Player player, Item bucket) {
            player.snapTo(Vec3.atBottomCenterOf(this.absolutePos(pos.above())));
            player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(this.absolutePos(pos)));
            player.setItemInHand(InteractionHand.MAIN_HAND, bucket.getDefaultInstance());
            // Use the bucket normally so both target selection and emptyContents are exercised.
            return bucket.use(this.getLevel(), player, InteractionHand.MAIN_HAND);
        }

        void assertBucketResult(InteractionResult result, Item expectedItem) {
            this.assertTrue(result instanceof InteractionResult.Success, "Using the bucket should succeed");
            final var remainder = ((InteractionResult.Success) result).heldItemTransformedTo();
            this.assertTrue(remainder != null && remainder.is(expectedItem), "The bucket should become " + expectedItem);
        }
    }

    private static class LavaLoggableBlock extends Block implements LiquidBlockContainer {
        private static final BooleanProperty LAVA_LOGGED = BooleanProperty.create("lava_logged");

        private LavaLoggableBlock(Properties properties) {
            super(properties);
            registerDefaultState(defaultBlockState().setValue(LAVA_LOGGED, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(LAVA_LOGGED);
        }

        @Override
        public boolean canPlaceLiquid(@Nullable LivingEntity user, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
            return !state.getValue(LAVA_LOGGED) && fluid == Fluids.LAVA;
        }

        @Override
        public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
            if (!canPlaceLiquid(null, level, pos, state, fluidState.getType())) {
                return false;
            }
            return level.setBlock(pos, state.setValue(LAVA_LOGGED, true), Block.UPDATE_ALL);
        }

        @Override
        protected FluidState getFluidState(BlockState state) {
            return state.getValue(LAVA_LOGGED) ? Fluids.LAVA.defaultFluidState() : Fluids.EMPTY.defaultFluidState();
        }
    }
}
