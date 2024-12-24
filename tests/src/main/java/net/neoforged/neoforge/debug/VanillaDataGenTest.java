/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import java.util.function.BiConsumer;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "vanilla_data_gen", side = Dist.CLIENT)
public interface VanillaDataGenTest {
    @TestHolder(description = "Tests the patched vanilla model generators work for modded usages")
    static void testModelGenerators(DynamicTest test, RegistrationHelper reg) {
        var headModelName = ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(reg.modId(), "vanilla_model_gen_item_head"));
        // item should appear as red/blue chessboard
        // when worn on head should be cyan/yellow chessboard
        var item = reg.items().registerSimpleItem("vanilla_model_gen_item", new Item.Properties()
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD)
                        .setAsset(headModelName)
                        .build()));

        // block should appear green/red chessboard
        var block = reg.blocks().registerSimpleBlock("vanilla_model_gen_block", BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));
        var blockItem = reg.items().registerSimpleBlockItem(block);

        reg.addClientProvider(event -> new ModelProvider(event.getGenerator().getPackOutput(), reg.modId()) {
            @Override
            protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
                // generate simple cube model for our block
                blockModels.createTrivialCube(block.value());

                // generate simple flat model for our item
                itemModels.generateFlatItem(item.value(), ModelTemplates.FLAT_ITEM.extend().renderType("cutout").build());
            }
        });

        reg.addClientProvider(event -> new EquipmentAssetProvider(event.getGenerator().getPackOutput()) {
            @Override
            protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
                // generate model which switches out the texture when worn
                output.accept(headModelName, EquipmentClientInfo.builder()
                        .addLayers(EquipmentClientInfo.LayerType.HUMANOID, new EquipmentClientInfo.Layer(headModelName.location()))
                        .build());
            }
        });
    }
}
