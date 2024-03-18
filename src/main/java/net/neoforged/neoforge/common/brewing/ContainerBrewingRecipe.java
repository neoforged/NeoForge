/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.brewing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

public record ContainerBrewingRecipe(Ingredient input, Ingredient catalyst, ItemStack output) implements IBrewingRecipe {

    @Override
    public RecipeSerializer<ContainerBrewingRecipe> getSerializer() {
        return NeoForgeMod.CONTAINER_BREWING_RECIPE_SERIALIZER.value();
    }

    @Override
    public boolean isInput(ItemStack input) {
        return this.input().test(input);
    }

    @Override
    public boolean isCatalyst(ItemStack catalyst) {
        return this.catalyst().test(catalyst);
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack catalyst) {
        return isInput(input) && isCatalyst(catalyst) ? makeOutput(input) : ItemStack.EMPTY;
    }

    private ItemStack makeOutput(ItemStack input) {
        ItemStack stack = this.output();
        PotionUtils.setPotion(stack, PotionUtils.getPotion(input));
        PotionUtils.setCustomEffects(stack, PotionUtils.getCustomEffects(input));
        return stack;
    }
    public static class Serializer implements RecipeSerializer<ContainerBrewingRecipe> {
        private static final Codec<ContainerBrewingRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(ContainerBrewingRecipe::input),
                Ingredient.CODEC_NONEMPTY.fieldOf("catalyst").forGetter(ContainerBrewingRecipe::catalyst),
                NeoForgeExtraCodecs.withAlternative(BuiltInRegistries.ITEM.byNameCodec().xmap(ItemStack::new, ItemStack::getItem), ItemStack.ITEM_WITH_COUNT_CODEC).fieldOf("output").forGetter(ContainerBrewingRecipe::output)).apply(inst, ContainerBrewingRecipe::new));

        @Override
        public Codec<ContainerBrewingRecipe> codec() {
            return CODEC;
        }

        @Override
        public ContainerBrewingRecipe fromNetwork(FriendlyByteBuf buf) {
            return new ContainerBrewingRecipe(
                    Ingredient.fromNetwork(buf),
                    Ingredient.fromNetwork(buf),
                    buf.readItem());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ContainerBrewingRecipe recipe) {
            recipe.input().toNetwork(buf);
            recipe.catalyst().toNetwork(buf);
            buf.writeItem(recipe.output());
        }
    }
}
