/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.loading;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.DatapackRegistryGatherer;
import net.neoforged.neoforge.data.event.RegistrySets;
import net.neoforged.neoforge.registries.DataPackRegistriesHooks;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.tuple.Triple;
import org.jspecify.annotations.Nullable;

// TODO-ASH: Document
record DatapackRegistryGathererImpl(@Nullable DatapackRegistryGatherer global, String id, RegistrySets<RegistrySetBuilder> local, Set<String> modIds, RegistrySets<MutableBoolean> hasEntries) implements DatapackRegistryGatherer {
    DatapackRegistryGathererImpl(DatapackRegistryGatherer global, String id, Set<String> modIds) {
        this(global, id, new RegistrySets<>(new RegistrySetBuilder(), new RegistrySetBuilder()), modIds, new RegistrySets<>(new MutableBoolean(), new MutableBoolean()));
    }

    DatapackRegistryGathererImpl(Set<String> modIds) {
        this(null, "Global", new RegistrySets<>(new RegistrySetBuilder(), new RegistrySetBuilder()), modIds, new RegistrySets<>(new MutableBoolean(), new MutableBoolean()));
    }

    @Override
    public <T> DatapackRegistryGatherer add(ResourceKey<? extends Registry<T>> registryKey, SingleRegistryBootstrap<T> bootstrap) throws IllegalArgumentException {
        if (this.global != null) this.global.add(registryKey, bootstrap);

        this.getBuilderForRegistry(registryKey).add(registryKey, bootstrap);

        return this;
    }

    @Override
    public <T> DatapackRegistryGatherer add(MultiRegistryBootstrap bootstrap) throws IllegalArgumentException {
        if (this.global != null) this.global.add(bootstrap);

        // Validate that all reguested registries are in the same layer.
        if (bootstrap.requestedRegistries().stream().allMatch(DatapackRegistryGathererImpl::isWorldOrDimensionRegistry)) {
            this.hasEntries.worldAndDimension().setTrue();
            this.local.worldAndDimension().add(bootstrap);
        } else if (bootstrap.requestedRegistries().stream().allMatch(DataPackRegistriesHooks::isReloadableRegistry)) {
            this.hasEntries.reloadable().setTrue();
            this.local.reloadable().add(bootstrap);
        } else {
            // Separate out registries for error message.
            // (World, Reloadable, Neither)
            Triple<Set<Identifier>, Set<Identifier>, Set<Identifier>> keysByLayer = bootstrap.requestedRegistries().stream().collect(
                    () -> Triple.of(new HashSet<>(), new HashSet<>(), new HashSet<>()),
                    (layers, registryKey) -> {
                        if (isWorldOrDimensionRegistry(registryKey)) layers.getLeft().add(registryKey.identifier());
                        else if (DataPackRegistriesHooks.isReloadableRegistry(registryKey)) layers.getMiddle().add(registryKey.identifier());
                        else layers.getRight().add(registryKey.identifier());
                    },
                    (a, b) -> {
                        a.getLeft().addAll(b.getLeft());
                        a.getMiddle().addAll(b.getMiddle());
                        a.getRight().addAll(b.getRight());
                    });
            throw new IllegalArgumentException("Requested registries must all be in the same layer. Currently, world: "
                    + keysByLayer.getLeft() + ", reloadable: " + keysByLayer.getMiddle() + ", illegal: " + keysByLayer.getRight());
        }

        return this;
    }

    private RegistrySetBuilder getBuilderForRegistry(ResourceKey<? extends Registry<?>> registryKey) throws IllegalArgumentException {
        // Dimensions are generated with the world layer.
        if (isWorldOrDimensionRegistry(registryKey)) {
            this.hasEntries.worldAndDimension().setTrue();
            return this.local.worldAndDimension();
        } else if (DataPackRegistriesHooks.isReloadableRegistry(registryKey)) {
            this.hasEntries.reloadable().setTrue();
            return this.local.reloadable();
        } else {
            throw new IllegalArgumentException(
                    registryKey.identifier() + " is not a valid datpack registry." + (
            // Add hint if resource key is not vanilla.
            registryKey.identifier().getNamespace().equals("minecraft") ? ""
                    : " Make sure your registry has been registered via 'NewDatapackRegistryEvent'."));
        }
    }

    private static boolean isWorldOrDimensionRegistry(ResourceKey<? extends Registry<?>> registryKey) {
        return DataPackRegistriesHooks.isWorldRegistry(registryKey)
                || RegistryDataLoader.DIMENSION_REGISTRIES.stream().anyMatch(data -> data.key().equals(registryKey));
    }

    RegistrySets<CompletableFuture<HolderLookup.Provider>> createMain(DataGenerator generator, CompletableFuture<HolderLookup.Provider> worldRegistries) {
        CompletableFuture<HolderLookup.Provider> worldAndDimension = worldRegistries;
        if (this.hasEntries.worldAndDimension().booleanValue()) {
            worldAndDimension = generator.addProvider(true, DatapackBuiltinEntriesProvider.forWorldLayer(
                    generator.getPackOutput(), "World (" + this.id + "): " + this.modIds, worldAndDimension, this.local.worldAndDimension(), this.modIds)).getRegistryProvider();
        }
        CompletableFuture<HolderLookup.Provider> reloadable = worldAndDimension.thenApply(VanillaRegistries::createReloadableLookup);
        if (this.hasEntries.reloadable().booleanValue()) {
            reloadable = generator.addProvider(true, DatapackBuiltinEntriesProvider.forReloadableLayer(
                    generator.getPackOutput(), "Reloadable (" + this.id + "): " + this.modIds, worldAndDimension,
                    reloadable, this.local.reloadable(), this.modIds)).getRegistryProvider();
        }
        return new RegistrySets<>(worldAndDimension, reloadable);
    }

    void createSub(DataGenerator generator, RegistrySets<CompletableFuture<HolderLookup.Provider>> registrySets, int index) {
        CompletableFuture<HolderLookup.Provider> worldAndDimension = registrySets.worldAndDimension();
        if (this.hasEntries.worldAndDimension().booleanValue()) {
            worldAndDimension = generator.addProvider(true, DatapackBuiltinEntriesProvider.forWorldLayer(
                    generator.getPackOutput(), "World (" + this.id + "[" + index + "]" + "): " + this.modIds, worldAndDimension, this.local.worldAndDimension(), this.modIds)).getRegistryProvider();
        }
        if (this.hasEntries.reloadable().booleanValue()) {
            generator.addProvider(true, DatapackBuiltinEntriesProvider.forReloadableLayer(
                    generator.getPackOutput(), "Reloadable (" + this.id + "[" + index + "]" + "): " + this.modIds, worldAndDimension, registrySets.reloadable(), this.local.reloadable(), this.modIds));
        }
    }
}
