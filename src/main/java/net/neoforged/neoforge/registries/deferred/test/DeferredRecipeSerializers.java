/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Specialized DeferredRegister for {@link RecipeSerializer RecipeSerializers} that uses the specialized {@link DeferredRecipeSerializer} as the return type for {@link #register}.
 */
public class DeferredRecipeSerializers extends DeferredRegister<RecipeSerializer<?>> {
    protected DeferredRecipeSerializers(String namespace) {
        super(Registries.RECIPE_SERIALIZER, namespace);
    }

    @Override
    protected <TRecipeSerializer extends RecipeSerializer<?>> DeferredHolder<RecipeSerializer<?>, TRecipeSerializer> createHolder(ResourceKey<? extends Registry<RecipeSerializer<?>>> registryType, ResourceLocation registryName) {
        return (DeferredHolder<RecipeSerializer<?>, TRecipeSerializer>) DeferredRecipeSerializer.createRecipeSerializer(ResourceKey.create(registryType, registryName));
    }

    /**
     * Adds a new recipe serializer to the list of entries to be registered and returns a {@link DeferredRecipeSerializer} that will be populated with the created entry automatically.
     *
     * @param identifier  The new entry's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param codec       {@link MapCodec Codec} to be used during file serialization.
     * @param streamCodec {@link StreamCodec} to be used during network serialization.
     * @return A {@link DeferredRecipeSerializer} that will track updates from the registry for this entry.
     */
    public <TRecipe extends Recipe<?>> DeferredRecipeSerializer<TRecipe> registerRecipeSerializer(String identifier, MapCodec<TRecipe> codec, StreamCodec<RegistryFriendlyByteBuf, TRecipe> streamCodec) {
        return (DeferredRecipeSerializer<TRecipe>) this.<RecipeSerializer<TRecipe>>register(identifier, () -> new RecipeSerializer<>() {
            @Override
            public MapCodec<TRecipe> codec() {
                return codec;
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, TRecipe> streamCodec() {
                return streamCodec;
            }
        });
    }

    /**
     * Factory for a specialized DeferredRegister for {@link RecipeSerializer RecipeSerializers}.
     *
     * @param namespace The namespace for all objects registered to this DeferredRegister
     */
    public static DeferredRecipeSerializers createRecipeSerializers(String namespace) {
        return new DeferredRecipeSerializers(namespace);
    }
}
