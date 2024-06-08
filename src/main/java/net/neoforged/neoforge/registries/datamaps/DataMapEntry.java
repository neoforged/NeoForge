/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public record DataMapEntry<T>(T value, boolean replace) {
    private DataMapEntry(T value) {
        this(value, false);
    }

    public static <T> Codec<DataMapEntry<T>> codec(DataMapType<?, T> type) {
        return Codec.either(
                RecordCodecBuilder.<DataMapEntry<T>>create(i -> i.group(
                        type.codec().fieldOf("value").forGetter(DataMapEntry::value),
                        Codec.BOOL.optionalFieldOf("replace", false).forGetter(DataMapEntry::replace)).apply(i, DataMapEntry::new)),
                type.codec()).xmap(e -> e.map(Function.identity(), DataMapEntry::new), entry -> entry.replace() ? Either.left(entry) : Either.right(entry.value()));
    }
}
