/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * An ingredient type encapsulates the codecs to serialize and deserialize a custom ingredient.
 *
 * <p>Note that the {@link #streamCodec()} is only used if {@link ICustomIngredient#isSimple()} returns {@code false}.
 */
public record IngredientType<T extends ICustomIngredient>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
    /**
     * Constructor for ingredient types that use a regular codec for network syncing.
     */
    public IngredientType(MapCodec<T> codec) {
        this(codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec()));
    }
}
