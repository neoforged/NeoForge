/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.loading;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.data.event.DatapackRegistryGatherer;
import net.neoforged.neoforge.data.event.DatapackRegistrySets;
import net.neoforged.neoforge.data.event.GlobalDatapackRegistryGatherer;
import net.neoforged.neoforge.data.event.PackGatherer;
import org.jetbrains.annotations.ApiStatus;

/// An internal implementation of [PackGatherer.WithRegistries].
@ApiStatus.Internal
public class RegistryPackGatherer implements PackGatherer.WithRegistries {
    private final PackGatherer delegate;
    private final CompletableFuture<HolderLookup.Provider> registries;

    /// Constructs the pack gatherer from an existing set of registries to merge the provided
    /// registry entries onto.
    ///
    /// @param mod            The container of the mod.
    /// @param providerPrefix The prefix to apply to the registry set.
    /// @param pack           The pack gatherer to provide registry support for.
    /// @param registrySets   The parent registries for each layer.
    /// @param gatherer       A consumer that gathers the datapack registry entries to generate.
    /// @return A pack gatherer.
    public static PackGatherer.WithRegistries createFromRegistries(ModContainer mod, String providerPrefix, PackGatherer pack, DatapackRegistrySets<CompletableFuture<HolderLookup.Provider>> registrySets, Consumer<GlobalDatapackRegistryGatherer> gatherer) {
        // Gather the subpack datapack registries
        List<DatapackRegistryGathererImpl> registryProviders = new ArrayList<>();
        var subpackGatherer = new DatapackRegistryGathererImpl(providerPrefix, Set.of(mod.getModId()));
        gatherer.accept(new GlobalDatapackRegistryGatherer() {
            @Override
            public DatapackRegistryGatherer gatherFor(String modId, String... modIds) {
                var subGatherer = new DatapackRegistryGathererImpl(subpackGatherer, providerPrefix, Stream.concat(Stream.of(modId), Arrays.stream(modIds)).collect(Collectors.toSet()));
                registryProviders.add(subGatherer);
                return subGatherer;
            }

            @Override
            public <T> DatapackRegistryGatherer add(ResourceKey<? extends Registry<T>> registryKey, SingleRegistryBootstrap<T> bootstrap) throws IllegalArgumentException {
                return subpackGatherer.add(registryKey, bootstrap);
            }

            @Override
            public DatapackRegistryGatherer add(MultiRegistryBootstrap bootstrap) throws IllegalArgumentException {
                return subpackGatherer.add(bootstrap);
            }
        });
        var registries = subpackGatherer.createGlobal(registrySets.worldAndDimension());

        // Add them to the pack generator
        for (int i = 0; i < registryProviders.size(); ++i) {
            registryProviders.get(i).createSub(pack, registries, i);
        }

        // Create the subpack gatherer
        return create(pack, registries.reloadable());
    }

    /// Constructs the pack gatherer using the existing registry set.
    ///
    /// @param pack       The pack gatherer to provide registry support for.
    /// @param registries The registries to use.
    /// @return A pack gatherer.
    public static PackGatherer.WithRegistries create(PackGatherer pack, CompletableFuture<HolderLookup.Provider> registries) {
        return new RegistryPackGatherer(pack, registries);
    }

    private RegistryPackGatherer(PackGatherer delegate, CompletableFuture<HolderLookup.Provider> registries) {
        this.delegate = delegate;
        this.registries = registries;
    }

    @Override
    public CompletableFuture<HolderLookup.Provider> registries() {
        return this.registries;
    }

    @Override
    public <T extends DataProvider> T addProvider(DataProvider.Factory<T> factory) {
        return this.delegate.addProvider(factory);
    }
}
