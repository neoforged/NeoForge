/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.outgredient;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Function;

/**
 * Helper class for various outgredient-related hooks.
 */
public final class OutgredientCodecs {
    public static final Codec<Outgredient<ItemStack>> ITEM_STACK_OUTGREDIENT_CODEC = makeCodec(ItemStack.STRICT_CODEC, NeoForgeRegistries.ITEM_OUTGREDIENT_TYPES.byNameCodec(), Outgredient::ofItem);
    public static final Codec<Outgredient<FluidStack>> FLUID_STACK_OUTGREDIENT_CODEC = makeCodec(FluidStack.CODEC, NeoForgeRegistries.FLUID_OUTGREDIENT_TYPES.byNameCodec(), Outgredient::ofFluid);
    private static final int CUSTOM_OUTGREDIENT_MARKER = -1000;
    public static final StreamCodec<RegistryFriendlyByteBuf, Outgredient<ItemStack>> ITEM_STACK_OUTGREDIENT_STREAM_CODEC = makeStreamCodec(
            ItemStack.STREAM_CODEC,
            ByteBufCodecs.registry(NeoForgeRegistries.Keys.ITEM_OUTGREDIENT_TYPES).dispatch(Outgredient::type, OutgredientType::streamCodec),
            Outgredient::ofItem
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Outgredient<FluidStack>> FLUID_STACK_OUTGREDIENT_STREAM_CODEC = makeStreamCodec(
            FluidStack.STREAM_CODEC,
            ByteBufCodecs.registry(NeoForgeRegistries.Keys.FLUID_OUTGREDIENT_TYPES).dispatch(Outgredient::type, OutgredientType::streamCodec),
            Outgredient::ofFluid
    );

    /**
     * Creates a new codec for {@link Outgredient}s.
     *
     * @param vanillaCodec  The codec to use for vanilla values.
     * @param registryCodec The {@link Outgredient} registry codec to use for non-vanilla values.
     * @param toOutgredient A {@code T} -> {@code Outgredient<T>} converter.
     * @return A codec for {@link Outgredient}s.
     * @param <T> The {@link Outgredient}'s generic type.
     * @see net.neoforged.neoforge.common.crafting.IngredientCodecs#codec(Codec)
     */
    public static <T> Codec<Outgredient<T>> makeCodec(Codec<T> vanillaCodec, Codec<OutgredientType<? extends Outgredient<T>>> registryCodec, Function<T, ? extends Outgredient<T>> toOutgredient) {
        return Codec.xor(vanillaCodec, registryCodec.<Outgredient<T>>dispatch("neoforge:outgredient_type", Outgredient::type, OutgredientType::codec)).xmap(
                either -> Either.unwrap(either.mapLeft(toOutgredient)),
                outgredient -> outgredient instanceof OutgredientWrapper<T> ? Either.left(outgredient.resolve()) : Either.right(outgredient)
        );
    }

    /**
     * Creates a new stream codec for {@link Outgredient}s.
     *
     * @param vanillaCodec     The stream codec to use for vanilla values.
     * @param outgredientCodec The {@link Outgredient} stream codec to use for non-vanilla values.
     * @param toOutgredient    A {@code T} -> {@code Outgredient<T>} converter.
     * @return A stream codec for {@link Outgredient}s.
     * @param <T> The {@link Outgredient}'s generic type.
     * @see net.neoforged.neoforge.common.crafting.IngredientCodecs#streamCodec(StreamCodec)
     */
    public static <T> StreamCodec<RegistryFriendlyByteBuf, Outgredient<T>> makeStreamCodec(
            StreamCodec<RegistryFriendlyByteBuf, T> vanillaCodec,
            StreamCodec<RegistryFriendlyByteBuf, Outgredient<T>> outgredientCodec,
            Function<T, ? extends Outgredient<T>> toOutgredient
    ) {
        return new StreamCodec<>() {
            @Override
            public Outgredient<T> decode(RegistryFriendlyByteBuf buf) {
                int readerIndex = buf.readerIndex();
                int length = buf.readVarInt();
                if (length == CUSTOM_OUTGREDIENT_MARKER) {
                    return outgredientCodec.decode(buf);
                } else {
                    buf.readerIndex(readerIndex);
                    return toOutgredient.apply(vanillaCodec.decode(buf));
                }
            };

            @Override
            public void encode(RegistryFriendlyByteBuf buf, Outgredient<T> outgredient) {
                if (!(outgredient instanceof OutgredientWrapper<T>) && buf.getConnectionType().isNeoForge()) {
                    buf.writeVarInt(CUSTOM_OUTGREDIENT_MARKER);
                    outgredientCodec.encode(buf, outgredient);
                } else {
                    vanillaCodec.encode(buf, outgredient.resolve());
                }
            }
        };
    }

    private OutgredientCodecs() {
    }
}
