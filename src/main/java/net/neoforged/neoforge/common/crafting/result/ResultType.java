/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.result;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;

/**
 * A result type encapsulates the codecs to serialize and deserialize a custom result.
 *
 * <p>Note that the {@link #streamCodec()} is only used if {@link ICustomIngredient#isSimple()} returns {@code false}.
 */
public record ResultType<T extends Result<?>>(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
    /**
     * Constructor for result types that use a regular codec for network syncing.
     */
    public ResultType(MapCodec<T> codec) {
        this(codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec()));
    }
}
