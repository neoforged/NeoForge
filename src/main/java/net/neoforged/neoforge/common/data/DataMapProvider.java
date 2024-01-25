/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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
        ImmutableList.Builder<CompletableFuture<?>> futuresBuilder = new ImmutableList.Builder<>();

        gather();

        return lookupProvider.thenCompose(provider -> {
            final DynamicOps<JsonElement> dynamicOps = ConditionalOps.create(RegistryOps.create(JsonOps.INSTANCE, provider), ICondition.IContext.EMPTY);

            this.builders.forEach((type, builder) -> {
                final Path path = this.pathProvider.json(new ResourceLocation(type.id().getNamespace(), DataMapLoader.getFolderLocation(type.registryKey().location()) + "/" + type.id().getPath()));
                futuresBuilder.add(generate(path, cache, builder, dynamicOps));
            });

            return CompletableFuture.allOf(futuresBuilder.build().toArray(CompletableFuture[]::new));
        });
    }

    private <T, R> CompletableFuture<?> generate(Path out, CachedOutput cache, Builder<T, R> builder, DynamicOps<JsonElement> ops) {
        return CompletableFuture.supplyAsync(() -> {
            final Codec<Optional<WithConditions<DataMapFile<T, R>>>> withConditionsCodec = ConditionalOps.createConditionalCodecWithConditions(DataMapFile.codec(builder.registryKey, builder.type));
            return withConditionsCodec.encodeStart(ops, Optional.of(builder.build())).getOrThrow(false, msg -> LOGGER.error("Failed to encode {}: {}", out, msg));
        }).thenComposeAsync(encoded -> DataProvider.saveStable(cache, encoded, out));
    }

    /**
     * Generate data map entries.
     */
    protected abstract void gather();

    @SuppressWarnings("unchecked")
    public <T, R> Builder<T, R> builder(DataMapType<T, R> type) {
        // Avoid any weird classcastexceptions at runtime if a builder was previously created with this method
        if (type instanceof AdvancedDataMapType<?, ?, ?> advanced) {
            return (Builder<T, R>) builder(advanced);
        }
        return (Builder<T, R>) builders.computeIfAbsent(type, k -> new Builder<>(type));
    }

    @SuppressWarnings("unchecked")
    public <T, R, VR extends DataMapValueRemover<T, R>> AdvancedBuilder<T, R, VR> builder(AdvancedDataMapType<T, R, VR> type) {
        return (AdvancedBuilder<T, R, VR>) builders.computeIfAbsent(type, k -> new AdvancedBuilder<>(type));
    }

    @Override
    public String getName() {
        return "Data map";
    }

    public static class Builder<T, R> {
        private boolean replace;
        private final Map<Either<TagKey<R>, ResourceKey<R>>, Optional<WithConditions<DataMapEntry<T>>>> values = new LinkedHashMap<>();
        protected final List<DataMapEntry.Removal<T, R>> removals = new ArrayList<>();
        protected final ResourceKey<Registry<R>> registryKey;
        private final DataMapType<T, R> type;
        private final List<ICondition> conditions = new ArrayList<>();

        public Builder(DataMapType<T, R> type) {
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
            this.values.put(Either.right(object.unwrapKey().orElseThrow()), Optional.of(new WithConditions<>(new DataMapEntry<>(value, replace), conditions)));
            return this;
        }

        public Builder<T, R> add(TagKey<R> tag, T value, boolean replace, ICondition... conditions) {
            this.values.put(Either.left(tag), Optional.of(new WithConditions<>(new DataMapEntry<>(value, replace), conditions)));
            return this;
        }

        public Builder<T, R> remove(ResourceLocation id) {
            this.removals.add(new DataMapEntry.Removal<>(Either.right(ResourceKey.create(registryKey, id)), Optional.empty()));
            return this;
        }

        public Builder<T, R> remove(TagKey<R> tag) {
            this.removals.add(new DataMapEntry.Removal<>(Either.left(tag), Optional.empty()));
            return this;
        }

        public Builder<T, R> remove(Holder<R> value) {
            this.removals.add(new DataMapEntry.Removal<>(Either.right(value.unwrap().orThrow()), Optional.empty()));
            return this;
        }

        public Builder<T, R> replace(boolean replace) {
            this.replace = replace;
            return this;
        }

        public Builder<T, R> conditions(ICondition... conditions) {
            this.conditions.addAll(Arrays.asList(conditions));
            return this;
        }

        public WithConditions<DataMapFile<T, R>> build() {
            return new WithConditions<>(conditions, new DataMapFile<>(replace, values, removals));
        }
    }

    public static class AdvancedBuilder<T, R, VR extends DataMapValueRemover<T, R>> extends Builder<T, R> {
        public AdvancedBuilder(AdvancedDataMapType<T, R, VR> type) {
            super(type);
        }

        public AdvancedBuilder<T, R, VR> remove(TagKey<R> tag, VR remover) {
            this.removals.add(new DataMapEntry.Removal<>(Either.left(tag), Optional.of(remover)));
            return this;
        }

        public AdvancedBuilder<T, R, VR> remove(Holder<R> value, VR remover) {
            this.removals.add(new DataMapEntry.Removal<>(Either.right(value.unwrap().orThrow()), Optional.of(remover)));
            return this;
        }

        public AdvancedBuilder<T, R, VR> remove(ResourceLocation id, VR remover) {
            this.removals.add(new DataMapEntry.Removal<>(Either.right(ResourceKey.create(registryKey, id)), Optional.of(remover)));
            return this;
        }
    }
}
