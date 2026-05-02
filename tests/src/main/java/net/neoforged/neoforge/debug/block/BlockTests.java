/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayer.RespawnConfig;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.enums.BubbleColumnDirection;
import net.neoforged.neoforge.common.world.poi.ExtendPoiTypesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = BlockTests.GROUP)
public class BlockTests {
    public static final String GROUP = "level.block";

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if player breaking decorated pots with swords drops Bricks")
    static void decoratedPotBreaking(final DynamicTest test) {
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))

                // Mine pot with sword
                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.DECORATED_POT.defaultBlockState()))
                .thenExecute(player -> player.setItemInHand(InteractionHand.MAIN_HAND, Items.DIAMOND_SWORD.getDefaultInstance()))
                .thenExecute(player -> player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(1, 1, 1))))
                .thenExecute(player -> helper.assertTrue(
                        helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().expandTowards(2, 2, 2)).stream().anyMatch(itemEntity -> itemEntity.getItem().is(Items.BRICK)),
                        "Decorated Pot should had dropped Bricks"))
                .thenExecute(player -> helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().expandTowards(2, 2, 2)).forEach(itemEntity -> itemEntity.remove(Entity.RemovalReason.DISCARDED)))

                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.DECORATED_POT.defaultBlockState()))
                .thenExecute(player -> player.setItemInHand(InteractionHand.MAIN_HAND, Items.DANDELION.getDefaultInstance()))
                .thenExecute(player -> player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(1, 1, 1))))
                .thenExecute(player -> helper.assertTrue(
                        helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().expandTowards(2, 2, 2)).stream().anyMatch(itemEntity -> itemEntity.getItem().is(Items.DECORATED_POT)),
                        "Decorated Pot should had dropped the Decorated Pot"))
                .thenExecute(player -> helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().expandTowards(2, 2, 2)).forEach(itemEntity -> itemEntity.remove(Entity.RemovalReason.DISCARDED)))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if custom fence gates without wood types work, allowing for the use of the vanilla block for non-wooden gates")
    static void woodlessFenceGate(final DynamicTest test, final RegistrationHelper reg) {
        final var gate = reg.blocks().registerBlock("gate", props -> new FenceGateBlock(props, SoundEvents.BARREL_OPEN, SoundEvents.CHEST_CLOSE), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_FENCE_GATE))
                .withLang("Woodless Fence Gate")
                .withBlockItem();

        reg.addClientProvider(event -> event.addProvider(new ModelProvider(event.getGenerator().getPackOutput(), reg.modId()) {
            @Override
            protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
                var family = new BlockFamily.Builder(Blocks.IRON_BLOCK).fenceGate(gate.value()).getFamily();
                blockModels.familyWithExistingFullBlock(family.getBaseBlock()).generateFor(family);
            }

            @Override
            protected Stream<? extends Holder<Item>> getKnownItems() {
                return Stream.of(DeferredItem.createItem(gate.getId()));
            }

            @Override
            protected Stream<? extends Holder<Block>> getKnownBlocks() {
                return Stream.of(gate);
            }

            @Override
            public String getName() {
                return "test_woodless_fence_gate_model_generator";
            }
        }));

        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(1, 1, 1, gate.get().defaultBlockState().setValue(FenceGateBlock.OPEN, true)))

                // Close gate as a player
                .thenExecute(player -> helper.useBlock(new BlockPos(1, 1, 1)))
                .thenExecute(player -> helper.assertTrue(
                        player.getOutboundPackets(ClientboundSoundPacket.class)
                                .anyMatch(sound -> sound.getSound().value() == SoundEvents.CHEST_CLOSE),
                        "Close sound was not broadcast"))

                // Open gate with redstone
                .thenExecute(player -> helper.pulseRedstone(1, 2, 1, 1))
                .thenExecute(player -> helper.assertTrue(
                        player.getOutboundPackets(ClientboundSoundPacket.class)
                                .anyMatch(sound -> sound.getSound().value() == SoundEvents.BARREL_OPEN),
                        "Open sound was not broadcast"))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true, value = "5x5x5") // barrier blocks may prevent respawn
    @TestHolder(description = "Tests if the Neo-added getRespawnPosition method correctly changes the position")
    static void customRespawnTest(final DynamicTest test, final RegistrationHelper reg) {
        final var respawn = reg.blocks().register("respawn", key -> new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, key))) {
            @Override
            protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
                if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.setRespawnPosition(new RespawnConfig(new LevelData.RespawnData(GlobalPos.of(world.dimension(), pos), 0, 0), false), true);
                }
                return InteractionResult.SUCCESS;
            }

            @Override
            public Optional<ServerPlayer.RespawnPosAngle> getRespawnPosition(BlockState state, EntityType<?> type, LevelReader levelReader, BlockPos pos, float orientation) {
                // have the player respawn a block north to the location of the anchor
                return Optional.of(ServerPlayer.RespawnPosAngle.of(pos.getCenter().add(0, 1, 1), pos, 0));
            }
        }).withBlockItem().withLang("Respawn").withDefaultWhiteModel();

        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(1, 2, 1, respawn.get()))
                .thenExecute(() -> helper.setBlock(1, 2, 2, Blocks.IRON_BLOCK))

                .thenExecute(player -> helper.useBlock(new BlockPos(1, 2, 1), player))
                .thenExecute(player -> player.level().getServer().getPlayerList().respawn(player, false, Entity.RemovalReason.CHANGED_DIMENSION))
                .thenExecute(() -> helper.assertEntityPresent(
                        EntityType.PLAYER,
                        1, 3, 2))
                .thenSucceed());
    }

    @GameTest()
    @TestHolder(description = "Adds a block that can sustain Bubble Columns and verify it works")
    static void bubbleColumnTest(final DynamicTest test, final RegistrationHelper reg) {
        final var upwardBubbleColumnSustainingBlock = reg.blocks()
                .registerBlock("upward_bubble_column_sustaining_block", (properties) -> new CustomBubbleColumnSustainingBlock(properties, BubbleColumnDirection.UPWARD))
                .withLang("Upward Bubble Column Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();
        final var downwardBubbleColumnSustainingBlock = reg.blocks()
                .registerBlock("downward_bubble_column_sustaining_block", (properties) -> new CustomBubbleColumnSustainingBlock(properties, BubbleColumnDirection.DOWNWARD))
                .withLang("Downward Bubble Column Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();

        test.registerGameTestTemplate(StructureTemplateBuilder.withSize(3, 3, 3)
                .fill(0, 0, 0, 2, 2, 2, Blocks.WATER));

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.setBlock(new BlockPos(0, 0, 1), upwardBubbleColumnSustainingBlock.get().defaultBlockState()))
                .thenExecute(() -> helper.setBlock(new BlockPos(1, 0, 1), downwardBubbleColumnSustainingBlock.get().defaultBlockState()))
                .thenExecute(() -> helper.setBlock(new BlockPos(2, 0, 1), Blocks.OAK_PLANKS.defaultBlockState()))
                .thenIdle(20)
                .thenExecute(() -> helper.assertTrue(helper.getBlockState(new BlockPos(0, 2, 1)).is(Blocks.BUBBLE_COLUMN), "Bubble Column presence was not found where it should be"))
                .thenExecute(() -> helper.assertTrue(helper.getBlockState(new BlockPos(1, 2, 1)).is(Blocks.BUBBLE_COLUMN), "Bubble Column presence was not found where it should be"))
                .thenExecute(() -> helper.assertFalse(helper.getBlockState(new BlockPos(2, 2, 1)).is(Blocks.BUBBLE_COLUMN), "Bubble Column presence was found where it shouldn't be"))
                .thenSucceed());
    }

    private static class CustomBubbleColumnSustainingBlock extends Block {
        private final BubbleColumnDirection bubbleColumnDirection;

        public CustomBubbleColumnSustainingBlock(Properties properties, BubbleColumnDirection bubbleColumnDirection1) {
            super(properties);
            this.bubbleColumnDirection = bubbleColumnDirection1;
        }

        @Override
        protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
            BubbleColumnBlock.updateColumn(Blocks.BUBBLE_COLUMN, serverLevel, blockPos.above(), blockState);
        }

        @Override
        protected BlockState updateShape(BlockState currentBlockState, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos currentBlockPos, Direction direction, BlockPos sideBlockPos, BlockState sideBlockState, RandomSource randomSource) {
            if (direction == Direction.UP && sideBlockState.is(Blocks.WATER)) {
                scheduledTickAccess.scheduleTick(currentBlockPos, this, 1);
            }
            return super.updateShape(currentBlockState, level, scheduledTickAccess, currentBlockPos, direction, sideBlockPos, sideBlockState, randomSource);
        }

        @Override
        protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState oldBlockState, boolean isMoving) {
            level.scheduleTick(blockPos, this, 1);
        }

        @Override
        public BubbleColumnDirection getBubbleColumnDirection(BlockState state) {
            return bubbleColumnDirection;
        }
    }

    @TestHolder(description = "Adds a block whose states are added to the PoiTypes.MEETING PoI type", enabledByDefault = true)
    static void extendPoiTypeTest(final DynamicTest test, final RegistrationHelper reg) {
        DeferredBlock<Block> block = reg.blocks().registerSimpleBlock("test_meeting_point");
        boolean[] failedEarly = new boolean[1];
        test.eventListeners().mod().addListener((ExtendPoiTypesEvent event) -> {
            try {
                event.addBlockToPoi(PoiTypes.MEETING, block.value());
            } catch (Exception e) {
                test.updateStatus(Test.Status.failed("PoiType extension failed with exception", e), null);
                failedEarly[0] = true;
            }
        });
        test.eventListeners().mod().addListener((FMLLoadCompleteEvent event) -> {
            if (failedEarly[0]) return;

            PoiType poiType = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getValueOrThrow(PoiTypes.MEETING);
            ImmutableList<BlockState> states = block.value().getStateDefinition().getPossibleStates();
            if (!poiType.matchingStates().containsAll(states)) {
                test.fail("Test block's states were not added to PoiType's matchingStates");
                return;
            }
            for (BlockState state : states) {
                Optional<Holder<PoiType>> type = PoiTypes.forState(state);
                if (type.isEmpty() || type.get().getKey() != PoiTypes.MEETING) {
                    test.fail("A state of the test block is missing from or assigned to the wrong PoI in PoiTypes.TYPE_BY_STATE");
                    return;
                }
            }
            test.pass();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if double blocks correctly implement the isRelocatable block extension")
    static void areDoubleBlocksRelocatable(final DynamicTest test, final RegistrationHelper reg) {
        test.onGameTest(helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos floorCenter = helper.absolutePos(new BlockPos(1, 0, 1)); // we're in a 3x3x3 cube by default
            BlockPos lowerPos = floorCenter.above();
            BlockPos abovePos = lowerPos.above();
            // set some dirt so we can place a plant on it
            level.setBlock(floorCenter, Blocks.DIRT.defaultBlockState(), 0);
            // test vertical double blocks
            for (Block doubleBlock : new Block[] { Blocks.OAK_DOOR, Blocks.ROSE_BUSH }) {
                BlockState lowerState = doubleBlock.defaultBlockState();
                BlockState upperState = lowerState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
                level.setBlock(lowerPos, lowerState, 0);
                level.setBlock(abovePos, upperState, 0);
                // validate individual halves are not relocatable but both halves are
                helper.assertFalse(lowerState.getRelocability(level, lowerPos).isRelocatable(Set.of(lowerPos)), lowerState + " incorrectly relocatable without upper half");
                helper.assertFalse(upperState.getRelocability(level, abovePos).isRelocatable(Set.of(abovePos)), upperState + " incorrectly relocatable without lower half");
                helper.assertTrue(lowerState.getRelocability(level, lowerPos).isRelocatable(Set.of(lowerPos, abovePos)), lowerState + " incorrectly non-relocatable with whole multiblock");
                helper.assertTrue(upperState.getRelocability(level, abovePos).isRelocatable(Set.of(lowerPos, abovePos)), upperState + " incorrectly non-relocatable with whole multiblock");
                // clean up blocks or rose bush nukes itself from shape updates
                level.setBlock(lowerPos, Blocks.AIR.defaultBlockState(), 0);
                level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), 0);
            }

            // test beds
            BlockState bedFoot = Blocks.WHITE_BED.defaultBlockState();
            BlockState bedHead = bedFoot.setValue(BedBlock.PART, BedPart.HEAD);
            Direction directionToHead = BedBlock.getConnectedDirection(bedFoot);
            BlockPos headPos = lowerPos.relative(directionToHead);
            // do beds need support? can't remember, just place dirt under where the head will be too
            level.setBlock(floorCenter.relative(directionToHead), Blocks.DIRT.defaultBlockState(), 0);
            level.setBlock(lowerPos, bedFoot, 0);
            level.setBlock(headPos, bedHead, 0);
            helper.assertFalse(bedFoot.getRelocability(level, lowerPos).isRelocatable(Set.of(lowerPos)), "Bed foot " + bedFoot + " incorrectly relocatable without head");
            helper.assertFalse(bedHead.getRelocability(level, headPos).isRelocatable(Set.of(headPos)), "Bed head " + bedHead + " incorrectly relocatable without foot");
            helper.assertTrue(bedFoot.getRelocability(level, lowerPos).isRelocatable(Set.of(lowerPos, headPos)), "Bed foot " + bedFoot + " incorrectly non-relocatable with whole bed");
            helper.assertTrue(bedHead.getRelocability(level, headPos).isRelocatable(Set.of(lowerPos, headPos)), "Bed head " + bedHead + " incorrectly non-relocatable with whole bed");

            // finally, check pistons
            // unextended pistons are always relocatable
            // extended pistons are relocatable if and only if both halves are being relocated
            BlockState unextendedPiston = Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.UP);
            level.setBlock(lowerPos, unextendedPiston, 0);
            helper.assertTrue(unextendedPiston.getRelocability(level, lowerPos).isRelocatable(Set.of(lowerPos)), "Unextended piston " + unextendedPiston + " incorrectly non-relocatable");
            level.setBlock(headPos, Blocks.REDSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            helper.startSequence()
                    .thenExecuteAfter(3, () -> {
                        BlockState pistonBase = level.getBlockState(lowerPos);
                        BlockState pistonHead = level.getBlockState(abovePos);
                        helper.assertFalse(pistonBase.getRelocability(level, lowerPos).isRelocatable(Set.of(lowerPos)), "Piston base " + pistonBase + " incorrectly relocatable without head");
                        helper.assertFalse(pistonHead.getRelocability(level, abovePos).isRelocatable(Set.of(abovePos)), "Piston head " + pistonHead + " incorrectly relocatable without base");
                        helper.assertTrue(pistonBase.getRelocability(level, lowerPos).isRelocatable(Set.of(lowerPos, abovePos)), "Piston base " + pistonBase + " incorrectly non-relocatable with head");
                        helper.assertTrue(pistonHead.getRelocability(level, abovePos).isRelocatable(Set.of(lowerPos, abovePos)), "Piston head " + pistonHead + " incorrectly non-relocatable with base");
                    })
                    .thenSucceed();
        });
    }
}
