/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.data.internal;

import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.neoforge.client.model.item.TrimmedArmorModel;

public class VanillaItemModelProvider extends ItemModelGenerators {
    public VanillaItemModelProvider(ItemModelOutput output, BiConsumer<ResourceLocation, ModelInstance> modelOutput) {
        super(output, modelOutput);
    }

    @Override
    public void run() {
        this.generateDynamicTrimmableItem(Items.TURTLE_HELMET, "helmet");
        this.generateDynamicTrimmableItem(Items.LEATHER_HELMET, "helmet", DyedItemColor.LEATHER_COLOR);
        this.generateDynamicTrimmableItem(Items.LEATHER_CHESTPLATE, "chestplate", DyedItemColor.LEATHER_COLOR);
        this.generateDynamicTrimmableItem(Items.LEATHER_LEGGINGS, "leggings", DyedItemColor.LEATHER_COLOR);
        this.generateDynamicTrimmableItem(Items.LEATHER_BOOTS, "boots", DyedItemColor.LEATHER_COLOR);
        this.generateDynamicTrimmableItem(Items.CHAINMAIL_HELMET, "helmet");
        this.generateDynamicTrimmableItem(Items.CHAINMAIL_CHESTPLATE, "chestplate");
        this.generateDynamicTrimmableItem(Items.CHAINMAIL_LEGGINGS, "leggings");
        this.generateDynamicTrimmableItem(Items.CHAINMAIL_BOOTS, "boots");
        this.generateDynamicTrimmableItem(Items.IRON_HELMET, "helmet");
        this.generateDynamicTrimmableItem(Items.IRON_CHESTPLATE, "chestplate");
        this.generateDynamicTrimmableItem(Items.IRON_LEGGINGS, "leggings");
        this.generateDynamicTrimmableItem(Items.IRON_BOOTS, "boots");
        this.generateDynamicTrimmableItem(Items.DIAMOND_HELMET, "helmet");
        this.generateDynamicTrimmableItem(Items.DIAMOND_CHESTPLATE, "chestplate");
        this.generateDynamicTrimmableItem(Items.DIAMOND_LEGGINGS, "leggings");
        this.generateDynamicTrimmableItem(Items.DIAMOND_BOOTS, "boots");
        this.generateDynamicTrimmableItem(Items.GOLDEN_HELMET, "helmet");
        this.generateDynamicTrimmableItem(Items.GOLDEN_CHESTPLATE, "chestplate");
        this.generateDynamicTrimmableItem(Items.GOLDEN_LEGGINGS, "leggings");
        this.generateDynamicTrimmableItem(Items.GOLDEN_BOOTS, "boots");
        this.generateDynamicTrimmableItem(Items.NETHERITE_HELMET, "helmet");
        this.generateDynamicTrimmableItem(Items.NETHERITE_CHESTPLATE, "chestplate");
        this.generateDynamicTrimmableItem(Items.NETHERITE_LEGGINGS, "leggings");
        this.generateDynamicTrimmableItem(Items.NETHERITE_BOOTS, "boots");
    }

    public void generateDynamicTrimmableItem(Item item, String name) {
        this.generateDynamicTrimmableItem(item, name, -1);
    }

    public void generateDynamicTrimmableItem(Item item, String name, int color) {
        ResourceLocation model = ModelLocationUtils.getModelLocation(item);

        BlockModelWrapper.Unbaked armorModel;
        if (color != -1) {
            armorModel = new BlockModelWrapper.Unbaked(model, List.of(new Dye(color)));
        } else {
            armorModel = new BlockModelWrapper.Unbaked(model, List.of());
        }

        this.itemModelOutput.accept(item, new TrimmedArmorModel.Unbaked(armorModel, ResourceLocation.parse(name + "_trim").withPrefix("trims/items/")));
    }
}
