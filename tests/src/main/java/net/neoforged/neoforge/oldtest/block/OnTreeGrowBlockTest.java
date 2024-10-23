/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.block;

import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.extensions.IBlockStateExtension;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * A test case used to ensure that {@link IBlockStateExtension#onTreeGrow(LevelReader, BiConsumer, RandomSource, BlockPos, TreeConfiguration)}
 * works properly, using a custom grass block that should revert to its custom dirt form after a tree grows on
 * top of it instead of turning to dirt.
 */
@Mod(OnTreeGrowBlockTest.ID)
public class OnTreeGrowBlockTest {
    public static final boolean ENABLED = true;

    static final String ID = "on_tree_grow_block_test";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

    public static final Holder<Block> TEST_GRASS_BLOCK = BLOCKS.registerBlock("test_grass_block", props -> new Block(props) {
        @Override
        public TriState canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, BlockState plantable) {
            return plantable.getBlock() instanceof SaplingBlock ? TriState.TRUE : TriState.DEFAULT;
        }

        @Override
        public boolean onTreeGrow(BlockState state, LevelReader level, BiConsumer<BlockPos, BlockState> placeFunction, RandomSource randomSource, BlockPos pos, TreeConfiguration config) {
            // Respect vanilla behavior for trees that want custom dirt blocks
            if (config.forceDirt) {
                return false;
            } else {
                placeFunction.accept(pos, TEST_DIRT.value().defaultBlockState());
                return true;
            }
        }
    }, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).destroyTime(1.5f));
    public static final Holder<Block> TEST_DIRT = BLOCKS.registerBlock("test_dirt", props -> new Block(props) {
        @Override
        public TriState canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, BlockState plantable) {
            return plantable.getBlock() instanceof SaplingBlock ? TriState.TRUE : TriState.DEFAULT;
        }
    }, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).destroyTime(1.5f));
    public static final DeferredItem<BlockItem> TEST_GRASS_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(TEST_GRASS_BLOCK);
    public static final DeferredItem<BlockItem> TEST_DIRT_ITEM = ITEMS.registerSimpleBlockItem(TEST_DIRT);

    public OnTreeGrowBlockTest(IEventBus modBus) {
        if (ENABLED) {
            BLOCKS.register(modBus);
            ITEMS.register(modBus);
        }
    }
}
