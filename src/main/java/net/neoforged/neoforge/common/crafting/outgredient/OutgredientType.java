/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.outgredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;

/**
 * An outgredient type encapsulates the codecs to serialize and deserialize a custom outgredient.
 *
 * <p>Note that the {@link #streamCodec()} is only used if {@link ICustomIngredient#isSimple()} returns {@code false}.
 */
public record OutgredientType<T extends Outgredient<?>>(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
    /**
     * Constructor for outgredient types that use a regular codec for network syncing.
     */
    public OutgredientType(MapCodec<T> codec) {
        this(codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec()));
    }
}
