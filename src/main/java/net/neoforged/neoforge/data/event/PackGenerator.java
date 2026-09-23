/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.event;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

public final class PackGenerator {
    private final GatherDataEvent owner;
    private final PackOutput output;

    PackGenerator(GatherDataEvent owner, PackOutput output) {
        this.owner = owner;
        this.output = output;
    }

    public PackOutput getPackOutput() {
        return output;
    }

    public CompletableFuture<HolderLookup.Provider> getWorldLookupProvider() {
        return this.owner.getWorldLookupProvider();
    }

    public CompletableFuture<HolderLookup.Provider> getReloadableLookupProvider() {
        return this.owner.getReloadableLookupProvider();
    }

    public <T extends DataProvider> T addProvider(T provider) {
        return this.owner.getGenerator().addProvider(true, provider);
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

    public void createWorldRegistryObjects(RegistrySetBuilder entriesBuilder) {
        this.createWorldRegistryObjects(entriesBuilder, Set.of(this.owner.getModContainer().getModId()));
    }

    public void createWorldRegistryObjects(RegistrySetBuilder entriesBuilder, Set<String> modIds) {
        this.createWorldRegistryObjects(entriesBuilder, modIds, "world");
    }

    public void createWorldRegistryObjects(RegistrySetBuilder entriesBuilder, Set<String> modIds, String name) {
        var registries = this.createProvider((output) -> DatapackBuiltinEntriesProvider.forWorldLayer(output, name, this.getWorldLookupProvider(), entriesBuilder, modIds));
        this.owner.worldRegistriesWithModdedEntries = registries.getRegistryProvider();
    }

    public void createReloadableRegistryObjects(RegistrySetBuilder entriesBuilder) {
        this.createReloadableRegistryObjects(entriesBuilder, Set.of(this.owner.getModContainer().getModId()));
    }

    public void createReloadableRegistryObjects(RegistrySetBuilder entriesBuilder, Set<String> modIds) {
        this.createReloadableRegistryObjects(entriesBuilder, modIds, "reloadable");
    }

    public void createReloadableRegistryObjects(RegistrySetBuilder entriesBuilder, Set<String> modIds, String name) {
        var registries = this.createProvider((output) -> DatapackBuiltinEntriesProvider.forReloadableLayer(output, name, this.getWorldLookupProvider(), this.getReloadableLookupProvider(), entriesBuilder, modIds));
        this.owner.reloadableRegistriesWithModdedEntries = registries.getRegistryProvider();
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
