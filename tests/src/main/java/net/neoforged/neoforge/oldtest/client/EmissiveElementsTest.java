/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client;

import java.util.stream.Stream;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Test mod that demos emissivity on "elements" models and on item layer textures.
 */
@Mod(EmissiveElementsTest.MOD_ID)
public class EmissiveElementsTest {
    public static final String MOD_ID = "emissive_elements_test";
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredBlock<Block> TEST_BLOCK = BLOCKS.registerSimpleBlock("emissive", props -> props.mapColor(MapColor.STONE));
    public static final DeferredItem<BlockItem> TEST_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(TEST_BLOCK);
    // Exercises ItemLayerKey#compute with a non-default ExtraFaceData: light_emission=15 on layer0.
    public static final DeferredItem<Item> TEST_GLOW_ITEM = ITEMS.registerSimpleItem("glow_item");

    public EmissiveElementsTest(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(TEST_BLOCK_ITEM);
            event.accept(TEST_GLOW_ITEM);
        }
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    static final class DatagenHandler {
        @SubscribeEvent
        static void onGatherData(GatherDataEvent.Client event) {
            event.addProvider(new ModelProvider(event.getGenerator().getPackOutput(), MOD_ID) {
                @Override
                protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
                    Identifier modelId = ModelTemplates.FLAT_ITEM.extend()
                            .itemLayerFaceData("layer0", new ExtraFaceData(-1, 15, false))
                            .build()
                            .create(TEST_GLOW_ITEM.value(), TextureMapping.layer0(Items.REDSTONE), itemModels.modelOutput);
                    itemModels.itemModelOutput.accept(TEST_GLOW_ITEM.value(), ItemModelUtils.plainModel(modelId));
                }

                @Override
                protected Stream<? extends Holder<Block>> getKnownBlocks() {
                    return Stream.empty();
                }

                @Override
                protected Stream<? extends Holder<Item>> getKnownItems() {
                    return Stream.of(TEST_GLOW_ITEM);
                }
            });
        }

        private DatagenHandler() {}
    }
}
