/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import net.minecraft.core.Holder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Test mod that demos disabling ambient occlusion on specific faces of "elements" models.
 */
@Mod(AmbientOcclusionElementsTest.MOD_ID)
public class AmbientOcclusionElementsTest {
    private static final boolean ENABLED = false;

    public static final String MOD_ID = "ambient_occlusion_elements_test";
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.blocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.items(MOD_ID);

    public static final Holder<Block> AO_BLOCK_SHADE = BLOCKS.block("ambient_occlusion_shade", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    public static final Holder<Block> AO_BLOCK_NO_SHADE = BLOCKS.block("ambient_occlusion_no_shade", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    public static final Holder<Block> NO_AO_BLOCK_SHADE = BLOCKS.block("no_ambient_occlusion_shade", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    public static final Holder<Block> NO_AO_BLOCK_NO_SHADE = BLOCKS.block("no_ambient_occlusion_no_shade", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    public static final DeferredItem<BlockItem> AO_BLOCK_SHADE_ITEM = ITEMS.blockItem("ambient_occlusion_shade", AO_BLOCK_SHADE, new Item.Properties());
    public static final DeferredItem<BlockItem> AO_BLOCK_NO_SHADE_ITEM = ITEMS.blockItem("ambient_occlusion_no_shade", AO_BLOCK_NO_SHADE, new Item.Properties());
    public static final DeferredItem<BlockItem> NO_AO_BLOCK_SHADE_ITEM = ITEMS.blockItem("no_ambient_occlusion_shade", NO_AO_BLOCK_SHADE, new Item.Properties());
    public static final DeferredItem<BlockItem> NO_AO_BLOCK_NO_SHADE_ITEM = ITEMS.blockItem("no_ambient_occlusion_no_shade", NO_AO_BLOCK_NO_SHADE, new Item.Properties());

    public AmbientOcclusionElementsTest() {
        if (!ENABLED)
            return;

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept((ItemLike) AO_BLOCK_SHADE_ITEM);
            event.accept((ItemLike) AO_BLOCK_NO_SHADE_ITEM);
            event.accept((ItemLike) NO_AO_BLOCK_SHADE_ITEM);
            event.accept((ItemLike) NO_AO_BLOCK_NO_SHADE_ITEM);
        }
    }
}
