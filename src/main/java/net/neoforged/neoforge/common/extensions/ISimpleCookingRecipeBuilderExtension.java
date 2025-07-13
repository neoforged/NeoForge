/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.outgredient.DefaultedItemTagOutgredient;
import net.neoforged.neoforge.common.crafting.outgredient.Outgredient;
import net.neoforged.neoforge.common.crafting.outgredient.OutgredientWrapper;

public interface ISimpleCookingRecipeBuilderExtension {
    /**
     * Creates a {@link SimpleCookingRecipeBuilder} for a generic cooking recipe using an {@link ItemStack} as the result.
     *
     * @param ingredient  The {@link Ingredient} to use.
     * @param category    The {@link RecipeCategory} to use.
     * @param result      The result {@link ItemStack} to use.
     * @param experience  The amount of experience to award for the recipe.
     * @param cookingTime The duration of the recipe.
     * @param serializer  The {@link RecipeSerializer} to use.
     * @param factory     The {@link AbstractCookingRecipe.Factory} to use.
     * @param <T>         The type of the recipe being built.
     * @return A {@link SimpleCookingRecipeBuilder}.
     */
    static <T extends AbstractCookingRecipe> SimpleCookingRecipeBuilder generic(Ingredient ingredient, RecipeCategory category, ItemStack result, float experience, int cookingTime, RecipeSerializer<T> serializer, AbstractCookingRecipe.Factory<T> factory) {
        return generic(ingredient, category, Outgredient.ofItem(result), experience, cookingTime, serializer, factory);
    }

    /**
     * Creates a {@link SimpleCookingRecipeBuilder} for a generic cooking recipe using a {@link TagKey} and {@link ItemLike} combination as the result.
     *
     * @param ingredient  The {@link Ingredient} to use.
     * @param category    The {@link RecipeCategory} to use.
     * @param tagKey      The {@link TagKey} to use.
     * @param fallback    The fallback {@link ItemLike} to use.
     * @param experience  The amount of experience to award for the recipe.
     * @param cookingTime The duration of the recipe.
     * @param serializer  The {@link RecipeSerializer} to use.
     * @param factory     The {@link AbstractCookingRecipe.Factory} to use.
     * @param <T>         The type of the recipe being built.
     * @return A {@link SimpleCookingRecipeBuilder}.
     */
    static <T extends AbstractCookingRecipe> SimpleCookingRecipeBuilder generic(Ingredient ingredient, RecipeCategory category, TagKey<Item> tagKey, ItemLike fallback, float experience, int cookingTime, RecipeSerializer<T> serializer, AbstractCookingRecipe.Factory<T> factory) {
        return generic(ingredient, category, new DefaultedItemTagOutgredient(tagKey, fallback.asItem().builtInRegistryHolder()), experience, cookingTime, serializer, factory);
    }

    /**
     * Creates a {@link SimpleCookingRecipeBuilder} for a generic cooking recipe using a generic {@link Outgredient} as the result.
     *
     * @param ingredient  The {@link Ingredient} to use.
     * @param category    The {@link RecipeCategory} to use.
     * @param result      The result {@link Outgredient} to use.
     * @param experience  The amount of experience to award for the recipe.
     * @param cookingTime The duration of the recipe.
     * @param serializer  The {@link RecipeSerializer} to use.
     * @param factory     The {@link AbstractCookingRecipe.Factory} to use.
     * @param <T>         The type of the recipe being built.
     * @return A {@link SimpleCookingRecipeBuilder}.
     */
    static <T extends AbstractCookingRecipe> SimpleCookingRecipeBuilder generic(Ingredient ingredient, RecipeCategory category, Outgredient<ItemStack> result, float experience, int cookingTime, RecipeSerializer<T> serializer, AbstractCookingRecipe.Factory<T> factory) {
        return new SimpleCookingRecipeBuilder(category, SimpleCookingRecipeBuilder.determineRecipeCategory(serializer, result.resolve().getItem()), result, ingredient, experience, cookingTime, factory);
    }

    /**
     * Creates a {@link SimpleCookingRecipeBuilder} for a campfire cooking recipe using an {@link ItemStack} as the result.
     *
     * @param ingredient  The {@link Ingredient} to use.
     * @param category    The {@link RecipeCategory} to use.
     * @param result      The result {@link ItemStack} to use.
     * @param experience  The amount of experience to award for the recipe.
     * @param cookingTime The duration of the recipe.
     * @return A {@link SimpleCookingRecipeBuilder}.
     */
    static SimpleCookingRecipeBuilder campfireCooking(Ingredient ingredient, RecipeCategory category, ItemStack result, float experience, int cookingTime) {
        return campfireCooking(ingredient, category, Outgredient.ofItem(result), experience, cookingTime);
    }

