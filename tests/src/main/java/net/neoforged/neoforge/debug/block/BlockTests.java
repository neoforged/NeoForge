/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.enums.BubbleColumnDirection;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
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
        final var gate = reg.blocks().registerBlock("gate", props -> new FenceGateBlock(props, SoundEvents.BARREL_OPEN, SoundEvents.CHEST_CLOSE), BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_FENCE_GATE))
                .withLang("Woodless Fence Gate").withBlockItem();
        reg.provider(BlockStateProvider.class, prov -> prov.fenceGateBlock(gate.get(), ResourceLocation.withDefaultNamespace("block/iron_block")));
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
            protected InteractionResult useItemOn(ItemStack p_316304_, BlockState state, Level world, BlockPos pos, Player player, InteractionHand p_316595_, BlockHitResult p_316140_) {
                if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.setRespawnPosition(world.dimension(), pos, 0, false, true);
                }
                return InteractionResult.SUCCESS;
            }

            @Override
            public Optional<ServerPlayer.RespawnPosAngle> getRespawnPosition(BlockState state, EntityType<?> type, LevelReader levelReader, BlockPos pos, float orientation) {
                // have the player respawn a block north to the location of the anchor
                return Optional.of(ServerPlayer.RespawnPosAngle.of(pos.getCenter().add(0, 1, 1), pos));
            }
        }).withBlockItem().withLang("Respawn").withDefaultWhiteModel();

        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(1, 2, 1, respawn.get()))
                .thenExecute(() -> helper.setBlock(1, 2, 2, Blocks.IRON_BLOCK))

                .thenExecute(player -> helper.useBlock(new BlockPos(1, 2, 1), player))
                .thenExecute(player -> player.getServer().getPlayerList().respawn(player, false, Entity.RemovalReason.CHANGED_DIMENSION))
                .thenExecute(() -> helper.assertEntityPresent(
                        EntityType.PLAYER,
                        1, 3, 2))
                .thenSucceed());
    }

    @GameTest()
    @TestHolder(description = "Adds a block that can sustain Bubble Columns and verify it works")
    static void bubbleColumnTest(final DynamicTest test, final RegistrationHelper reg) {
        final var upwardBubbleColumnSustainingBlock = reg.blocks()
                .registerBlock("upward_bubble_column_sustaining_block", (properties) -> new CustomBubbleColumnSustainingBlock(properties, BubbleColumnDirection.UPWARD), BlockBehaviour.Properties.of())
                .withLang("Upward Bubble Column Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();
        final var downwardBubbleColumnSustainingBlock = reg.blocks()
                .registerBlock("downward_bubble_column_sustaining_block", (properties) -> new CustomBubbleColumnSustainingBlock(properties, BubbleColumnDirection.DOWNWARD), BlockBehaviour.Properties.of())
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
            BubbleColumnBlock.updateColumn(serverLevel, blockPos.above(), blockState);
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
}
