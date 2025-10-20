/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber
@Mod(CustomPlantTypeTest.MODID)
public class CustomPlantTypeTest {
    static final String MODID = "custom_plant_type_test";
    private static final String CUSTOM_SOIL_BLOCK = "test_custom_block";
    private static final String CUSTOM_PLANT_BLOCK = "test_custom_plant";

    public static final DeferredBlock<Block> CUSTOM_SOIL = DeferredBlock.createBlock(ResourceLocation.fromNamespaceAndPath(MODID, CUSTOM_SOIL_BLOCK));
    public static final DeferredBlock<Block> CUSTOM_PLANT = DeferredBlock.createBlock(ResourceLocation.fromNamespaceAndPath(MODID, CUSTOM_PLANT_BLOCK));

    @SubscribeEvent
    public static void registerBlocks(RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> {
            helper.register(CUSTOM_SOIL.getId(), new CustomBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).setId(CUSTOM_SOIL.unwrapKey().orElseThrow())));
            helper.register(CUSTOM_PLANT.getId(), new CustomPlantBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().sound(SoundType.GRASS).setId(CUSTOM_PLANT.unwrapKey().orElseThrow())));
        });
    }

    @SubscribeEvent
    public static void registerItems(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> {
            helper.register(CUSTOM_SOIL.getId(), new BlockItem(CUSTOM_SOIL.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, CUSTOM_SOIL.getId())).useBlockDescriptionPrefix()));
            helper.register(CUSTOM_PLANT.getId(), new BlockItem(CUSTOM_PLANT.get(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, CUSTOM_PLANT.getId())).useBlockDescriptionPrefix()));
        });
    }

    public static class CustomBlock extends Block {
        public CustomBlock(Properties props) {
            super(props);
        }

        @Override
        public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing, BlockState plantable) {
            if (plantable.is(CUSTOM_PLANT)) {
                return TriState.TRUE;
            }
            return super.canSustainPlant(state, level, pos, facing, plantable);
        }
    }

    public static class CustomPlantBlock extends FlowerBlock {
        public CustomPlantBlock(Properties props) {
            super(MobEffects.WEAKNESS, 9, props);
        }

        @Override
        public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
            BlockState soil = world.getBlockState(pos.below());
            TriState soilDecision = soil.canSustainPlant(world, pos.below(), Direction.UP, state);
            if (soilDecision.isDefault()) {
                return soil.is(Blocks.MAGMA_BLOCK);
            }
            return soilDecision.isTrue();
        }

        @Override
        public boolean mayPlaceOn(BlockState state, BlockGetter worldIn, BlockPos pos) {
            Block block = state.getBlock();
            return block == CUSTOM_SOIL.get();
        }
    }
}
