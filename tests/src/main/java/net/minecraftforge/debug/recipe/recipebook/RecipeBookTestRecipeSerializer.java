/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.debug.recipe.recipebook;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.debug.recipe.recipebook.RecipeBookTestRecipe.Ingredients;
import org.jetbrains.annotations.Nullable;

public class RecipeBookTestRecipeSerializer implements RecipeSerializer<RecipeBookTestRecipe>
{
    private static final Codec<RecipeBookTestRecipe> CODEC = Ingredients.CODEC.xmap(RecipeBookTestRecipe::new, recipeBookTestRecipe -> recipeBookTestRecipe.ingredients);

    @Override
    public Codec<RecipeBookTestRecipe> codec()
    {
        return CODEC;
    }

    @Override
    public @Nullable RecipeBookTestRecipe fromNetwork(FriendlyByteBuf buf)
    {
        Ingredients ingredients = buf.readWithCodecTrusted(NbtOps.INSTANCE, Ingredients.CODEC);
        return new RecipeBookTestRecipe(ingredients);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RecipeBookTestRecipe recipe)
    {
        buffer.writeWithCodec(NbtOps.INSTANCE, Ingredients.CODEC, recipe.ingredients);
    }
}
