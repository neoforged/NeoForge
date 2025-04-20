/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class FluidIngredientCodecs {
    static Codec<FluidIngredient> codec() {
        return Codec.xor(
                NeoForgeRegistries.FLUID_INGREDIENT_TYPES.byNameCodec().<FluidIngredient>dispatch("neoforge:ingredient_type", FluidIngredient::getType, FluidIngredientType::codec),
                // Use lazy: SimpleFluidIngredient.CODEC is initialized after FluidIngredient.CODEC which lives in a superclass.
                Codec.lazyInitialized(() -> SimpleFluidIngredient.CODEC))
                .xmap(either -> either.map(i -> i, i -> i), ingredient -> switch (ingredient) {
                    case SimpleFluidIngredient simple -> Either.right(simple);
                    default -> Either.left(ingredient);
                });
    }

    static StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> streamCodec() {
        return ByteBufCodecs.registry(NeoForgeRegistries.Keys.FLUID_INGREDIENT_TYPES)
                .dispatch(FluidIngredient::getType, FluidIngredientType::streamCodec);
    }

    public static FluidIngredientType<SimpleFluidIngredient> simpleType() {
        MapCodec<SimpleFluidIngredient> erroringMapCodec = new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
                return Stream.empty();
            }

            @Override
            public <T> DataResult<SimpleFluidIngredient> decode(DynamicOps<T> ops, MapLike<T> mapLike) {
                return DataResult.error(() -> "Simple fluid ingredients cannot be decoded using map syntax!");
            }

            @Override
            public <T> RecordBuilder<T> encode(SimpleFluidIngredient ingredient, DynamicOps<T> ops, RecordBuilder<T> builder) {
                return builder.withErrorsFrom(DataResult.error(() -> "Simple fluid ingredients cannot be encoded using map syntax! Please use vanilla syntax (namespaced:item or #tag) instead!"));
            }
        };

        return new FluidIngredientType<>(erroringMapCodec, SimpleFluidIngredient.CONTENTS_STREAM_CODEC);
    }
}
