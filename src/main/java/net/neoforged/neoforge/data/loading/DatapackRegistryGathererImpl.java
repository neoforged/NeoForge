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
import net.neoforged.neoforge.data.event.DatapackRegistrySets;
import net.neoforged.neoforge.registries.DataPackRegistriesHooks;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.tuple.Triple;
import org.jspecify.annotations.Nullable;

/// An internal implementation of [DatapackRegistryGatherer].
///
/// @param global     The gather of the global registry set, `null` if this is the global registry set.
/// @param id         The id of the gatherer, either 'Global' for the global registry set, or the mod id specifying the set of mods to generate for.
/// @param builder    The [RegistrySetBuilder]s to register the datapack registry objects to, depending on their registry's layer.
/// @param modIds     The set of mod ids to generate the registry elements of.
/// @param hasEntries A helper for keeping track of whether any entries were registered to the [RegistrySetBuilder]s.
record DatapackRegistryGathererImpl(@Nullable DatapackRegistryGatherer global, String id, DatapackRegistrySets<RegistrySetBuilder> builder, Set<String> modIds, DatapackRegistrySets<MutableBoolean> hasEntries) implements DatapackRegistryGatherer {
    /// Used to construct the registry gatherer for a subset of mod ids.
    ///
    /// @param global The global registry gatherer.
    /// @param id     The mod id specifying the subset of mod ids to generate for.
    /// @param modIds The set of mod ids to generate the registry elements of.
    DatapackRegistryGathererImpl(DatapackRegistryGatherer global, String id, Set<String> modIds) {
        this(global, id, new DatapackRegistrySets<>(new RegistrySetBuilder(), new RegistrySetBuilder()), modIds, new DatapackRegistrySets<>(new MutableBoolean(), new MutableBoolean()));
    }

    /// Used to construct the global registry gatherer.
    ///
    /// @param modIds The set of mod ids to generate the registry elements of.
    DatapackRegistryGathererImpl(Set<String> modIds) {
        this(null, "Global", new DatapackRegistrySets<>(new RegistrySetBuilder(), new RegistrySetBuilder()), modIds, new DatapackRegistrySets<>(new MutableBoolean(), new MutableBoolean()));
    }

    @Override
    public <T> DatapackRegistryGatherer add(ResourceKey<? extends Registry<T>> registryKey, SingleRegistryBootstrap<T> bootstrap) throws IllegalArgumentException {
        if (this.global != null) this.global.add(registryKey, bootstrap);

        // Check which layer the bootstrap should be added to.
        if (isWorldOrDimensionRegistry(registryKey)) {
            this.hasEntries.worldAndDimension().setTrue();
            this.builder.worldAndDimension().add(registryKey, bootstrap);
        } else if (DataPackRegistriesHooks.isReloadableRegistry(registryKey)) {
            this.hasEntries.reloadable().setTrue();
            this.builder.reloadable().add(registryKey, bootstrap);
        } else {
            throw new IllegalArgumentException(
                    registryKey.identifier() + " is not a valid datapack registry." + (
            // Add hint if resource key is not vanilla.
            registryKey.identifier().getNamespace().equals("minecraft") ? ""
                    : " Make sure your registry has been registered via 'NewDatapackRegistryEvent'."));
        }

        return this;
    }

    @Override
    public DatapackRegistryGatherer add(MultiRegistryBootstrap bootstrap) throws IllegalArgumentException {
        if (this.global != null) this.global.add(bootstrap);

        // Validate that all reguested registries are in the same layer.
        if (bootstrap.requestedRegistries().stream().allMatch(DatapackRegistryGathererImpl::isWorldOrDimensionRegistry)) {
            this.hasEntries.worldAndDimension().setTrue();
            this.builder.worldAndDimension().add(bootstrap);
        } else if (bootstrap.requestedRegistries().stream().allMatch(DataPackRegistriesHooks::isReloadableRegistry)) {
            this.hasEntries.reloadable().setTrue();
            this.builder.reloadable().add(bootstrap);
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
                    + keysByLayer.getLeft() + ", reloadable: " + keysByLayer.getMiddle() + ", invalid: " + keysByLayer.getRight());
        }

        return this;
    }

    /// {@return whether the provided [ResourceKey] belongs to a datapack registry in the dimension
    /// or world layer}
    ///
    /// @param registryKey The [ResourceKey] of the registry to check.
    private static boolean isWorldOrDimensionRegistry(ResourceKey<? extends Registry<?>> registryKey) {
        return DataPackRegistriesHooks.isWorldRegistry(registryKey)
                || RegistryDataLoader.DIMENSION_REGISTRIES.stream().anyMatch(data -> data.key().equals(registryKey));
    }

    /// An internal method used to add the data providers for the global registry set, if any entries were added.
    ///
    /// @param generator       The data generator.
    /// @param worldRegistries The vanilla world registries.
    /// @return The registry set containing the modded registries for each layer.
    DatapackRegistrySets<CompletableFuture<HolderLookup.Provider>> createGlobal(DataGenerator generator, CompletableFuture<HolderLookup.Provider> worldRegistries) {
        CompletableFuture<HolderLookup.Provider> worldAndDimension = worldRegistries;
        if (this.hasEntries.worldAndDimension().booleanValue()) {
            worldAndDimension = generator.addProvider(true, DatapackBuiltinEntriesProvider.forWorldLayer(
                    generator.getPackOutput(), "World (" + this.id + "): " + this.modIds, worldAndDimension, this.builder.worldAndDimension(), this.modIds)).getRegistryProvider();
        }
        CompletableFuture<HolderLookup.Provider> reloadable = worldAndDimension.thenApply(VanillaRegistries::createReloadableLookup);
        if (this.hasEntries.reloadable().booleanValue()) {
            reloadable = generator.addProvider(true, DatapackBuiltinEntriesProvider.forReloadableLayer(
                    generator.getPackOutput(), "Reloadable (" + this.id + "): " + this.modIds, worldAndDimension,
                    reloadable, this.builder.reloadable(), this.modIds)).getRegistryProvider();
        }
        return new DatapackRegistrySets<>(worldAndDimension, reloadable);
    }

    /// An internal method used to add the data providers for any mod id subset, if any entries were added.
    ///
    /// @param generator    The data generator.
    /// @param registrySets The global registries for each layer.
    /// @param index        A unique identifier to prevent name collisions.
    void createSub(DataGenerator generator, DatapackRegistrySets<CompletableFuture<HolderLookup.Provider>> registrySets, int index) {
        CompletableFuture<HolderLookup.Provider> worldAndDimension = registrySets.worldAndDimension();
        if (this.hasEntries.worldAndDimension().booleanValue()) {
            worldAndDimension = generator.addProvider(true, DatapackBuiltinEntriesProvider.forWorldLayer(
                    generator.getPackOutput(), "World (" + this.id + "[" + index + "]" + "): " + this.modIds, worldAndDimension, this.builder.worldAndDimension(), this.modIds)).getRegistryProvider();
        }
        if (this.hasEntries.reloadable().booleanValue()) {
            generator.addProvider(true, DatapackBuiltinEntriesProvider.forReloadableLayer(
                    generator.getPackOutput(), "Reloadable (" + this.id + "[" + index + "]" + "): " + this.modIds, worldAndDimension, registrySets.reloadable(), this.builder.reloadable(), this.modIds));
        }
    }
}
