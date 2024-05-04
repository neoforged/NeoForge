/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.block;

import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CustomSustainBubbleColumnTest.MODID)
public class CustomSustainBubbleColumnTest {
    static final String MODID = "custom_sustain_bubble_column_test";
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    private static final boolean ENABLE = true;

    public CustomSustainBubbleColumnTest(IEventBus modBus) {
        if (!ENABLE)
            return;

        BLOCKS.register(modBus);
    }

    private static class CustomUpwardSustainingBlock extends Block {
        public CustomUpwardSustainingBlock(Properties properties) {
            super(properties);
        }

        @Override
        protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
            BubbleColumnBlock.updateColumn(serverLevel, blockPos.above(), blockState);
        }

        @Override
        protected BlockState updateShape(BlockState currentBlockState, Direction direction, BlockState sideBlockState, LevelAccessor levelAccessor, BlockPos currentBlockPos, BlockPos sideBlockPos) {
            if (direction == Direction.UP && sideBlockState.is(Blocks.WATER)) {
                levelAccessor.scheduleTick(currentBlockPos, this, 20);
            }

            return super.updateShape(currentBlockState, direction, sideBlockState, levelAccessor, currentBlockPos, sideBlockPos);
        }

        @Override
        protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState oldBlockState, boolean isMoving) {
            level.scheduleTick(blockPos, this, 20);
        }

        @Override
        public BubbleColumnDirection sustainBubbleColumn(BlockState state) {
            return BubbleColumnDirection.UPWARD;
        }
    }
}
