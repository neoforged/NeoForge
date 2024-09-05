/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Special {@link DeferredHolder} for {@link RecipeType RecipeTypes}.
 *
 * @param <TRecipe> The specific {@link RecipeType}.
 */
public class DeferredRecipeType<TRecipe extends Recipe<?>> extends DeferredHolder<RecipeType<?>, RecipeType<TRecipe>> {
    protected DeferredRecipeType(ResourceKey<RecipeType<?>> key) {
        super(key);
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the specified {@link RecipeType}.
     *
     * @param <TRecipe>   The type of the target {@link RecipeType}.
     * @param registryKey The resource key of the target {@link RecipeType}.
     */
    public static <TRecipe extends Recipe<?>> DeferredRecipeType<TRecipe> createRecipeType(ResourceKey<RecipeType<?>> registryKey) {
        return new DeferredRecipeType<>(registryKey);
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the {@link RecipeType} with the specified name.
     *
     * @param <TRecipe>    The type of the target {@link RecipeType}.
     * @param registryName The name of the target {@link RecipeType}.
     */
    public static <TRecipe extends Recipe<?>> DeferredRecipeType<TRecipe> createRecipeType(ResourceLocation registryName) {
        return createRecipeType(ResourceKey.create(Registries.RECIPE_TYPE, registryName));
    }
}
