/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

public class ConditionalRecipe<T extends Recipe<?>> implements RecipeSerializer<T> {
    public static final Codec<Recipe<?>> CONDITIONAL_RECIPES_CODEC = NeoForgeExtraCodecs.CONDITIONAL_RECIPE_CODEC.listOf().fieldOf("recipes").codec()
            .xmap(optionals -> optionals.stream().filter(Optional::isPresent).findFirst().flatMap(Function.identity()).<Recipe<?>>map(WithConditions::carrier).orElse(CraftingHelper.EMPTY_RECIPE),
                    r -> List.of(Optional.of(new WithConditions<>(r))));

    @Override
    public Codec<T> codec() {
        return (Codec<T>) CONDITIONAL_RECIPES_CODEC;
    }

    // Should never get here as it's a wrapper
    @Override
    public T fromNetwork(FriendlyByteBuf p_44106_) {
        throw new UnsupportedOperationException("Attempted to read conditional recipe from network; this is a wrapper class!");
    }

    @Override
    public void toNetwork(FriendlyByteBuf p_44101_, T p_44102_) {

    }
}
