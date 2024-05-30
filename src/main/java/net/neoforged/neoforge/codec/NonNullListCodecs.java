/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.codec;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;

public class NonNullListCodecs {
    /**
     * Neo: utility method to construct a Codec for a NonNullList
     *
     * @param entryCodec the codec to use for the elements
     * @param <E>        the element type
     * @return a codec that encodes as a list, and decodes into NonNullList
     */
    public static <E> com.mojang.serialization.Codec<NonNullList<E>> of(com.mojang.serialization.Codec<E> entryCodec) {
        return entryCodec.listOf().xmap(NonNullList::copyOf, java.util.function.Function.identity());
    }

    public static <T> Codec<NonNullList<T>> withIndices(Codec<T> elementCodec, T defaultValue, String indexKey) {
        return withIndices(elementCodec, defaultValue, indexKey, Predicates.alwaysFalse());
    }

    public static <T> Codec<NonNullList<T>> withIndices(Codec<T> elementCodec, T defaultValue, String indexKey, Predicate<T> skipPredicate) {
        final var asMapCodec = MapCodec.assumeMapUnsafe(elementCodec);
        return new IndexedNonNullListCodec<T>(asMapCodec, defaultValue, skipPredicate, indexKey);
    }
}
