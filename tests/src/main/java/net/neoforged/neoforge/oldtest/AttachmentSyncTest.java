/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(AttachmentSyncTest.MOD_ID)
public class AttachmentSyncTest {
    public static final String MOD_ID = "attachment_sync_test";
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);
    private static final Supplier<AttachmentType<Integer>> ATTACHMENT_TYPE = ATTACHMENT_TYPES.register("test",
            () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT.fieldOf("value"))
                    .copyOnDeath()
                    .sync(ByteBufCodecs.VAR_INT)
                    .build());
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    static {
        ITEMS.registerItem("tester_blockentity", BlockEntityTester::new);
        ITEMS.registerItem("tester_chunk", ChunkTester::new);
        ITEMS.registerItem("tester_entity", EntityTester::new);
        ITEMS.registerItem("tester_level", LevelTester::new);
    }
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredBlock<Block> TEST_BLOCK = BLOCKS.registerBlock("test_block", TestBlock::new);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    private static final Supplier<BlockEntityType<TestBlockEntity>> TEST_BLOCK_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("test_block", () -> new BlockEntityType<>(TestBlockEntity::new, TEST_BLOCK.get()));

    public AttachmentSyncTest(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
        ITEMS.register(modBus);
        BLOCKS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
    }

    /**
     * On the logical client: print attachment value.
     * On the logical server: increment attachment value, with a reset to 0 when we get to 5, and print the old and new values.
     */
    private static void testInteraction(String what, Player player, IAttachmentHolder holder) {
        if (player.level().isClientSide()) {
            Integer data = holder.getExistingDataOrNull(ATTACHMENT_TYPE);
            player.displayClientMessage(Component.literal(
                    "[Client] Current value on %s is %s.".formatted(
                            what,
                            data == null ? "null" : data.toString())),
                    false);
        } else {
            Integer value = holder.getExistingDataOrNull(ATTACHMENT_TYPE);
            int newValue = value == null ? 1 : value + 1;

            if (newValue >= 5) {
                holder.removeData(ATTACHMENT_TYPE);
            } else {
                holder.setData(ATTACHMENT_TYPE, newValue);
            }

            player.displayClientMessage(Component.literal(
                    "[Server] Changed value on %s from %s to %s.".formatted(
                            what,
                            value == null ? "null" : value.toString(),
                            newValue == 5 ? "null" : newValue)),
                    false);
        }
    }

    private static class BlockEntityTester extends Item {
        public BlockEntityTester(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            if (context.getPlayer() instanceof Player p
                    && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof BlockEntity be) {
                testInteraction("block entity", p, be);
                return InteractionResult.SUCCESS_SERVER;
            }
            return super.useOn(context);
        }
    }

    private static class ChunkTester extends Item {
        public ChunkTester(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            testInteraction("chunk", player, level.getChunkAt(player.blockPosition()));
            return InteractionResult.SUCCESS_SERVER;
        }
    }

    private static class EntityTester extends Item {
        public EntityTester(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity livingEntity, InteractionHand hand) {
            if (player.isSecondaryUseActive()) {
                // Test the player itself if sneaking
                return super.interactLivingEntity(stack, player, livingEntity, hand);
            }
            testInteraction("entity", player, livingEntity);
            return InteractionResult.SUCCESS_SERVER;
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (!player.isSecondaryUseActive()) {
                return super.use(level, player, hand);
            }
            testInteraction("player", player, player);
            return InteractionResult.SUCCESS_SERVER;
        }
    }

    private static class LevelTester extends Item {
        public LevelTester(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            testInteraction("level", player, level);
            return InteractionResult.SUCCESS_SERVER;
        }
    }

    /// Block with a block entity that sets data attachments at unusual timings,
    /// to test how robust data attachment sync is in those cases.
    private static class TestBlock extends BaseEntityBlock {
        public TestBlock(Properties properties) {
            super(properties);
        }

        @Override
        protected MapCodec<? extends BaseEntityBlock> codec() {
            return simpleCodec(TestBlock::new);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
            return new TestBlockEntity(worldPosition, blockState);
        }
    }

    private static class TestBlockEntity extends BlockEntity {
        public TestBlockEntity(BlockPos worldPosition, BlockState blockState) {
            super(TEST_BLOCK_ENTITY_TYPE.get(), worldPosition, blockState);
        }

        @Override
        public void preRemoveSideEffects(BlockPos pos, BlockState state) {
            super.preRemoveSideEffects(pos, state);
            setData(ATTACHMENT_TYPE, 10);
        }

        @Override
        public void setRemoved() {
            super.setRemoved();
            setData(ATTACHMENT_TYPE, 20);
        }
    }
}
