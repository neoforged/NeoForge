/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.data.internal;

import java.util.stream.Stream;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.item.TrimmedArmorModel.PaletteTransform;

public class VanillaModelProvider extends ModelProvider {
    public VanillaModelProvider(PackOutput packOutput) {
        super(packOutput, "minecraft");
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateDynamicTrimmableItem(Items.TURTLE_HELMET, ItemModelGenerators.TRIM_PREFIX_HELMET, null);
        itemModels.generateDynamicTrimmableArmorSet(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS,
                DyedItemColor.LEATHER_COLOR, null);
        itemModels.generateDynamicTrimmableArmorSet(Items.COPPER_HELMET, Items.COPPER_CHESTPLATE, Items.COPPER_LEGGINGS, Items.COPPER_BOOTS,
                new PaletteTransform(TrimMaterials.Palette.COPPER, TrimMaterials.Palette.COPPER_DARKER));
        itemModels.generateDynamicTrimmableArmorSet(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS, null);
        itemModels.generateDynamicTrimmableArmorSet(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
                new PaletteTransform(TrimMaterials.Palette.IRON, TrimMaterials.Palette.IRON_DARKER));
        itemModels.generateDynamicTrimmableArmorSet(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
                new PaletteTransform(TrimMaterials.Palette.DIAMOND, TrimMaterials.Palette.DIAMOND_DARKER));
        itemModels.generateDynamicTrimmableArmorSet(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS,
                new PaletteTransform(TrimMaterials.Palette.GOLD, TrimMaterials.Palette.GOLD_DARKER));
        itemModels.generateDynamicTrimmableArmorSet(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS,
                new PaletteTransform(TrimMaterials.Palette.NETHERITE, TrimMaterials.Palette.NETHERITE_DARKER));
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.empty();
    }
}
