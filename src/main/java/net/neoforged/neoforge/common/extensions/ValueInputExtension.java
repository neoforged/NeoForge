/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * Extension class for {@link ValueInput}
 */
public interface ValueInputExtension {
    MapCodec<Set<String>> EXTRACT_KEYS = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.empty();
        }

        @Override
        public <T> DataResult<Set<String>> decode(DynamicOps<T> ops, MapLike<T> input) {
            return DataResult.success(input.entries().map(entry -> ops.getStringValue(entry.getFirst()).getOrThrow()).collect(Collectors.toSet()));
        }

        @Override
        public <T> RecordBuilder<T> encode(Set<String> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return prefix;
        }
    };

    private ValueInput self() {
        return (ValueInput) this;
    }

    /**
     * {@return the top-level keys of this object}
     */
    default Set<String> keySet() {
        //noinspection deprecation
        return self().read(EXTRACT_KEYS).orElseThrow();
    }

    /**
     * Reads the {@code child} object from the given {@code key}.
     * The object will read the child <strong>ONLY</strong> if it's present.
     *
     * @param key    the key to read the child from
     * @param object the object to read from the given key
     */
    default void readChild(String key, ValueIOSerializable object) {
        self().child(key).ifPresent(object::deserialize);
    }
}
