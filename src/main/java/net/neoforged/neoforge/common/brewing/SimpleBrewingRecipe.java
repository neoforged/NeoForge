/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.brewing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.NeoForgeMod;

public record SimpleBrewingRecipe(Ingredient input, Ingredient catalyst, ItemStack output) implements IBrewingRecipe {

    @Override
    public RecipeSerializer<SimpleBrewingRecipe> getSerializer() {
        return NeoForgeMod.SIMPLE_BREWING_RECIPE_SERIALIZER.get();
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return this.input().test(stack);
    }

    @Override
    public boolean isCatalyst(ItemStack catalyst) {
        return this.catalyst().test(catalyst);
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack catalyst) {
        return isInput(input) && isCatalyst(catalyst) ? output().copy() : ItemStack.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<SimpleBrewingRecipe> {
        private static final Codec<SimpleBrewingRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(SimpleBrewingRecipe::input),
                Ingredient.CODEC_NONEMPTY.fieldOf("catalyst").forGetter(SimpleBrewingRecipe::catalyst),
                ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("output").forGetter(SimpleBrewingRecipe::output)).apply(inst, SimpleBrewingRecipe::new));

        @Override
        public Codec<SimpleBrewingRecipe> codec() {
            return CODEC;
        }

        @Override
        public SimpleBrewingRecipe fromNetwork(FriendlyByteBuf buf) {
            return new SimpleBrewingRecipe(
                    Ingredient.fromNetwork(buf),
                    Ingredient.fromNetwork(buf),
                    buf.readItem());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, SimpleBrewingRecipe recipe) {
            recipe.input().toNetwork(buf);
            recipe.catalyst().toNetwork(buf);
            buf.writeItem(recipe.output());
        }
    }
}
