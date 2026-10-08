/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.event;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import org.jspecify.annotations.Nullable;

public final class PackGenerator {
    private final GatherDataEvent owner;
    private final PackOutput output;
    private final String providerPrefix;

    @Nullable
    CompletableFuture<HolderLookup.Provider> worldRegistriesWithModdedEntries = null;
    @Nullable
    CompletableFuture<HolderLookup.Provider> reloadableRegistries = null;
    @Nullable
    CompletableFuture<HolderLookup.Provider> reloadableRegistriesWithModdedEntries = null;

    PackGenerator(GatherDataEvent owner, PackOutput output, String providerPrefix) {
        this.owner = owner;
        this.output = output;
        this.providerPrefix = providerPrefix;
    }

    public PackOutput getPackOutput() {
        return output;
    }

    public CompletableFuture<HolderLookup.Provider> getWorldLookupProvider() {
        return Objects.requireNonNullElse(this.worldRegistriesWithModdedEntries, this.owner.getWorldLookupProvider());
    }

    public CompletableFuture<HolderLookup.Provider> getReloadableLookupProvider() {
        if (this.reloadableRegistriesWithModdedEntries != null) {
            return this.reloadableRegistriesWithModdedEntries;
        }
        if (this.reloadableRegistries == null) {
            this.reloadableRegistries = RegistryPatchGenerator.createReloadableLookup(this.getWorldLookupProvider(), this.owner.getReloadableLookupProvider(), new RegistrySetBuilder())
                    .thenApply(RegistrySetBuilder.PatchedRegistries::full);
        }
        return this.reloadableRegistries;
    }

    public <T extends DataProvider> T addProvider(T provider) {
        // wrap the provider to ensure the `getName` return is unique
        this.owner.getGenerator().addProvider(true, new DataProvider() {
            @Override
            public CompletableFuture<?> run(CachedOutput cache) {
                return provider.run(cache);
            }

            @Override
            public String getName() {
                return providerPrefix + provider.getName();
            }
        });

        return provider;
    }

    public <T extends DataProvider> T createProvider(DataProviderFromOutput<T> builder) {
        return addProvider(builder.create(this.output));
    }

    public <T extends DataProvider> T createProvider(DataProviderFromOutputLookup<T> builder) {
        return addProvider(builder.create(this.output, this.getReloadableLookupProvider()));
    }

    public void createBlockAndItemTags(DataProviderFromOutputLookup<TagsProvider<Block>> blockTagsProvider, ItemTagsProvider itemTagsProvider) {
        var blockTags = createProvider(blockTagsProvider);
        addProvider(itemTagsProvider.create(this.output, this.getReloadableLookupProvider(), blockTags.contentsGetter()));
    }

    /// Generates the datapack registry entries in the world layer for the owner's mod id.
    ///
    /// This should only be used when generating a built-in datapack. If generating entries for the
    /// main mod, use [GatherDataRegistryEntriesEvent] instead.
    ///
    /// @param entriesBuilder The registry entries to generate.
    public void createWorldRegistryObjects(RegistrySetBuilder entriesBuilder) {
        this.createWorldRegistryObjects(entriesBuilder, Set.of(this.owner.getModContainer().getModId()));
    }

    /// Generates the datapack registry entries in the world layer for provided mod ids.
    ///
    /// This should only be used when generating a built-in datapack. If generating entries for the
    /// main mod, use [GatherDataRegistryEntriesEvent] instead.
    ///
    /// @param entriesBuilder The registry entries to generate.
    /// @param modIds         The set of mod ids to generate the registry elements of.
    public void createWorldRegistryObjects(RegistrySetBuilder entriesBuilder, Set<String> modIds) {
        this.createWorldRegistryObjects(entriesBuilder, modIds, "world");
    }

    /// Generates the datapack registry entries in the world layer for provided mod ids.
    ///
    /// This should only be used when generating a built-in datapack. If generating entries for the
    /// main mod, use [GatherDataRegistryEntriesEvent] instead.
    ///
    /// @param entriesBuilder The registry entries to generate.
    /// @param modIds         The set of mod ids to generate the registry elements of, or `null` to generate all elements.
    /// @param name           The name of the data provider. Must be unique to prevent collisions with other providers.
    public void createWorldRegistryObjects(RegistrySetBuilder entriesBuilder, @Nullable Set<String> modIds, String name) {
        var registries = this.createProvider((output) -> DatapackBuiltinEntriesProvider.forWorldLayer(output, name, this.getWorldLookupProvider(), entriesBuilder, modIds));
        this.worldRegistriesWithModdedEntries = registries.getRegistryProvider();
    }

    /// Generates the datapack registry entries in the reloadable layer for the owner's mod id.
    ///
    /// This should only be used when generating a built-in datapack. If generating entries for the
    /// main mod, use [GatherDataRegistryEntriesEvent] instead.
    ///
    /// @param entriesBuilder The registry entries to generate.
    public void createReloadableRegistryObjects(RegistrySetBuilder entriesBuilder) {
        this.createReloadableRegistryObjects(entriesBuilder, Set.of(this.owner.getModContainer().getModId()));
    }

    /// Generates the datapack registry entries in the reloadable layer for provided mod ids.
    ///
    /// This should only be used when generating a built-in datapack. If generating entries for the
    /// main mod, use [GatherDataRegistryEntriesEvent] instead.
    ///
    /// @param entriesBuilder The registry entries to generate.
    /// @param modIds         The set of mod ids to generate the registry elements of.
    public void createReloadableRegistryObjects(RegistrySetBuilder entriesBuilder, Set<String> modIds) {
        this.createReloadableRegistryObjects(entriesBuilder, modIds, "reloadable");
    }

    /// Generates the datapack registry entries in the reloadable layer for provided mod ids.
    ///
    /// This should only be used when generating a built-in datapack. If generating entries for the
    /// main mod, use [GatherDataRegistryEntriesEvent] instead.
    ///
    /// @param entriesBuilder The registry entries to generate.
    /// @param modIds         The set of mod ids to generate the registry elements of, or `null` to generate all elements.
    /// @param name           The name of the data provider. Must be unique to prevent collisions with other providers.
    public void createReloadableRegistryObjects(RegistrySetBuilder entriesBuilder, @Nullable Set<String> modIds, String name) {
        var registries = this.createProvider((output) -> DatapackBuiltinEntriesProvider.forReloadableLayer(output, name, this.getWorldLookupProvider(), this.getReloadableLookupProvider(), entriesBuilder, modIds));
        this.reloadableRegistriesWithModdedEntries = registries.getRegistryProvider();
    }

    @FunctionalInterface
    public interface DataProviderFromOutput<T extends DataProvider> {
        T create(PackOutput output);
    }

    @FunctionalInterface
    public interface DataProviderFromOutputLookup<T extends DataProvider> {
        T create(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider);
    }

    @FunctionalInterface
    public interface ItemTagsProvider {
        TagsProvider<Item> create(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> contentsGetter);
    }
}
