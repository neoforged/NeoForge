/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.model;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

@Mod(CompositeModelTest.MODID)
public class CompositeModelTest {
    public static final String MODID = "composite_model_test";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static DeferredBlock<Block> composite_block = BLOCKS.register("composite_block", () -> new Block(Block.Properties.of().mapColor(MapColor.WOOD).strength(10)) {
        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(BlockStateProperties.HORIZONTAL_FACING);
        }

        @Nullable
        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return defaultBlockState().setValue(
                    BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection());
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
            return Shapes.or(
                    box(5.6, 5.6, 5.6, 10.4, 10.4, 10.4),
                    box(0, 0, 0, 4.8, 4.8, 4.8),
                    box(11.2, 0, 0, 16, 4.8, 4.8),
                    box(0, 0, 11.2, 4.8, 4.8, 16),
                    box(11.2, 0, 11.2, 16, 4.8, 16),
                    box(0, 11.2, 0, 4.8, 16, 4.8),
                    box(11.2, 11.2, 0, 16, 16, 4.8),
                    box(0, 11.2, 11.2, 4.8, 16, 16),
                    box(11.2, 11.2, 11.2, 16, 16, 16));
        }
    });

    public static DeferredItem<Item> composite_item = ITEMS.register("composite_block", () -> new BlockItem(composite_block.get(), new Item.Properties()) {
        @Override
        public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
            return armorType == EquipmentSlot.HEAD;
        }
    });

    public CompositeModelTest(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
            event.accept(composite_item);
    }
}
