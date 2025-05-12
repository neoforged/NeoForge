/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.data.internal;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.neoforged.neoforge.client.model.item.TrimmedArmorModel;

public class VanillaItemModelProvider extends ItemModelGenerators {
    public VanillaItemModelProvider(ItemModelOutput output, BiConsumer<ResourceLocation, ModelInstance> modelOutput) {
        super(output, modelOutput);
    }

    @Override
    public void run() {
        this.generateDynamicTrimmableItem(Items.TURTLE_HELMET, "helmet");
        this.generateDynamicTrimmableItem(Items.LEATHER_HELMET, "helmet", -6265536);
        this.generateDynamicTrimmableItem(Items.LEATHER_CHESTPLATE, "chestplate", -6265536);
        this.generateDynamicTrimmableItem(Items.LEATHER_LEGGINGS, "leggings", -6265536);
        this.generateDynamicTrimmableItem(Items.LEATHER_BOOTS, "boots", -6265536);
        this.generateDynamicTrimmableItem(Items.CHAINMAIL_HELMET, "helmet");
        this.generateDynamicTrimmableItem(Items.CHAINMAIL_CHESTPLATE, "chestplate");
        this.generateDynamicTrimmableItem(Items.CHAINMAIL_LEGGINGS, "leggings");
        this.generateDynamicTrimmableItem(Items.CHAINMAIL_BOOTS, "boots");
        this.generateDynamicTrimmableItem(Items.IRON_HELMET, "helmet", TrimMaterials.IRON);
        this.generateDynamicTrimmableItem(Items.IRON_CHESTPLATE, "chestplate", TrimMaterials.IRON);
        this.generateDynamicTrimmableItem(Items.IRON_LEGGINGS, "leggings", TrimMaterials.IRON);
        this.generateDynamicTrimmableItem(Items.IRON_BOOTS, "boots", TrimMaterials.IRON);
        this.generateDynamicTrimmableItem(Items.DIAMOND_HELMET, "helmet", TrimMaterials.DIAMOND);
        this.generateDynamicTrimmableItem(Items.DIAMOND_CHESTPLATE, "chestplate", TrimMaterials.DIAMOND);
        this.generateDynamicTrimmableItem(Items.DIAMOND_LEGGINGS, "leggings", TrimMaterials.DIAMOND);
        this.generateDynamicTrimmableItem(Items.DIAMOND_BOOTS, "boots", TrimMaterials.DIAMOND);
        this.generateDynamicTrimmableItem(Items.GOLDEN_HELMET, "helmet", TrimMaterials.GOLD);
        this.generateDynamicTrimmableItem(Items.GOLDEN_CHESTPLATE, "chestplate", TrimMaterials.GOLD);
        this.generateDynamicTrimmableItem(Items.GOLDEN_LEGGINGS, "leggings", TrimMaterials.GOLD);
        this.generateDynamicTrimmableItem(Items.GOLDEN_BOOTS, "boots", TrimMaterials.GOLD);
        this.generateDynamicTrimmableItem(Items.NETHERITE_HELMET, "helmet", TrimMaterials.NETHERITE);
        this.generateDynamicTrimmableItem(Items.NETHERITE_CHESTPLATE, "chestplate", TrimMaterials.NETHERITE);
        this.generateDynamicTrimmableItem(Items.NETHERITE_LEGGINGS, "leggings", TrimMaterials.NETHERITE);
        this.generateDynamicTrimmableItem(Items.NETHERITE_BOOTS, "boots", TrimMaterials.NETHERITE);
    }

    public void generateDynamicTrimmableItem(Item item, String name) {
        this.generateDynamicTrimmableItem(item, name, -1, Optional.empty());
    }

    public void generateDynamicTrimmableItem(Item item, String name, ResourceKey<TrimMaterial> darkerTrim) {
        this.generateDynamicTrimmableItem(item, name, -1, Optional.of(darkerTrim));
    }

    public void generateDynamicTrimmableItem(Item item, String name, int color) {
        this.generateDynamicTrimmableItem(item, name, color, Optional.empty());
    }

    public void generateDynamicTrimmableItem(Item item, String name, int dyeColor, Optional<ResourceKey<TrimMaterial>> darkerTrim) {
        ResourceLocation model = ModelLocationUtils.getModelLocation(item);

        BlockModelWrapper.Unbaked armorModel;
        if (dyeColor != -1) {
            armorModel = new BlockModelWrapper.Unbaked(model, List.of(new Dye(dyeColor)));
        } else {
            armorModel = new BlockModelWrapper.Unbaked(model, List.of());
        }

        this.itemModelOutput.accept(item, new TrimmedArmorModel.Unbaked(armorModel, ResourceLocation.parse(name + "_trim").withPrefix("trims/items/"), darkerTrim.map(ResourceKey::location)));
    }
}
