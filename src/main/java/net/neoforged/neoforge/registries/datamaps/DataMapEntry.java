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
import net.minecraft.util.ExtraCodecs;

public record DataMapEntry<T>(T value, boolean replace) {
    private DataMapEntry(T value) {
        this(value, false);
    }

    public static <T> Codec<DataMapEntry<T>> codec(DataMapType<?, T> type) {
        return ExtraCodecs.either(
                RecordCodecBuilder.<DataMapEntry<T>>create(i -> i.group(
                        type.codec().fieldOf("value").forGetter(DataMapEntry::value),
                        ExtraCodecs.strictOptionalField(Codec.BOOL, "replace", false).forGetter(DataMapEntry::replace)).apply(i, DataMapEntry::new)),
                type.codec()).xmap(e -> e.map(Function.identity(), DataMapEntry::new), entry -> entry.replace() ? Either.left(entry) : Either.right(entry.value()));
    }

    public record Removal<T, R>(Either<TagKey<R>, ResourceKey<R>> key,
            Optional<DataMapValueRemover<R, T>> remover) {
        private Removal(Either<TagKey<R>, ResourceKey<R>> key) {
            this(key, Optional.empty());
        }

        @SuppressWarnings("unchecked")
        public static <T, R> Codec<Removal<T, R>> codec(Codec<Either<TagKey<R>, ResourceKey<R>>> tagOrValue, DataMapType<R, T> attachment) {
            if (attachment instanceof AdvancedDataMapType<R, T, ?> advanced) {
                return RecordCodecBuilder.create(in -> in.group(
                        tagOrValue.fieldOf("key").forGetter(Removal::key),
                        ExtraCodecs.strictOptionalField((Codec<DataMapValueRemover<R, T>>) advanced.remover(), "remover").forGetter(Removal::remover)).apply(in, Removal::new));
            }
            return RecordCodecBuilder.create(inst -> inst
                    .group(tagOrValue.fieldOf("key").forGetter(Removal::key))
                    .apply(inst, Removal::new));
        }
    }
}
