/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

public record DataMapFile<T, R>(
        boolean replace,
        Map<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapEntry<T>>>> values,
        Map<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapValueRemover<R, T>>>> removals) {
    public static <T, R> Codec<DataMapFile<T, R>> codec(ResourceKey<Registry<R>> registryKey, DataMapType<R, T> dataMap) {
        final Codec<Either<TagKey<R>, ResourceKey<R>>> tagOrValue = ExtraCodecs.TAG_OR_ELEMENT_ID.xmap(
                l -> l.tag() ? Either.left(TagKey.create(registryKey, l.id())) : Either.right(ResourceKey.create(registryKey, l.id())),
                e -> e.map(t -> new ExtraCodecs.TagOrElementLocation(t.location(), true), r -> new ExtraCodecs.TagOrElementLocation(r.location(), false)));

        final Codec<Optional<WithConditions<DataMapEntry<T>>>> valueCodec = ConditionalOps.createConditionalCodecWithConditions(DataMapEntry.codec(dataMap));
        final Codec<Map<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapEntry<T>>>>> valuesCodec = ExtraCodecs.strictUnboundedMap(tagOrValue, valueCodec);

        final Codec<Map<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapValueRemover<R, T>>>>> removalsCodec;
        final Codec<Map<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapValueRemover<R, T>>>>> removalListCodec = NeoForgeExtraCodecs
                .setOf(tagOrValue)
                .flatXmap(
                        keys -> DataResult.success(Maps.asMap(keys, key -> Optional.empty())),
                        map -> {
                            for (var removal : map.values()) {
                                if (removal.isPresent()) {
                                    if (!removal.get().conditions().isEmpty())
                                        return DataResult.error(() -> "Data map removals with conditions can not be encoded to key set");
                                    if (!removal.get().carrier().equals(DataMapValueRemover.Default.INSTANCE))
                                        return DataResult.error(() -> "Data map removals with custom removers can not be encoded to key set");
                                }
                            }
                            return DataResult.success(map.keySet());
                        });
        if (dataMap instanceof AdvancedDataMapType<R, T, ?>) {
            final AdvancedDataMapType<R, T, DataMapValueRemover<R, T>> advanced = (AdvancedDataMapType<R, T, DataMapValueRemover<R, T>>) dataMap;
            final Codec<Optional<WithConditions<DataMapValueRemover<R, T>>>> removerCodec = ConditionalOps.createConditionalCodecWithConditions(advanced.remover());
            final Codec<Map<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapValueRemover<R, T>>>>> advancedRemovalsCodec = ExtraCodecs
                    .strictUnboundedMap(tagOrValue, Codec.either(removerCodec, ICondition.LIST_CODEC))
                    .xmap(
                            map -> {
                                final var builder = ImmutableMap.<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapValueRemover<R, T>>>>builder();
                                map.forEach((source, either) -> {
                                    either.ifLeft(remover -> builder.put(source, remover));
                                    either.ifRight(conditions -> {
                                        if (conditions.stream().allMatch(condition -> condition.test(ICondition.IContext.TAGS_INVALID))) {
                                            builder.put(source, Optional.empty());
                                        }
                                    });
                                });
                                return builder.build();
                            },
                            map -> {
                                final var builder = ImmutableMap.<Either<TagKey<R>, ResourceKey<R>>, Either<Optional<WithConditions<DataMapValueRemover<R, T>>>, List<ICondition>>>builder();
                                for (var entry : map.entrySet()) {
                                    builder.put(entry.getKey(), entry.getValue().<Either<Optional<WithConditions<DataMapValueRemover<R, T>>>, List<ICondition>>>map(conditioned -> conditioned.carrier().equals(DataMapValueRemover.Default.INSTANCE)
                                            ? Either.right(conditioned.conditions())
                                            : Either.left(Optional.of(conditioned))).orElse(Either.right(List.of())));
                                }
                                return builder.build();
                            });
            removalsCodec = NeoForgeExtraCodecs.withAlternative(removalListCodec, advancedRemovalsCodec);
        } else {
            final Codec<Map<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapValueRemover<R, T>>>>> conditionedRemovalsCodec = ExtraCodecs
                    .strictUnboundedMap(tagOrValue, ICondition.LIST_CODEC)
                    .xmap(
                            map -> {
                                final var builder = ImmutableMap.<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapValueRemover<R, T>>>>builder();
                                map.forEach((source, conditions) -> {
                                    if (conditions.stream().allMatch(condition -> condition.test(ICondition.IContext.TAGS_INVALID))) {
                                        builder.put(source, Optional.empty());
                                    }
                                });
                                return builder.build();
                            },
                            map -> {
                                final var builder = ImmutableMap.<Either<TagKey<R>, ResourceKey<R>>, List<ICondition>>builder();
                                map.forEach((source, optional) -> optional.ifPresent(conditioned -> builder.put(source, conditioned.conditions())));
                                return builder.build();
                            });
            removalsCodec = NeoForgeExtraCodecs.withAlternative(removalListCodec, conditionedRemovalsCodec);
        }

        return RecordCodecBuilder.create(in -> in.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(DataMapFile::replace),
                valuesCodec.optionalFieldOf("values", Map.of()).forGetter(DataMapFile::values),
                removalsCodec.optionalFieldOf("remove", Map.of()).forGetter(DataMapFile::removals)).apply(in, DataMapFile::new));
    }
}
