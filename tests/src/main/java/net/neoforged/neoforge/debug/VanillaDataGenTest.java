/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import java.util.function.BiConsumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.EquipmentModelProvider;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.ModelProvider;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.EquipmentModel;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "vanilla_data_gen")
public interface VanillaDataGenTest {
    @TestHolder(description = "Tests the the patched vanilla model generators work for modded usages")
    static void testModelGenerators(DynamicTest test, RegistrationHelper reg) {
        var headModelName = ResourceLocation.fromNamespaceAndPath(reg.modId(), "vanilla_model_gen_item_head");
        // item should appear as red/blue chessboard
        // when worn on head should be cyan/yellow chessboard
        var item = reg.items().registerSimpleItem("vanilla_model_gen_item", new Item.Properties()
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD)
                        .setModel(headModelName)
                        .build()));

        // block should appear green/red chessboard
        var block = reg.blocks().registerSimpleBlock("vanilla_model_gen_block", BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));
        var blockItem = reg.items().registerSimpleBlockItem(block);

        reg.addProvider(event -> new ModelProvider(event.getGenerator().getPackOutput(), reg.modId(), event.getExistingFileHelper()) {
            @Override
            protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
                // generate simple cube model for our block
                blockModels.createTrivialCube(block.value());

                // generate simple flat model for our item
                itemModels.generateFlatItem(item.value(), ModelTemplates.FLAT_ITEM);

                // It is possible to tell system to not generate a BlockItem model for a matching Block
                // this allows generating your own custom Item model for your BlockItem
                // blockModels.skipAutoItemBlock(block.value());
                // itemModels.generateFlatItem(blockItem.value(), ModelTemplates.FLAT_HANDHELD_ITEM);
            }
        });

        reg.addProvider(event -> new EquipmentModelProvider(event.getGenerator().getPackOutput(), event.getExistingFileHelper()) {
            @Override
            protected void registerModels(BiConsumer<ResourceLocation, EquipmentModel> consumer) {
                // generate model which switches out the texture when worn
                consumer.accept(headModelName, EquipmentModel.builder()
                        .addLayers(EquipmentModel.LayerType.HUMANOID, new EquipmentModel.Layer(headModelName))
                        .build());
            }
        });
    }
}
