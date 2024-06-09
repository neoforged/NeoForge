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
import net.minecraft.world.item.ItemStack;

public class NonNullListCodecs {
    /**
     * Neo: utility method to construct a Codec for a NonNullList
     *
     * @param entryCodec the codec to use for the elements
     * @param <E>        the element type
     * @return a codec that encodes as a list, and decodes into NonNullList
     */
    public static <E> Codec<NonNullList<E>> of(Codec<E> entryCodec) {
        return entryCodec.listOf().xmap(NonNullList::copyOf, java.util.function.Function.identity());
    }

    /**
     * Creates a codec for a non-null list that preserves index information.
     *
     * @param elementCodec  Codec for the elements of the list
     * @param defaultValue  A value to use for "filling" the default values of the list
     * @param indexKey      The field name for the index to be stored in
     *
     * @return A non-null list codec for type T
     * @param <T> Type information for the elements in the list
     */
    public static <T> Codec<NonNullList<T>> withIndices(Codec<T> elementCodec, T defaultValue, String indexKey) {
        return withIndices(elementCodec, defaultValue, indexKey, Predicates.alwaysFalse());
    }

    /**
     * Creates a codec for a non-null list that preserves index information.
     *
     * @param elementCodec  Codec for the elements of the list
     * @param defaultValue  A value to use for "filling" the default values of the list
     * @param indexKey      The field name for the index to be stored in
     * @param skipPredicate A predicate used for filtering out "default" or "empty" values
     *
     * @return A non-null list codec for type T
     * @param <T> Type information for the elements in the list
     */
    public static <T> Codec<NonNullList<T>> withIndices(Codec<T> elementCodec, T defaultValue, String indexKey, Predicate<T> skipPredicate) {
        final var asMapCodec = MapCodec.assumeMapUnsafe(elementCodec);
        return new IndexedNonNullListCodec<T>(asMapCodec, defaultValue, skipPredicate, indexKey);
    }

    /**
     * Creates a non-null list codec for item stacks, serializing slot information to "slot" and using the isEmpty check
     * to filter out empty slots.
     *
     * @return A non-null list codec for item stacks.
     */
    public static Codec<NonNullList<ItemStack>> optionalItems() {
        return optionalItems("slot");
    }

    /**
     * Creates a non-null list codec for item stacks, serializing slot information to a specified index key and using
     * the isEmpty check to filter out empty slots.
     *
     * @return A non-null list codec for item stacks.
     */
    public static Codec<NonNullList<ItemStack>> optionalItems(String indexKey) {
        return withIndices(ItemStack.OPTIONAL_CODEC, ItemStack.EMPTY, indexKey, ItemStack::isEmpty);
    }
}
