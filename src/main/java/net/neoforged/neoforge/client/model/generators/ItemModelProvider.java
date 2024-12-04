/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators;

import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Stub class to extend for item model data providers, eliminates some
 * boilerplate constructor parameters.
 */
public abstract class ItemModelProvider extends ModelProvider<ItemModelBuilder> {
    public ItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, ITEM_FOLDER, ItemModelBuilder::new, existingFileHelper);
    }

    public ItemModelBuilder basicItem(Item item) {
        return basicItem(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)));
    }

    public ItemModelBuilder basicItem(ResourceLocation item) {
        return getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/" + item.getPath()));
    }

    public ItemModelBuilder handheldItem(Item item) {
        return handheldItem(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)));
    }

    public ItemModelBuilder handheldItem(ResourceLocation item) {
        return getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/" + item.getPath()));
    }

    public ItemModelBuilder spawnEggItem(Item item) {
        return spawnEggItem(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)));
    }

    public ItemModelBuilder spawnEggItem(ResourceLocation item) {
        return getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/template_spawn_egg"));
    }

    public ItemModelBuilder simpleBlockItem(Block block) {
        return simpleBlockItem(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)));
    }

    public ItemModelBuilder simpleBlockItem(ResourceLocation block) {
        return withExistingParent(block.toString(), ResourceLocation.fromNamespaceAndPath(block.getNamespace(), "block/" + block.getPath()));
    }

    @Override
    public String getName() {
        return "Item Models: " + modid;
    }
}
