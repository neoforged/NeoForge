/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class VanillaItemModelProvider extends ItemModelProvider {
    public VanillaItemModelProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, "minecraft", helper);
    }

    @Override
    protected void registerModels() {
        dynamicTrimmableItem((ArmorItem) Items.TURTLE_HELMET);
        dynamicTrimmableItem((ArmorItem) Items.LEATHER_HELMET);
        dynamicTrimmableItem((ArmorItem) Items.LEATHER_CHESTPLATE);
        dynamicTrimmableItem((ArmorItem) Items.LEATHER_LEGGINGS);
        dynamicTrimmableItem((ArmorItem) Items.LEATHER_BOOTS);
        dynamicTrimmableItem((ArmorItem) Items.CHAINMAIL_HELMET);
        dynamicTrimmableItem((ArmorItem) Items.CHAINMAIL_CHESTPLATE);
        dynamicTrimmableItem((ArmorItem) Items.CHAINMAIL_LEGGINGS);
        dynamicTrimmableItem((ArmorItem) Items.CHAINMAIL_BOOTS);
        dynamicTrimmableItem((ArmorItem) Items.IRON_HELMET);
        dynamicTrimmableItem((ArmorItem) Items.IRON_CHESTPLATE);
        dynamicTrimmableItem((ArmorItem) Items.IRON_LEGGINGS);
        dynamicTrimmableItem((ArmorItem) Items.IRON_BOOTS);
        dynamicTrimmableItem((ArmorItem) Items.DIAMOND_HELMET);
        dynamicTrimmableItem((ArmorItem) Items.DIAMOND_CHESTPLATE);
        dynamicTrimmableItem((ArmorItem) Items.DIAMOND_LEGGINGS);
        dynamicTrimmableItem((ArmorItem) Items.DIAMOND_BOOTS);
        dynamicTrimmableItem((ArmorItem) Items.GOLDEN_HELMET);
        dynamicTrimmableItem((ArmorItem) Items.GOLDEN_CHESTPLATE);
        dynamicTrimmableItem((ArmorItem) Items.GOLDEN_LEGGINGS);
        dynamicTrimmableItem((ArmorItem) Items.GOLDEN_BOOTS);
        dynamicTrimmableItem((ArmorItem) Items.NETHERITE_HELMET);
        dynamicTrimmableItem((ArmorItem) Items.NETHERITE_CHESTPLATE);
        dynamicTrimmableItem((ArmorItem) Items.NETHERITE_LEGGINGS);
        dynamicTrimmableItem((ArmorItem) Items.NETHERITE_BOOTS);
    }
}
