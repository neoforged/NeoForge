/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.registries.DataMapLoader;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMap;
import net.neoforged.neoforge.registries.datamaps.DataMapEntry;
import net.neoforged.neoforge.registries.datamaps.DataMapFile;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;

/**
 * A provider for {@link DataMapType data map} generation.
 */
public abstract class DataMapProvider implements DataProvider {
    protected final CompletableFuture<HolderLookup.Provider> lookupProvider;
    protected final PackOutput.PathProvider pathProvider;
    private final Map<DataMapType<?, ?>, Builder<?, ?>> builders = new HashMap<>();

    /**
     * Create a new provider.
     *
     * @param packOutput     the output location
     * @param lookupProvider a {@linkplain CompletableFuture} supplying the registries
     */
    protected DataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.lookupProvider = lookupProvider;
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, DataMapLoader.PATH);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        gather();

        return lookupProvider.thenCompose(provider -> {
            final DynamicOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, provider);

            return CompletableFuture.allOf(this.builders.entrySet().stream().map(entry -> {
                DataMapType<?, ?> type = entry.getKey();
                final Path path = this.pathProvider.json(type.id().withPrefix(DataMapLoader.getFolderLocation(type.registryKey().location()) + "/"));
                return generate(path, cache, entry.getValue(), dynamicOps);
            }).toArray(CompletableFuture[]::new));
        });
    }

    private <T, R> CompletableFuture<?> generate(Path out, CachedOutput cache, Builder<T, R> builder, DynamicOps<JsonElement> ops) {
        return CompletableFuture.supplyAsync(() -> {
            final Codec<Optional<WithConditions<DataMapFile<T, R>>>> withConditionsCodec = ConditionalOps.createConditionalCodecWithConditions(DataMapFile.codec(builder.registryKey, builder.type));
            return withConditionsCodec.encodeStart(ops, Optional.of(builder.build())).getOrThrow(msg -> new RuntimeException("Failed to encode %s: %s".formatted(out, msg)));
        }).thenComposeAsync(encoded -> DataProvider.saveStable(cache, encoded, out));
    }

    /**
     * Generate data map entries.
     */
    protected abstract void gather();

    @SuppressWarnings("unchecked")
    public <T, R> Builder<T, R> builder(DataMapType<R, T> type) {
        // Avoid any weird classcastexceptions at runtime if a builder was previously created with this method
        if (type instanceof AdvancedDataMapType<R, T, ?> advanced) {
            return builder(advanced);
        }
        return (Builder<T, R>) builders.computeIfAbsent(type, k -> new Builder<>(type));
    }

    @SuppressWarnings("unchecked")
    public <T, R, VR extends DataMapValueRemover<R, T>> AdvancedBuilder<T, R, VR> builder(AdvancedDataMapType<R, T, VR> type) {
        return (AdvancedBuilder<T, R, VR>) builders.computeIfAbsent(type, k -> new AdvancedBuilder<>(type));
    }

    @Override
    public String getName() {
        return "Data Maps";
    }

    public static class Builder<T, R> {
        private final Map<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapEntry<T>>>> values = new LinkedHashMap<>();
        protected final Map<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapValueRemover<R, T>>>> removals = new LinkedHashMap<>();
        protected final ResourceKey<Registry<R>> registryKey;
        private final DataMapType<R, T> type;
        private final List<ICondition> conditions = new ArrayList<>();

        private boolean replace;

        public Builder(DataMapType<R, T> type) {
            this.type = type;
            this.registryKey = type.registryKey();
        }

        public Builder<T, R> add(ResourceKey<R> key, T value, boolean replace, ICondition... conditions) {
            this.values.put(Either.right(key), Optional.of(new WithConditions<>(new DataMapEntry<>(value, replace), conditions)));
            return this;
        }

        public Builder<T, R> add(ResourceLocation id, T value, boolean replace, ICondition... conditions) {
            return add(ResourceKey.create(registryKey, id), value, replace, conditions);
        }

        public Builder<T, R> add(Holder<R> object, T value, boolean replace, ICondition... conditions) {
            return add(object.unwrapKey().orElseThrow(), value, replace, conditions);
        }

        public Builder<T, R> add(TagKey<R> tag, T value, boolean replace, ICondition... conditions) {
            this.values.put(Either.left(tag), Optional.of(new WithConditions<>(new DataMapEntry<>(value, replace), conditions)));
            return this;
        }

        public Builder<T, R> remove(ResourceLocation id, ICondition... conditions) {
            this.removals.put(Either.right(ResourceKey.create(registryKey, id)), Optional.of(new WithConditions<>(DataMapValueRemover.Default.defaultRemover(), conditions)));
            return this;
        }

        public Builder<T, R> remove(TagKey<R> tag, ICondition... conditions) {
            this.removals.put(Either.left(tag),  Optional.of(new WithConditions<>(DataMapValueRemover.Default.defaultRemover(), conditions)));
            return this;
        }

        public Builder<T, R> remove(Holder<R> value, ICondition... conditions) {
            this.removals.put(Either.right(value.unwrap().orThrow()), Optional.of(new WithConditions<>(DataMapValueRemover.Default.defaultRemover(), conditions)));
            return this;
        }

        public Builder<T, R> replace(boolean replace) {
            this.replace = replace;
            return this;
        }

        public Builder<T, R> conditions(ICondition... conditions) {
            Collections.addAll(this.conditions, conditions);
            return this;
        }

        public WithConditions<DataMapFile<T, R>> build() {
            return new WithConditions<>(conditions, new DataMapFile<>(replace, values, removals));
        }
    }

    public static class AdvancedBuilder<T, R, VR extends DataMapValueRemover<R, T>> extends Builder<T, R> {
        public AdvancedBuilder(AdvancedDataMapType<R, T, VR> type) {
            super(type);
        }

        public AdvancedBuilder<T, R, VR> remove(TagKey<R> tag, VR remover, ICondition... conditions) {
            this.removals.put(Either.left(tag), Optional.of(new WithConditions<>(remover, conditions)));
            return this;
        }

        public AdvancedBuilder<T, R, VR> remove(Holder<R> value, VR remover, ICondition... conditions) {
            this.removals.put(Either.right(value.unwrap().orThrow()), Optional.of(new WithConditions<>(remover, conditions)));
            return this;
        }

        public AdvancedBuilder<T, R, VR> remove(ResourceLocation id, VR remover, ICondition... conditions) {
            this.removals.put(Either.right(ResourceKey.create(registryKey, id)), Optional.of(new WithConditions<>(remover, conditions)));
            return this;
        }
    }
}
