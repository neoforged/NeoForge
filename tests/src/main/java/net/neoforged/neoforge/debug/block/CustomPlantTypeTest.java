/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.common.IPlantable;
import net.neoforged.neoforge.common.PlantType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(CustomPlantTypeTest.MODID)
@Mod.EventBusSubscriber(bus = Bus.MOD)
public class CustomPlantTypeTest {
    static final String MODID = "custom_plant_type_test";
    private static final String CUSTOM_SOIL_BLOCK = "test_custom_block";
    private static final String CUSTOM_PLANT_BLOCK = "test_custom_plant";

    public static final Holder<Block> CUSTOM_SOIL = DeferredHolder.create(Registries.BLOCK, new ResourceLocation(MODID, CUSTOM_SOIL_BLOCK));
    public static final Holder<Block> CUSTOM_PLANT = DeferredHolder.create(Registries.BLOCK, new ResourceLocation(MODID, CUSTOM_PLANT_BLOCK));

    @SubscribeEvent
    public static void registerBlocks(RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> {
            helper.register(CUSTOM_SOIL_BLOCK, new CustomBlock());
            helper.register(CUSTOM_PLANT_BLOCK, new CustomPlantBlock());
        });
    }

    @SubscribeEvent
    public static void registerItems(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> {
            helper.register(CUSTOM_SOIL_BLOCK, new BlockItem(CUSTOM_SOIL.get(), new Item.Properties()));
            helper.register(CUSTOM_PLANT_BLOCK, new BlockItem(CUSTOM_PLANT.get(), new Item.Properties()));
        });
    }

    public static class CustomBlock extends Block {
        public CustomBlock() {
            super(Properties.of().mapColor(MapColor.STONE));
        }

        @Override
        public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing, IPlantable plantable) {
            PlantType type = plantable.getPlantType(level, pos.relative(facing));
            if (type != null && type == CustomPlantBlock.pt) {
                return true;
            }
            return super.canSustainPlant(state, level, pos, facing, plantable);
        }
    }

    public static class CustomPlantBlock extends FlowerBlock implements IPlantable {
        public static PlantType pt = PlantType.get("custom_plant_type");

        public CustomPlantBlock() {
            super(MobEffects.WEAKNESS, 9, Properties.of().mapColor(MapColor.PLANT).noCollission().sound(SoundType.GRASS));
        }

        @Override
        public PlantType getPlantType(BlockGetter level, BlockPos pos) {
            return pt;
        }

        @Override
        public BlockState getPlant(BlockGetter level, BlockPos pos) {
            return defaultBlockState();
        }

        @Override
        public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
            BlockState soil = world.getBlockState(pos.below());
            return soil.canSustainPlant(world, pos, Direction.UP, this);
        }

        @Override
        public boolean mayPlaceOn(BlockState state, BlockGetter worldIn, BlockPos pos) {
            Block block = state.getBlock();
            return block == CUSTOM_SOIL.get();
        }
    }
}
