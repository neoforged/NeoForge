/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

/// An interface used to merge two conflicting registry data map values attached to the same object.
///
/// Using a merger you can, for example, merge list data maps that come from different sources, when
/// otherwise the newest entry would win and override the older one.
///
/// @param <T> the data type
/// @param <R> the type of the registry this merger is for
/// @apiNote This is only useful for [AdvancedDataMapType].
@FunctionalInterface
public interface DataMapValueMerger<R, T> {
    /// Merge two conflicting data map values.
    ///
    /// @param registry    the registry the data is for
    /// @param first       the source of the first (older) value
    /// @param firstValue  the first (older) value
    /// @param second      the source of the second (newer) value
    /// @param secondValue the second (newer) value
    /// @return the merged value
    T merge(Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> first, T firstValue, Either<TagKey<R>, ResourceKey<R>> second, T secondValue);

    /// {@return a default merger that overrides the old value with the new one}
    static <T, R> DataMapValueMerger<R, T> defaultMerger() {
        return (_, _, _, _, secondValue) -> secondValue;
    }

    /// {@return a default merger that merges list data}
    static <T, R> DataMapValueMerger<R, List<T>> listMerger() {
        return (_, _, firstValue, _, secondValue) -> {
            final List<T> list = new ArrayList<>(firstValue);
            list.addAll(secondValue);
            return list;
        };
    }

    /// {@return a default merger that merges list data wrapped in another object}
    static <T, R, E> DataMapValueMerger<R, T> wrappedListMerger(Function<T, List<E>> unpacker, Function<List<E>, T> packer) {
        return (_, _, firstValue, _, secondValue) -> {
            final List<E> list = new ArrayList<>(unpacker.apply(firstValue));
            list.addAll(unpacker.apply(secondValue));
            return packer.apply(list);
        };
    }

    /// {@return a default merger that merges set data}
    static <T, R> DataMapValueMerger<R, Set<T>> setMerger() {
        return (_, _, firstValue, _, secondValue) -> {
            final Set<T> set = new HashSet<>(firstValue);
            set.addAll(secondValue);
            return set;
        };
    }

    /// {@return a default merger that merges set data wrapped in another object}
    static <T, R, E> DataMapValueMerger<R, T> wrappedSetMerger(Function<T, Set<E>> unpacker, Function<Set<E>, T> packer) {
        return (_, _, firstValue, _, secondValue) -> {
            final Set<E> set = new HashSet<>(unpacker.apply(firstValue));
            set.addAll(unpacker.apply(secondValue));
            return packer.apply(set);
        };
    }

    /// {@return a default merger that merges map data}
    static <K, V, R> DataMapValueMerger<R, Map<K, V>> mapMerger() {
        return (_, _, firstValue, _, secondValue) -> {
            final Map<K, V> map = new HashMap<>(firstValue);
            map.putAll(secondValue);
            return map;
        };
    }

    /// {@return a default merger that merges map data wrapped in another object}
    static <T, K, V, R> DataMapValueMerger<R, T> wrappedMapMerger(Function<T, Map<K, V>> unpacker, Function<Map<K, V>, T> packer) {
        return (_, _, firstValue, _, secondValue) -> {
            final Map<K, V> map = new HashMap<>(unpacker.apply(firstValue));
            map.putAll(unpacker.apply(secondValue));
            return packer.apply(map);
        };
    }
}
