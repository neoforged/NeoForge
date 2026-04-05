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
import net.minecraft.world.level.block.Block;

public class VanillaModelProvider extends ModelProvider {
    public VanillaModelProvider(PackOutput packOutput) {
        super(packOutput, "minecraft");
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateDynamicTrimmableItem(Items.TURTLE_HELMET, ItemModelGenerators.TRIM_PREFIX_HELMET);
        itemModels.generateDynamicTrimmableItem(Items.LEATHER_HELMET, ItemModelGenerators.TRIM_PREFIX_HELMET, DyedItemColor.LEATHER_COLOR);
        itemModels.generateDynamicTrimmableItem(Items.LEATHER_CHESTPLATE, ItemModelGenerators.TRIM_PREFIX_CHESTPLATE, DyedItemColor.LEATHER_COLOR);
        itemModels.generateDynamicTrimmableItem(Items.LEATHER_LEGGINGS, ItemModelGenerators.TRIM_PREFIX_LEGGINGS, DyedItemColor.LEATHER_COLOR);
        itemModels.generateDynamicTrimmableItem(Items.LEATHER_BOOTS, ItemModelGenerators.TRIM_PREFIX_BOOTS, DyedItemColor.LEATHER_COLOR);
        itemModels.generateDynamicTrimmableItem(Items.CHAINMAIL_HELMET, ItemModelGenerators.TRIM_PREFIX_HELMET);
        itemModels.generateDynamicTrimmableItem(Items.CHAINMAIL_CHESTPLATE, ItemModelGenerators.TRIM_PREFIX_CHESTPLATE);
        itemModels.generateDynamicTrimmableItem(Items.CHAINMAIL_LEGGINGS, ItemModelGenerators.TRIM_PREFIX_LEGGINGS);
        itemModels.generateDynamicTrimmableItem(Items.CHAINMAIL_BOOTS, ItemModelGenerators.TRIM_PREFIX_BOOTS);
        itemModels.generateDynamicTrimmableItem(Items.IRON_HELMET, ItemModelGenerators.TRIM_PREFIX_HELMET);
        itemModels.generateDynamicTrimmableItem(Items.IRON_CHESTPLATE, ItemModelGenerators.TRIM_PREFIX_CHESTPLATE);
        itemModels.generateDynamicTrimmableItem(Items.IRON_LEGGINGS, ItemModelGenerators.TRIM_PREFIX_LEGGINGS);
        itemModels.generateDynamicTrimmableItem(Items.IRON_BOOTS, ItemModelGenerators.TRIM_PREFIX_BOOTS);
        itemModels.generateDynamicTrimmableItem(Items.DIAMOND_HELMET, ItemModelGenerators.TRIM_PREFIX_HELMET);
        itemModels.generateDynamicTrimmableItem(Items.DIAMOND_CHESTPLATE, ItemModelGenerators.TRIM_PREFIX_CHESTPLATE);
        itemModels.generateDynamicTrimmableItem(Items.DIAMOND_LEGGINGS, ItemModelGenerators.TRIM_PREFIX_LEGGINGS);
        itemModels.generateDynamicTrimmableItem(Items.DIAMOND_BOOTS, ItemModelGenerators.TRIM_PREFIX_BOOTS);
        itemModels.generateDynamicTrimmableItem(Items.GOLDEN_HELMET, ItemModelGenerators.TRIM_PREFIX_HELMET);
        itemModels.generateDynamicTrimmableItem(Items.GOLDEN_CHESTPLATE, ItemModelGenerators.TRIM_PREFIX_CHESTPLATE);
        itemModels.generateDynamicTrimmableItem(Items.GOLDEN_LEGGINGS, ItemModelGenerators.TRIM_PREFIX_LEGGINGS);
        itemModels.generateDynamicTrimmableItem(Items.GOLDEN_BOOTS, ItemModelGenerators.TRIM_PREFIX_BOOTS);
        itemModels.generateDynamicTrimmableItem(Items.NETHERITE_HELMET, ItemModelGenerators.TRIM_PREFIX_HELMET);
        itemModels.generateDynamicTrimmableItem(Items.NETHERITE_CHESTPLATE, ItemModelGenerators.TRIM_PREFIX_CHESTPLATE);
        itemModels.generateDynamicTrimmableItem(Items.NETHERITE_LEGGINGS, ItemModelGenerators.TRIM_PREFIX_LEGGINGS);
        itemModels.generateDynamicTrimmableItem(Items.NETHERITE_BOOTS, ItemModelGenerators.TRIM_PREFIX_BOOTS);
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
