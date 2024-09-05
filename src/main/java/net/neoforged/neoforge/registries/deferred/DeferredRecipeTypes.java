/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Specialized DeferredRegister for {@link RecipeType RecipeTypes} that uses the specialized {@link DeferredRecipeType} as the return type for {@link #register}.
 */
public class DeferredRecipeTypes extends DeferredRegister<RecipeType<?>> {
    protected DeferredRecipeTypes(String namespace) {
        super(Registries.RECIPE_TYPE, namespace);
    }

    @Override
    protected <TRecipeType extends RecipeType<?>> DeferredHolder<RecipeType<?>, TRecipeType> createHolder(ResourceKey<? extends Registry<RecipeType<?>>> registryType, ResourceLocation registryName) {
        return (DeferredHolder<RecipeType<?>, TRecipeType>) DeferredRecipeType.createRecipeType(ResourceKey.create(registryType, registryName));
    }

    /**
     * Adds a new recipe type to the list of entries to be registered and returns a {@link DeferredRecipeType} that will be populated with the created entry automatically.
     *
     * @param identifier The new entry's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @return A {@link DeferredRecipeType} that will track updates from the registry for this entry.
     */
    public <TRecipe extends Recipe<?>> DeferredRecipeType<TRecipe> registerRecipeType(String identifier) {
        return (DeferredRecipeType<TRecipe>) this.<RecipeType<TRecipe>>register(identifier, RecipeType::simple);
    }

    /**
     * Factory for a specialized DeferredRegister for {@link RecipeType RecipeTypes}.
     *
     * @param namespace The namespace for all objects registered to this DeferredRegister
     */
    public static DeferredRecipeTypes createRecipeTypes(String namespace) {
        return new DeferredRecipeTypes(namespace);
    }
}
