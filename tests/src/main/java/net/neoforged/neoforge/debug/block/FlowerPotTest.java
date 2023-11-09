/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(FlowerPotTest.MODID)
@Mod.EventBusSubscriber(modid = FlowerPotTest.MODID, bus = Bus.MOD)
public class FlowerPotTest {
    static final String MODID = "flower_pot_test";
    static final String BLOCK_ID = "test_flower_pot";

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, MODID);

    public static final DeferredHolder<Block, FlowerPotBlock> EMPTY_FLOWER_POT = BLOCKS.register(BLOCK_ID, () -> new FlowerPotBlock(null, () -> Blocks.AIR, Block.Properties.copy(Blocks.FLOWER_POT)));
    public static final DeferredHolder<Block, FlowerPotBlock> OAK_FLOWER_POT = BLOCKS.register(BLOCK_ID + "_oak", () -> new FlowerPotBlock(EMPTY_FLOWER_POT, () -> Blocks.OAK_SAPLING, Block.Properties.copy(Blocks.FLOWER_POT)));

    static {
        ITEMS.register(BLOCK_ID, () -> new BlockItem(EMPTY_FLOWER_POT.get(), new Item.Properties()));
    }

    @SubscribeEvent
    public static void onItemRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ITEM)) {
            EMPTY_FLOWER_POT.get().addPlant(BuiltInRegistries.BLOCK.getKey(Blocks.OAK_SAPLING), OAK_FLOWER_POT);
        }
    }

    public FlowerPotTest() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }
}
