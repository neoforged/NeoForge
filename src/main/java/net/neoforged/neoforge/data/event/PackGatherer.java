/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.event;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

/// Gathers the [DataProvider]s during the [GatherDataEvent]s.
public interface PackGatherer {
    /// Adds the provider to generate.
    ///
    /// @param factory A factory that constructs the provider from the pack output.
    /// @return The constructed [DataProvider].
    /// @param <T> The type of the provider.
    <T extends DataProvider> T addProvider(DataProvider.Factory<T> factory);

    /// A [PackGatherer] that has access to the previously gathered registry set, containing all
    /// datpack registries (i.e., dimension, world, reloadable).
    ///
    /// @see GatherDataRegistryEntriesEvent
    /// @see GatherDataEvent#subpack
    public interface WithRegistries extends PackGatherer {
        /// {@return the registry set containing all datapack registries (i.e., dimension, world, reloadable)}
        CompletableFuture<HolderLookup.Provider> registries();

        /// Adds the provider to generate.
        ///
        /// @param factory A factory that constructs the provider from the pack output and registry set.
        /// @return The constructed [DataProvider].
        /// @param <T> The type of the provider.
        default <T extends DataProvider> T addProvider(FactoryWithRegistries<T> factory) {
            return this.addProvider(output -> factory.create(output, this.registries()));
        }
    }

    /// A functional interface that constructs a [DataProvider] from the pack output and the
    /// registry set.
    ///
    /// @param <T> The type of the provider.
    @FunctionalInterface
    public interface FactoryWithRegistries<T extends DataProvider> {
        /// Creates the provider.
        ///
        /// @param output     The output location of the pack generator.
        /// @param registries The gathered registry set.
        /// @return The constructed [DataProvider].
        T create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries);
    }
}
