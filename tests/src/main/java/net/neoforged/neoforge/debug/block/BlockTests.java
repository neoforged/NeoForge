/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.jetbrains.annotations.Nullable;

@ForEachTest(groups = BlockTests.GROUP)
public class BlockTests {
    public static final String GROUP = "level.block";

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if custom fence gates without wood types work, allowing for the use of the vanilla block for non-wooden gates")
    static void woodlessFenceGate(final DynamicTest test, final RegistrationHelper reg) {
        final var gate = reg.blocks().register("gate", () -> new FenceGateBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_FENCE_GATE), SoundEvents.BARREL_OPEN, SoundEvents.CHEST_CLOSE))
                .withLang("Woodless Fence Gate").withBlockItem();
        reg.provider(BlockStateProvider.class, prov -> prov.fenceGateBlock(gate.get(), new ResourceLocation("block/iron_block")));
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
        final var respawn = reg.blocks().register("respawn", () -> new Block(BlockBehaviour.Properties.of()) {
            @Override
            protected ItemInteractionResult useItemOn(ItemStack p_316304_, BlockState state, Level world, BlockPos pos, Player player, InteractionHand p_316595_, BlockHitResult p_316140_) {
                if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.setRespawnPosition(world.dimension(), pos, 0, false, true);
                }
                return ItemInteractionResult.sidedSuccess(world.isClientSide);
            }

            @Override
            public Optional<Vec3> getRespawnPosition(BlockState state, EntityType<?> type, LevelReader levelReader, BlockPos pos, float orientation, @Nullable LivingEntity entity) {
                // have the player respawn a block north to the location of the anchor
                return Optional.of(pos.getCenter().add(0, 1, 1));
            }
        }).withBlockItem().withLang("Respawn").withDefaultWhiteModel();

        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(1, 2, 1, respawn.get()))
                .thenExecute(() -> helper.setBlock(1, 2, 2, Blocks.IRON_BLOCK))

                .thenExecute(player -> helper.useBlock(new BlockPos(1, 2, 1), player))
                .thenExecute(player -> player.getServer().getPlayerList().respawn(player, false))
                .thenExecute(() -> helper.assertEntityPresent(
                        EntityType.PLAYER,
                        1, 3, 2))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = {
            "Dead bushes should be placeable on regular terracotta (colored or not), but not on glazed terracotta",
            "(neoforged/NeoForge#306)"
    })
    static void deadBushTerracottaTest(final ExtendedGameTestHelper helper) {
        final BlockPos farmlandBlock = new BlockPos(1, 1, 1);
        helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(farmlandBlock, Blocks.TERRACOTTA))
                .thenExecute(player -> helper.useBlock(farmlandBlock, player, new ItemStack(Items.DEAD_BUSH), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.DEAD_BUSH, farmlandBlock.above()))

                .thenExecute(() -> helper.setBlock(farmlandBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(farmlandBlock, Blocks.WHITE_TERRACOTTA))
                .thenExecute(player -> helper.useBlock(farmlandBlock, player, new ItemStack(Items.DEAD_BUSH), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.DEAD_BUSH, farmlandBlock.above()))

                .thenExecute(() -> helper.setBlock(farmlandBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(farmlandBlock, Blocks.WHITE_GLAZED_TERRACOTTA))
                .thenExecute(player -> helper.useBlock(farmlandBlock, player, new ItemStack(Items.DEAD_BUSH), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.DEAD_BUSH, farmlandBlock.above()))

                .thenSucceed();
    }
}
