/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Special {@link DeferredHolder} for {@link RecipeSerializer RecipeSerializers}.
 *
 * @param <TRecipe> The specific {@link RecipeSerializer}.
 */
public class DeferredRecipeSerializer<TRecipe extends Recipe<?>> extends DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TRecipe>> {
    protected DeferredRecipeSerializer(ResourceKey<RecipeSerializer<?>> key) {
        super(key);
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the specified {@link RecipeSerializer}.
     *
     * @param <TRecipe>   The type of the target {@link RecipeSerializer}.
     * @param registryKey The resource key of the target {@link RecipeSerializer}.
     */
    public static <TRecipe extends Recipe<?>> DeferredRecipeSerializer<TRecipe> createRecipeSerializer(ResourceKey<RecipeSerializer<?>> registryKey) {
        return new DeferredRecipeSerializer<>(registryKey);
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the {@link RecipeSerializer} with the specified name.
     *
     * @param <TRecipe>    The type of the target {@link RecipeSerializer}.
     * @param registryName The name of the target {@link RecipeSerializer}.
     */
    public static <TRecipe extends Recipe<?>> DeferredRecipeSerializer<TRecipe> createRecipeSerializer(ResourceLocation registryName) {
        return createRecipeSerializer(ResourceKey.create(Registries.RECIPE_SERIALIZER, registryName));
    }
}