    /**
     * Creates a {@link SimpleCookingRecipeBuilder} for a campfire cooking recipe using a {@link TagKey} and {@link ItemLike} combination as the result.
     *
     * @param ingredient  The {@link Ingredient} to use.
     * @param category    The {@link RecipeCategory} to use.
     * @param fallback    The fallback {@link ItemLike} to use.
     * @param tagKey      The {@link TagKey} to use.
     * @param experience  The amount of experience to award for the recipe.
     * @param cookingTime The duration of the recipe.
     * @return A {@link SimpleCookingRecipeBuilder}.
     */
    static SimpleCookingRecipeBuilder campfireCooking(Ingredient ingredient, RecipeCategory category, TagKey<Item> tagKey, ItemLike fallback, float experience, int cookingTime) {
        return campfireCooking(ingredient, category, new DefaultedItemTagOutgredient(tagKey, fallback.asItem().builtInRegistryHolder()), experience, cookingTime);
    }

    /**
     * Creates a {@link SimpleCookingRecipeBuilder} for a campfire cooking recipe using a generic {@link Outgredient} as the result.
     *
     * @param ingredient  The {@link Ingredient} to use.
     * @param category    The {@link RecipeCategory} to use.
     * @param result      The result {@link Outgredient} to use.
     * @param experience  The amount of experience to award for the recipe.
     * @param cookingTime The duration of the recipe.
     * @return A {@link SimpleCookingRecipeBuilder}.
     */
    static SimpleCookingRecipeBuilder campfireCooking(Ingredient ingredient, RecipeCategory category, Outgredient<ItemStack> result, float experience, int cookingTime) {
        return new SimpleCookingRecipeBuilder(category, CookingBookCategory.FOOD, result, ingredient, experience, cookingTime, CampfireCookingRecipe::new);
    }

    /**
     * Creates a {@link SimpleCookingRecipeBuilder} for a blasting recipe using an {@link ItemStack} as the result.
     *
     * @param ingredient  The {@link Ingredient} to use.
     * @param category    The {@link RecipeCategory} to use.
     * @param result      The result {@link ItemStack} to use.
     * @param experience  The amount of experience to award for the recipe.
     * @param cookingTime The duration of the recipe.
     * @return A {@link SimpleCookingRecipeBuilder}.
     */
    static SimpleCookingRecipeBuilder blasting(Ingredient ingredient, RecipeCategory category, ItemStack result, float experience, int cookingTime) {
        return blasting(ingredient, category, Outgredient.ofItem(result), experience, cookingTime);
    }

    /**
     * Creates a {@link SimpleCookingRecipeBuilder} for a blasting recipe using a {@link TagKey} and {@link ItemLike} combination as the result.
     *
     * @param ingredient  The {@link Ingredient} to use.
     * @param category    The {@link RecipeCategory} to use.
     * @param fallback    The fallback {@link ItemLike} to use.
     * @param tagKey      The {@link TagKey} to use.
     * @param experience  The amount of experience to award for the recipe.
     * @param cookingTime The duration of the recipe.
     * @return A {@link SimpleCookingRecipeBuilder}.
     */
    static SimpleCookingRecipeBuilder blasting(Ingredient ingredient, RecipeCategory category, TagKey<Item> tagKey, ItemLike fallback, float experience, int cookingTime) {
        return blasting(ingredient, category, new DefaultedItemTagOutgredient(tagKey, fallback.asItem().builtInRegistryHolder()), experience, cookingTime);
    }

    /**
     * Creates a {@link SimpleCookingRecipeBuilder} for a blasting recipe using a generic {@link Outgredient} as the result.
     *
     * @param ingredient  The {@link Ingredient} to use.
     * @param category    The {@link RecipeCategory} to use.
     * @param result      The result {@link Outgredient} to use.
     * @param experience  The amount of experience to award for the recipe.
     * @param cookingTime The duration of the recipe.
     * @return A {@link SimpleCookingRecipeBuilder}.
     */
    static SimpleCookingRecipeBuilder blasting(Ingredient ingredient, RecipeCategory category, Outgredient<ItemStack> result, float experience, int cookingTime) {
        return new SimpleCookingRecipeBuilder(category, SimpleCookingRecipeBuilder.determineBlastingRecipeCategory(result.resolve().getItem()), result, ingredient, experience, cookingTime, BlastingRecipe::new);
    }

