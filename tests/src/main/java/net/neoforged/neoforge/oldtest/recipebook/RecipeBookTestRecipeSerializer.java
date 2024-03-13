/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.recipebook;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.oldtest.recipebook.RecipeBookTestRecipe.Ingredients;

public class RecipeBookTestRecipeSerializer implements RecipeSerializer<RecipeBookTestRecipe> {
    private static final Codec<RecipeBookTestRecipe> CODEC = Ingredients.CODEC.xmap(RecipeBookTestRecipe::new, recipeBookTestRecipe -> recipeBookTestRecipe.ingredients);
    private static final StreamCodec<RegistryFriendlyByteBuf, RecipeBookTestRecipe> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    @Override
    public Codec<RecipeBookTestRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RecipeBookTestRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
