/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.outgredient.DefaultedItemTagOutgredient;
import net.neoforged.neoforge.common.crafting.outgredient.Outgredient;
import net.neoforged.neoforge.common.crafting.outgredient.OutgredientWrapper;

public interface IShapedRecipeBuilderExtension {
    /**
     * Creates a new {@link ShapedRecipeBuilder} using an {@link ItemStack} as the result.
     *
     * @param items    The item holder getter, usually the registry.
     * @param category The {@link RecipeCategory} to use.
     * @param result   The result {@link ItemStack} to use.
     * @return A new {@link ShapedRecipeBuilder}.
     */
    static ShapedRecipeBuilder shaped(HolderGetter<Item> items, RecipeCategory category, ItemStack result) {
        return shaped(items, category, Outgredient.ofItem(result));
    }

    /**
     * Creates a new {@link ShapedRecipeBuilder} using a {@link TagKey} and {@link ItemLike} combination as the result.
     *
     * @param items    The item holder getter, usually the registry.
     * @param category The {@link RecipeCategory} to use.
     * @param tagKey   The {@link TagKey} to use.
     * @param fallback The fallback {@link ItemLike} to use.
     * @return A new {@link ShapedRecipeBuilder}.
     */
    static ShapedRecipeBuilder shaped(HolderGetter<Item> items, RecipeCategory category, TagKey<Item> tagKey, ItemLike fallback) {
        return shaped(items, category, tagKey, fallback, 1);
    }

    /**
     * Creates a new {@link ShapedRecipeBuilder} using a {@link TagKey} and {@link ItemLike} combination as the result.
     *
     * @param items    The item holder getter, usually the registry.
     * @param category The {@link RecipeCategory} to use.
     * @param tagKey   The {@link TagKey} to use.
     * @param fallback The fallback {@link ItemLike} to use.
     * @param count    The result count to use.
     * @return A new {@link ShapedRecipeBuilder}.
     */
    static ShapedRecipeBuilder shaped(HolderGetter<Item> items, RecipeCategory category, TagKey<Item> tagKey, ItemLike fallback, int count) {
        return shaped(items, category, new DefaultedItemTagOutgredient(tagKey, fallback.asItem().builtInRegistryHolder(), count));
    }

    /**
     * Creates a new {@link ShapedRecipeBuilder} using a generic {@link Outgredient} as the result.
     *
     * @param items    The item holder getter, usually the registry.
     * @param category The {@link RecipeCategory} to use.
     * @param result   The result {@link Outgredient} to use.
     * @return A new {@link ShapedRecipeBuilder}.
     */
    static ShapedRecipeBuilder shaped(HolderGetter<Item> items, RecipeCategory category, Outgredient<ItemStack> result) {
        return new ShapedRecipeBuilder(items, category, result);
    }
}
