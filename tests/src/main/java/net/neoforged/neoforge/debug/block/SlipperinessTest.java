/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.ObjectHolder;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(SlipperinessTest.MOD_ID)
@EventBusSubscriber
public class SlipperinessTest {
    static final String MOD_ID = "slipperiness_test";
    static final String BLOCK_ID = "test_block";

    @ObjectHolder(registryName = "block", value = BLOCK_ID)
    public static final Block BB_BLOCK = null;

    @SubscribeEvent
    public static void registerBlocks(RegisterEvent e) {
        e.register(ForgeRegistries.Keys.BLOCKS, helper -> helper.register(BLOCK_ID, new Block(Block.Properties.of()) {
            @Override
            public float getFriction(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
                return entity instanceof Boat ? 2 : super.getFriction(state, level, pos, entity);
            }
        }));
    }

    @SubscribeEvent
    public static void registerItems(RegisterEvent e) {
        e.register(ForgeRegistries.Keys.ITEMS, helper -> helper.register(BLOCK_ID, new BlockItem(BB_BLOCK, new Item.Properties())));
    }

    /*
    @EventBusSubscriber(value = Dist.CLIENT, modid = MOD_ID)
    public static class ClientEventHandler
    {
        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event)
        {
            ModelLoader.setCustomStateMapper(BB_BLOCK, block -> Collections.emptyMap());
            ModelBakery.registerItemVariants(Item.getItemFromBlock(BB_BLOCK));
        }
    }
    */
}
