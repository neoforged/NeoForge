/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT license.
 */

package net.neoforged.neoforge.common;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.BaseMapCodec;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Objects;

/**
 * Key and value decoded independently, unknown set of keys
 */
public class LenientUnboundedMapCodec<K, V> implements BaseMapCodec<K, V>, Codec<Map<K, V>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Codec<K> keyCodec;
    private final Codec<V> elementCodec;

    public LenientUnboundedMapCodec(final Codec<K> keyCodec, final Codec<V> elementCodec) {
        this.keyCodec = keyCodec;
        this.elementCodec = elementCodec;
    }

    @Override
    public Codec<K> keyCodec() {
        return keyCodec;
    }

    @Override
    public Codec<V> elementCodec() {
        return elementCodec;
    }

    @Override // FORGE: Modified from decode() in BaseMapCodec
    public <T> DataResult<Map<K, V>> decode(DynamicOps<T> ops, MapLike<T> input) {
        final ImmutableMap.Builder<K, V> read = ImmutableMap.builder();

        input.entries().forEach(pair -> {
            final DataResult<K> k = keyCodec().parse(ops, pair.getFirst());
            final DataResult<V> v = elementCodec().parse(ops, pair.getSecond());

            final DataResult<Pair<K, V>> entry = k.apply2stable(Pair::of, v);
            entry.error().ifPresent(e -> LOGGER.error("Failed to decode key {} for value {}: {}", k, v, e));
            entry.result().ifPresent(e -> read.put(e.getFirst(), e.getSecond()));
        });

        final Map<K, V> elements = read.build();

        return DataResult.success(elements);
    }

    @Override
    public <T> DataResult<Pair<Map<K, V>, T>> decode(final DynamicOps<T> ops, final T input) {
        return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> decode(ops, map)).map(r -> Pair.of(r, input));
    }

    @Override
    public <T> DataResult<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final T prefix) {
        return encode(input, ops, ops.mapBuilder()).build(prefix);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final LenientUnboundedMapCodec<?, ?> that = (LenientUnboundedMapCodec<?, ?>) o;
        return Objects.equals(keyCodec, that.keyCodec) && Objects.equals(elementCodec, that.elementCodec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyCodec, elementCodec);
    }

    @Override
    public String toString() {
        return "LenientUnboundedMapCodec[" + keyCodec + " -> " + elementCodec + ']';
    }
}
