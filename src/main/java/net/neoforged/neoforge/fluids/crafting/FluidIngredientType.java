/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.crafting.IngredientType;

/**
 * This represents the "type" of a {@link FluidIngredient}, providing means of serializing
 * and deserializing the ingredient over both JSON and network, using the {@link #codec}
 * and {@link #streamCodec}, respectively.
 * <p>
 * Note that the {@link #streamCodec()} is only used if {@link FluidIngredient#isSimple()} returns {@code false},
 * as otherwise its contents are synchronized directly to the network.
 *
 * @param <T> The type of fluid ingredient
 * @see IngredientType IngredientType, a similar class for custom item ingredients
 */
public record FluidIngredientType<T extends FluidIngredient>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
    public FluidIngredientType(MapCodec<T> mapCodec) {
        this(mapCodec, ByteBufCodecs.fromCodecWithRegistries(mapCodec.codec()));
    }
}
