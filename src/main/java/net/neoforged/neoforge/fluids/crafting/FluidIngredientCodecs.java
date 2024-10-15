/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

class FluidIngredientCodecs {
    static Codec<FluidIngredient> codec() {
        return Codec.xor(
                NeoForgeRegistries.FLUID_INGREDIENT_TYPES.byNameCodec().<FluidIngredient>dispatch("neoforge:ingredient_type", FluidIngredient::getType, FluidIngredientType::codec),
                SimpleFluidIngredient.CODEC).xmap(either -> either.map(i -> i, i -> i), ingredient -> switch (ingredient) {
                    case SimpleFluidIngredient simple -> Either.right(simple);
                    default -> Either.left(ingredient);
                });
    }

    static StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> streamCodec() {
        return ByteBufCodecs.registry(NeoForgeRegistries.Keys.FLUID_INGREDIENT_TYPES)
                .dispatch(FluidIngredient::getType, FluidIngredientType::streamCodec);
    }
}
