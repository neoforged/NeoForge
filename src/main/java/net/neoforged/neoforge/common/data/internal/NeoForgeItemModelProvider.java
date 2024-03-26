/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.TrimmableArmorModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NeoForgeItemModelProvider extends ItemModelProvider {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final List<Item> TRIMMABLE_ARMOR_ITEMS = List.of(
            Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS,
            Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS,
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
            Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS,
            Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
            Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS,
            Items.TURTLE_HELMET);

    private static final List<ResourceKey<TrimMaterial>> VANILLA_TRIM_MATERIALS = List.of(
            TrimMaterials.QUARTZ, TrimMaterials.IRON, TrimMaterials.GOLD, TrimMaterials.DIAMOND,
            TrimMaterials.NETHERITE, TrimMaterials.REDSTONE, TrimMaterials.COPPER, TrimMaterials.EMERALD,
            TrimMaterials.LAPIS, TrimMaterials.AMETHYST);

    private static final ResourceLocation INTERNAL_MODEL_ID = new ResourceLocation("neoforge:internal");

    public NeoForgeItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, "neoforge", existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ModelFile.ExistingModelFile generatedItemModel = getExistingFile(new ResourceLocation("minecraft:item/generated"));
        for (Item item : TRIMMABLE_ARMOR_ITEMS) {
            if (!(item instanceof ArmorItem armorItem)) {
                throw new IllegalStateException("Item " + item.toString() + " is not an ArmorItem!");
            }
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(armorItem);
            ResourceLocation itemTexture = new ResourceLocation(id.getNamespace(), "item/" + id.getPath());
            ResourceLocation trimTexture = switch (armorItem.getEquipmentSlot()) {
                case HEAD -> new ResourceLocation("minecraft:trims/items/helmet_trim");
                case CHEST -> new ResourceLocation("minecraft:trims/items/chestplate_trim");
                case LEGS -> new ResourceLocation("minecraft:trims/items/leggings_trim");
                case FEET -> new ResourceLocation("minecraft:trims/items/boots_trim");
                default -> throw new IllegalStateException("Unexpected value: " + armorItem.getEquipmentSlot());
            };
            TrimmableArmorModelBuilder<ItemModelBuilder> builder = getBuilder(id.toString()).customLoader(TrimmableArmorModelBuilder::begin);
            if (armorItem.getMaterial() == ArmorMaterials.LEATHER) {
                builder.untrimmed(
                        new ItemModelBuilder(INTERNAL_MODEL_ID, existingFileHelper)
                                .parent(generatedItemModel)
                                .texture("layer0", itemTexture)
                                .texture("layer1", itemTexture + "_overlay"))
                        .trimmed(
                                new ItemModelBuilder(INTERNAL_MODEL_ID, existingFileHelper)
                                        .parent(generatedItemModel)
                                        .texture("layer0", itemTexture)
                                        .texture("layer1", itemTexture + "_overlay"),
                                "layer2", trimTexture);
            } else {
                builder.untrimmed(
                        new ItemModelBuilder(INTERNAL_MODEL_ID, existingFileHelper)
                                .parent(generatedItemModel)
                                .texture("layer0", itemTexture))
                        .trimmed(
                                new ItemModelBuilder(INTERNAL_MODEL_ID, existingFileHelper)
                                        .parent(generatedItemModel)
                                        .texture("layer0", itemTexture),
                                "layer1", trimTexture);
            }

            for (ResourceKey<TrimMaterial> trimMaterial : VANILLA_TRIM_MATERIALS) {
                String assetName = trimMaterial.location().getPath();
                if (assetName.equals(armorItem.getMaterial().getName())) assetName += "_darker";
                builder.override(trimMaterial.location().getPath(),
                        new ItemModelBuilder(INTERNAL_MODEL_ID, existingFileHelper)
                                .parent(getExistingFile(new ResourceLocation(id.getNamespace(), id.getPath() + "_" + assetName + "_trim"))));
            }
        }
    }
}
