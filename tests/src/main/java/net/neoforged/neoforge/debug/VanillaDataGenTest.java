/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import java.util.function.BiConsumer;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.EquipmentModelProvider;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.EquipmentModel;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.joml.Vector3f;

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

        reg.addProvider(event -> new ModelProvider(event.getGenerator().getPackOutput(), reg.modId()) {
            @Override
            protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
                // generate simple cube model for our block
                // blockModels.createTrivialCube(block.value());
                // generates the same output as above but with added render type (cutout)
                blockModels.createTrivialBlock(block.value(), TexturedModel.CUBE.withRenderType("cutout"));

                // generate simple flat model for our item
                itemModels.generateFlatItem(item.value(), ModelTemplates.FLAT_ITEM.withRenderType("cutout"));

                // generates the same output as generateFlatItem
                // but running through custom model building instead of templates
                /*itemModels.generateCustom(item.value(), builder -> builder
                        // .parent(getExistingModel("item/generated"))
                        .parent(itemModels.getExistingModel("generated"))
                        .texture(TextureSlot.LAYER0, TextureMapping.getItemTexture(item.value()))
                        .renderType("cutout"));*/

                // It is possible to tell system to not generate a BlockItem model for a matching Block
                // this allows generating your own custom Item model for your BlockItem
                // blockModels.skipAutoItemBlock(block.value());
                // itemModels.generateFlatItem(blockItem.value(), ModelTemplates.FLAT_HANDHELD_ITEM);

                // custom model generation
                // output should have all the below custom properties
                // parent 'item/generated'
                // and have texture slot 'layer0' set to 'item/diamond'
                ModelTemplates.FLAT_ITEM
                        .withItemTransform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new ItemTransform(new Vector3f(75F, 45F, 0F), new Vector3f(0F, 3F, 4F), new Vector3f(.375F)))
                        .withItemTransform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, new ItemTransform(new Vector3f(75F, 45F, 0F), new Vector3f(0F, 3F, 4F), new Vector3f(.375F)))
                        .withItemTransform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new ItemTransform(new Vector3f(0F, 135F, 0F), new Vector3f(0F, 7F, 4F), new Vector3f(.4F)))
                        .withItemTransform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, new ItemTransform(new Vector3f(0F, 135F, 0F), new Vector3f(0F, 7F, 4F), new Vector3f(.4F)))
                        .withItemTransform(ItemDisplayContext.HEAD, new ItemTransform(new Vector3f(0F), new Vector3f(0F, 30F, 4F), new Vector3f(1F)))
                        .withItemTransform(ItemDisplayContext.GROUND, new ItemTransform(new Vector3f(0F), new Vector3f(0F, 6F, 4F), new Vector3f(.25F)))
                        .withItemTransform(ItemDisplayContext.FIXED, new ItemTransform(new Vector3f(-90F, 0F, 0F), new Vector3f(0F, 0F, -23F), new Vector3f(1F)))
                        .withItemTransform(ItemDisplayContext.GUI, new ItemTransform(new Vector3f(30F, -135F, 0F), new Vector3f(0F, 3F, 0F), new Vector3f(.5F)))
                        .withRenderType("cutout")
                        .withAmbientOcclusion(true)
                        .withGuiLight(BlockModel.GuiLight.FRONT)
                        .create(itemModels.modLocation("custom_model_generation"), TextureMapping.layer0(Items.DIAMOND), itemModels.output);
            }
        });

        reg.addProvider(event -> new EquipmentModelProvider(event.getGenerator().getPackOutput()) {
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