    /**
     * Creates a {@link SimpleCookingRecipeBuilder} for a smelting recipe using an {@link ItemStack} as the result.
     *
     * @param ingredient  The {@link Ingredient} to use.
     * @param category    The {@link RecipeCategory} to use.
     * @param result      The result {@link ItemStack} to use.
     * @param experience  The amount of experience to award for the recipe.
     * @param cookingTime The duration of the recipe.
     * @return A {@link SimpleCookingRecipeBuilder}.
     */
    static SimpleCookingRecipeBuilder smelting(Ingredient ingredient, RecipeCategory category, ItemStack result, float experience, int cookingTime) {
        return smelting(ingredient, category, Outgredient.ofItem(result), experience, cookingTime);
    }

    /**
     * Creates a {@link SimpleCookingRecipeBuilder} for a smelting recipe using a {@link TagKey} and {@link ItemLike} combination as the result.
     *
     * @param ingredient  The {@link Ingredient} to use.
     * @param category    The {@link RecipeCategory} to use.
     * @param fallback    The fallback {@link ItemLike} to use.
     * @param tagKey      The {@link TagKey} to use.
     * @param experience  The amount of experience to award for the recipe.
     * @param cookingTime The duration of the recipe.
     * @return A {@link SimpleCookingRecipeBuilder}.
     */
    static SimpleCookingRecipeBuilder smelting(Ingredient ingredient, RecipeCategory category, TagKey<Item> tagKey, ItemLike fallback, float experience, int cookingTime) {
        return smelting(ingredient, category, new DefaultedItemTagOutgredient(tagKey, fallback.asItem().builtInRegistryHolder()), experience, cookingTime);
    }

    /**
     * Creates a {@link SimpleCookingRecipeBuilder} for a smelting recipe using a generic {@link Outgredient} as the result.
     *
     * @param ingredient  The {@link Ingredient} to use.
     * @param category    The {@link RecipeCategory} to use.
     * @param result      The result {@link Outgredient} to use.
     * @param experience  The amount of experience to award for the recipe.
     * @param cookingTime The duration of the recipe.
     * @return A {@link SimpleCookingRecipeBuilder}.
     */
    static SimpleCookingRecipeBuilder smelting(Ingredient ingredient, RecipeCategory category, Outgredient<ItemStack> result, float experience, int cookingTime) {
        return new SimpleCookingRecipeBuilder(category, SimpleCookingRecipeBuilder.determineSmeltingRecipeCategory(result.resolve().getItem()), result, ingredient, experience, cookingTime, SmeltingRecipe::new);
    }

    /**
     * Creates a {@link SimpleCookingRecipeBuilder} for a smoking recipe using an {@link ItemStack} as the result.
     *
     * @param ingredient  The {@link Ingredient} to use.
     * @param category    The {@link RecipeCategory} to use.
     * @param result      The result {@link ItemStack} to use.
     * @param experience  The amount of experience to award for the recipe.
     * @param cookingTime The duration of the recipe.
     * @return A {@link SimpleCookingRecipeBuilder}.
     */
    static SimpleCookingRecipeBuilder smoking(Ingredient ingredient, RecipeCategory category, ItemStack result, float experience, int cookingTime) {
        return smoking(ingredient, category, Outgredient.ofItem(result), experience, cookingTime);
    }

    /**
     * Creates a {@link SimpleCookingRecipeBuilder} for a smoking recipe using a {@link TagKey} and {@link ItemLike} combination as the result.
     *
     * @param ingredient  The {@link Ingredient} to use.
     * @param category    The {@link RecipeCategory} to use.
     * @param fallback    The fallback {@link ItemLike} to use.
     * @param tagKey      The {@link TagKey} to use.
     * @param experience  The amount of experience to award for the recipe.
     * @param cookingTime The duration of the recipe.
     * @return A {@link SimpleCookingRecipeBuilder}.
     */
    static SimpleCookingRecipeBuilder smoking(Ingredient ingredient, RecipeCategory category, TagKey<Item> tagKey, ItemLike fallback, float experience, int cookingTime) {
        return smoking(ingredient, category, new DefaultedItemTagOutgredient(tagKey, fallback.asItem().builtInRegistryHolder()), experience, cookingTime);
    }

    /**
     * Creates a {@link SimpleCookingRecipeBuilder} for a smoking recipe using a generic {@link Outgredient} as the result.
     *
     * @param ingredient  The {@link Ingredient} to use.
     * @param category    The {@link RecipeCategory} to use.
     * @param result      The result {@link Outgredient} to use.
     * @param experience  The amount of experience to award for the recipe.
     * @param cookingTime The duration of the recipe.
     * @return A {@link SimpleCookingRecipeBuilder}.
     */
    static SimpleCookingRecipeBuilder smoking(Ingredient ingredient, RecipeCategory category, Outgredient<ItemStack> result, float experience, int cookingTime) {
        return new SimpleCookingRecipeBuilder(category, CookingBookCategory.FOOD, result, ingredient, experience, cookingTime, SmokingRecipe::new);
    }
}
