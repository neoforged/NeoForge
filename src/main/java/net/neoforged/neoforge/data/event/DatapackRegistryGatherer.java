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

/// TODO-ASH: Document
public interface DatapackRegistryGatherer {
    <T> DatapackRegistryGatherer add(ResourceKey<? extends Registry<T>> registryKey, SingleRegistryBootstrap<T> bootstrap) throws IllegalArgumentException;

    <T> DatapackRegistryGatherer add(MultiRegistryBootstrap bootstrap) throws IllegalArgumentException;

    default DatapackRegistryGatherer lootTable(LootTableProvider.SubProviderEntry subProvider, LootTableProvider.SubProviderEntry... subProviders) {
        this.add(Registries.LOOT_TABLE, new LootTableProvider(Set.of(), createList(subProvider, subProviders)));
        return this;
    }

    default DatapackRegistryGatherer advancement(AdvancementSubProvider.Factory subProvider, AdvancementSubProvider.Factory... subProviders) {
        this.add(Registries.ADVANCEMENT, new AdvancementProvider(createList(subProvider, subProviders)));
        return this;
    }

    default DatapackRegistryGatherer recipe(BiFunction<BootstrapContext<Recipe<?>>, BootstrapContext<Advancement>, ? extends RecipeProvider> providerFactory) {
        this.add(RecipeProvider.asBootstrap(providerFactory));
        return this;
    }

    private static <T> List<T> createList(T first, T... elements) {
        return Stream.concat(Stream.of(first), Arrays.stream(elements)).toList();
    }
}
