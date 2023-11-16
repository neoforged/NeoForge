/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

@Mod(CustomRespawnTest.MODID)
public class CustomRespawnTest {
    public static final boolean ENABLE = true;
    public static final String MODID = "custom_respawn_test";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredBlock<Block> TEST_RESPAWN_BLOCK = BLOCKS.register("test_respawn_block", () -> new CustomRespawnBlock(Block.Properties.of().mapColor(MapColor.WOOD)));

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredItem<BlockItem> TEST_RESPAWN_BLOCK_ITEM = ITEMS.registerBlockItem(TEST_RESPAWN_BLOCK);

    public CustomRespawnTest() {
        if (ENABLE) {
            final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
            BLOCKS.register(eventBus);
            ITEMS.register(eventBus);
        }
    }

    public static class CustomRespawnBlock extends Block {

        public CustomRespawnBlock(Properties propertiesIn) {
            super(propertiesIn);
        }

        @Override
        public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
            if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.setRespawnPosition(world.dimension(), pos, 0, false, true);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        @Override
        public Optional<Vec3> getRespawnPosition(BlockState state, EntityType<?> type, LevelReader levelReader, BlockPos pos, float orientation, @Nullable LivingEntity entity) {
            return RespawnAnchorBlock.findStandUpPosition(type, levelReader, pos);
        }
    }

}
