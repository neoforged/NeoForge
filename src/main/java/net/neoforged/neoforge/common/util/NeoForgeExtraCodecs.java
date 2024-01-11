/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;

/**
 * {@link Codec}-related helper functions that are not in {@link ExtraCodecs}, but useful to NeoForge and other mods.
 * 
 * @see ExtraCodecs
 */
public class NeoForgeExtraCodecs {

    public static <T> MapCodec<T> aliasedFieldOf(final Codec<T> codec, final String... names) {
        if (names.length == 0)
            throw new IllegalArgumentException("Must have at least one name!");
        MapCodec<T> mapCodec = codec.fieldOf(names[0]);
        for (int i = 1; i < names.length; i++)
            mapCodec = mapWithAlternative(mapCodec, codec.fieldOf(names[i]));
        return mapCodec;
    }

    public static <T> MapCodec<T> mapWithAlternative(final MapCodec<T> mapCodec, final MapCodec<? extends T> alternative) {
        return Codec.mapEither(mapCodec, alternative).xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);
    }

    public static <T> MapCodec<Set<T>> singularOrPluralCodec(final Codec<T> codec, final String singularName) {
        return singularOrPluralCodec(codec, singularName, "%ss".formatted(singularName));
    }

    public static <T> MapCodec<Set<T>> singularOrPluralCodec(final Codec<T> codec, final String singularName, final String pluralName) {
        return Codec.mapEither(codec.fieldOf(singularName), setOf(codec).fieldOf(pluralName)).xmap(
                either -> either.map(ImmutableSet::of, ImmutableSet::copyOf),
                set -> set.size() == 1 ? Either.left(set.iterator().next()) : Either.right(set));
    }

    public static <T> MapCodec<Set<T>> singularOrPluralCodecNotEmpty(final Codec<T> codec, final String singularName) {
        return singularOrPluralCodecNotEmpty(codec, singularName, "%ss".formatted(singularName));
    }

    public static <T> MapCodec<Set<T>> singularOrPluralCodecNotEmpty(final Codec<T> codec, final String singularName, final String pluralName) {
        return Codec.mapEither(codec.fieldOf(singularName), setOf(codec).fieldOf(pluralName)).xmap(
                either -> either.map(ImmutableSet::of, ImmutableSet::copyOf),
                set -> set.size() == 1 ? Either.left(set.iterator().next()) : Either.right(set)).flatXmap(ts -> {
                    if (ts.isEmpty())
                        return DataResult.error(() -> "The set for: %s can not be empty!".formatted(singularName));
                    return DataResult.success(ts);
                }, ts -> {
                    if (ts.isEmpty())
                        return DataResult.error(() -> "The set for: %s can not be empty!".formatted(singularName));
                    return DataResult.success(ImmutableSet.copyOf(ts));
                });
    }

    public static <T> Codec<Set<T>> setOf(final Codec<T> codec) {
        return Codec.list(codec).xmap(ImmutableSet::copyOf, ImmutableList::copyOf);
    }

    /**
     * Creates a codec from a decoder.
     * The returned codec can only decode, and will throw on any attempt to encode.
     */
    public static <A> Codec<A> decodeOnly(Decoder<A> decoder) {
        return Codec.of(Codec.unit(() -> {
            throw new UnsupportedOperationException("Cannot encode with decode-only codec! Decoder:" + decoder);
        }), decoder, "DecodeOnly[" + decoder + "]");
    }

    /**
     * Creates a codec for a list from a codec of optional elements.
     * The empty optionals are removed from the list when decoding.
     */
    public static <A> Codec<List<A>> listWithOptionalElements(Codec<Optional<A>> elementCodec) {
        return listWithoutEmpty(elementCodec.listOf());
    }

    /**
     * Creates a codec for a list of optional elements, that removes empty values when decoding.
     */
    public static <A> Codec<List<A>> listWithoutEmpty(Codec<List<Optional<A>>> codec) {
        return codec.xmap(
                list -> list.stream().filter(Optional::isPresent).map(Optional::get).toList(),
                list -> list.stream().map(Optional::of).toList());
    }

    /**
     * Codec with two alternatives.
     * <p>
     * The vanilla {@link ExtraCodecs#withAlternative(Codec, Codec)} will try
     * the first codec and then the second codec for decoding, <b>but only the first for encoding</b>.
     * <p>
     * Unlike vanilla, this alternative codec also tries to encode with the second codec if the first encode fails.
     * <p>
     * This codec will also try to keep the resulting codec to a MapCodecCodec if both inputs are MapCodecCodecs.
     */
    public static <T> Codec<T> withAlternative(final Codec<T> codec, final Codec<T> alternative) {
        if (codec instanceof MapCodec.MapCodecCodec<T> mCodec && alternative instanceof MapCodec.MapCodecCodec<T> mAlternative) {
            //If both codecs are MapCodecCodecs then return it as a MapCodecCodec to allow for cleaner support in dispatch codecs
            return withAlternative(mCodec.codec(), mAlternative.codec()).codec();
        }
        return new AlternativeCodec<>(codec, alternative);
    }

    /**
     * MapCodec with two alternatives.
     * <p>
     * {@link #mapWithAlternative(MapCodec, MapCodec)} will try the first codec and then the second codec for decoding, <b>but only the first for encoding</b>.
     * <p>
     * Unlike {@link #mapWithAlternative(MapCodec, MapCodec)}, this alternative codec also tries to encode with the second codec if the first encode fails.
     */
    public static <T> MapCodec<T> withAlternative(final MapCodec<T> codec, final MapCodec<T> alternative) {
        return new AlternativeMapCodec<>(codec, alternative);
    }

    private record AlternativeCodec<T>(Codec<T> codec, Codec<T> alternative) implements Codec<T> {
        @Override
        public <T1> DataResult<Pair<T, T1>> decode(final DynamicOps<T1> ops, final T1 input) {
            final DataResult<Pair<T, T1>> result = codec.decode(ops, input);
            if (result.error().isEmpty()) {
                return result;
            } else {
                return alternative.decode(ops, input);
            }
        }

        @Override
        public <T1> DataResult<T1> encode(final T input, final DynamicOps<T1> ops, final T1 prefix) {
            final DataResult<T1> result = codec.encode(input, ops, prefix);
            if (result.error().isEmpty()) {
                return result;
            } else {
                return alternative.encode(input, ops, prefix);
            }
        }

        @Override
        public String toString() {
            return "Alternative[" + codec + ", " + alternative + "]";
        }
    }

    private static class AlternativeMapCodec<T> extends MapCodec<T> {

        private final MapCodec<T> codec;
        private final MapCodec<T> alternative;

        private AlternativeMapCodec(MapCodec<T> codec, MapCodec<T> alternative) {
            this.codec = codec;
            this.alternative = alternative;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.concat(codec.keys(ops), alternative.keys(ops)).distinct();
        }

        @Override
        public <T1> DataResult<T> decode(DynamicOps<T1> ops, MapLike<T1> input) {
            DataResult<T> result = codec.decode(ops, input);
            if (result.error().isEmpty()) {
                return result;
            }
            return alternative.decode(ops, input);
        }

        @Override
        public <T1> RecordBuilder<T1> encode(T input, DynamicOps<T1> ops, RecordBuilder<T1> prefix) {
            //Build it to see if there is an error
            DataResult<T1> result = codec.encode(input, ops, prefix).build(ops.empty());
            if (result.error().isEmpty()) {
                //But then we even if there isn't we have to encode it again so that we can actually allow the building to apply
                // as our earlier build consumes the result
                return codec.encode(input, ops, prefix);
            }
            return alternative.encode(input, ops, prefix);
        }

        @Override
        public String toString() {
            return "AlternativeMapCodec[" + codec + ", " + alternative + "]";
        }
    }
}
