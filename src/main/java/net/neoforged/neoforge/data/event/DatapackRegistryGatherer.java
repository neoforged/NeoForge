/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.event;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

/// Gathers the datapack registry entries during [GatherDataRegistryEntriesEvent]. Only mods added via '--mod'
/// in the run arguments generate their contents, but all gathered entries can still be used for other parts of
/// the data generation pipeline.
///
/// Distinctions between the different registry layers (i.e., world, reloadable) are abstracted away and handled
/// by the implementation.
public interface DatapackRegistryGatherer {
    /// Adds a bootstrap for the provided registry [ResourceKey] to register entries for use during
    /// data generation. The layer each registry belongs to is handled internally.
    ///
    /// Analogous to [net.minecraft.core.RegistrySetBuilder#add(ResourceKey, SingleRegistryBootstrap)].
    ///
    /// Custom datapack registries must be registered via [NewDatapackRegistryEvent][net.neoforged.neoforge.registries.NewDatapackRegistryEvent]
    /// first; otherwise, an exception will be thrown.
    ///
    /// @param registryKey The key of the registry.
    /// @param bootstrap   The bootstrap used to register entries to the registry.
    /// @return This gatherer.
    /// @param <T> The type of the registry object.
    /// @throws IllegalArgumentException If the `registryKey` does not belong to a datapack registry.
    <T> DatapackRegistryGatherer add(ResourceKey<? extends Registry<T>> registryKey, SingleRegistryBootstrap<T> bootstrap) throws IllegalArgumentException;

    /// Adds a bootstrap for the requested registries to register entries for use during data generation.
    /// The layer the registries belong to is handled internally.
    ///
    /// Analogous to [net.minecraft.core.RegistrySetBuilder#add(MultiRegistryBootstrap)].
    ///
    /// Custom datapack registries must be registered via [NewDatapackRegistryEvent][net.neoforged.neoforge.registries.NewDatapackRegistryEvent]
    /// first. Additionally, all requested registries must be part of the same registry layer. Otherwise, an
    /// exception will be thrown.
    ///
    /// @param bootstrap The bootstrap used to register entries to the requested registries.
    /// @return This gatherer.
    /// @throws IllegalArgumentException If not all requested registries are datapack registries in the same layer.
    DatapackRegistryGatherer add(MultiRegistryBootstrap bootstrap) throws IllegalArgumentException;

    /// Adds the sub providers used to bootstrap the [Registries#LOOT_TABLE] entries for use during data generation.
    /// Wraps around [LootTableProvider].
    ///
    /// @param subProvider  The sub provider used to add loot tables for a specific [loot context parameter set][net.minecraft.util.context.ContextKeySet].
    /// @param subProviders Any additional sub providers.
    /// @return This gatherer.
    default DatapackRegistryGatherer lootTable(LootTableProvider.SubProviderEntry subProvider, LootTableProvider.SubProviderEntry... subProviders) {
        this.add(Registries.LOOT_TABLE, new LootTableProvider(Set.of(), createList(subProvider, subProviders)));
        return this;
    }

    /// Adds the sub providers used to bootstrap the [Registries#ADVANCEMENT] entries for use during data generation.
    /// Wraps around [AdvancementProvider].
    ///
    /// @param subProvider  The sub provider used to add advancements.
    /// @param subProviders Any additional sub providers.
    /// @return This gatherer.
    default DatapackRegistryGatherer advancement(AdvancementSubProvider.Factory subProvider, AdvancementSubProvider.Factory... subProviders) {
        this.add(Registries.ADVANCEMENT, new AdvancementProvider(createList(subProvider, subProviders)));
        return this;
    }

    /// Adds the recipe provider used to bootstrap the [Registries#RECIPE] entries for use during data generation.
    /// Wraps around [RecipeProvider#asBootstrap(BiFunction)].
    ///
    /// @param providerFactory A bi-function that takes in the [BootstrapContext] for the recipe and advancement registries
    ///                        to construct the [RecipeProvider].
    /// @return This gatherer.
    default DatapackRegistryGatherer recipe(BiFunction<BootstrapContext<Recipe<?>>, BootstrapContext<Advancement>, ? extends RecipeProvider> providerFactory) {
        this.add(RecipeProvider.asBootstrap(providerFactory));
        return this;
    }

    /// An internal helper for converting a single element followed by a varargs of elements into a list.
    ///
    /// @param first    The first element.
    /// @param elements Any additional elements.
    /// @return An unmodifiable list of elements.
    /// @param <T> The type of the elements in the list.
    private static <T> List<T> createList(T first, T... elements) {
        return Stream.concat(Stream.of(first), Arrays.stream(elements)).toList();
    }
}
