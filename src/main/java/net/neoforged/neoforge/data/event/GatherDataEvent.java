/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.event;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/// Gathers the providers used to generate data and resources. This is only fired for mods that are added via
/// '--mod' in the run arguments.
///
/// These events are not [cancellable][ICancellableEvent].
///
/// These events are fired on the mod-specific event bus, during data generation.
/// 
/// @see GatherDataEvent.Client
/// @see GatherDataEvent.Server
public abstract sealed class GatherDataEvent extends Event implements IModBusEvent {
    private final DataGenerator dataGenerator;
    private final DataGeneratorConfig config;
    private final ModContainer modContainer;
    private final PackGenerator defaultPackGenerator;
    private final DatapackRegistrySets<CompletableFuture<HolderLookup.Provider>> registries;

    @ApiStatus.Internal
    public GatherDataEvent(final ModContainer mc, final DataGenerator dataGenerator, final DataGeneratorConfig dataGeneratorConfig, final DatapackRegistrySets<CompletableFuture<HolderLookup.Provider>> registries) {
        this.modContainer = mc;
        this.dataGenerator = dataGenerator;
        this.config = dataGeneratorConfig;
        this.registries = registries;
        this.defaultPackGenerator = new PackGenerator(this, dataGenerator.getPackOutput());
    }

    public ModContainer getModContainer() {
        return this.modContainer;
    }

    public ResourceManager getResourceManager(PackType packType) {
        return switch (packType) {
            case CLIENT_RESOURCES -> config.clientResourceManager;
            case SERVER_DATA -> config.serverResourceManager;
        };
    }

    public Collection<Path> getInputs() {
        return this.config.getInputs();
    }

    public DataGenerator getGenerator() {
        return this.dataGenerator;
    }

    public PackGenerator getDefaultPackGenerator() {
        return defaultPackGenerator;
    }

    public PackGenerator getPackGenerator(PackOutput output) {
        return new PackGenerator(this, output);
    }

    /// {@return the world registries with all modded entries}
    public CompletableFuture<HolderLookup.Provider> getWorldLookupProvider() {
        return this.registries.worldAndDimension();
    }

    /// {@return the reloadable registries with all modded entries}
    public CompletableFuture<HolderLookup.Provider> getReloadableLookupProvider() {
        return this.registries.reloadable();
    }

    public boolean includeDev() {
        return this.config.dev;
    }

    public boolean includeReports() {
        return this.config.reports;
    }

    public boolean validate() {
        return this.config.validate;
    }

    public static final class Server extends GatherDataEvent {
        @ApiStatus.Internal
        public Server(ModContainer mc, DataGenerator dataGenerator, DataGeneratorConfig dataGeneratorConfig, DatapackRegistrySets<CompletableFuture<HolderLookup.Provider>> registries) {
            super(mc, dataGenerator, dataGeneratorConfig, registries);
        }
    }

    public static final class Client extends GatherDataEvent {
        @ApiStatus.Internal
        public Client(ModContainer mc, DataGenerator dataGenerator, DataGeneratorConfig dataGeneratorConfig, DatapackRegistrySets<CompletableFuture<HolderLookup.Provider>> registries) {
            super(mc, dataGenerator, dataGeneratorConfig, registries);
        }
    }

    public <T extends DataProvider> T addProvider(T provider) {
        return this.defaultPackGenerator.addProvider(provider);
    }

    public <T extends DataProvider> T createProvider(PackGenerator.DataProviderFromOutput<T> builder) {
        return this.defaultPackGenerator.createProvider(builder);
    }

    public <T extends DataProvider> T createProvider(PackGenerator.DataProviderFromOutputLookup<T> builder) {
        return this.defaultPackGenerator.createProvider(builder);
    }

    public void createBlockAndItemTags(PackGenerator.DataProviderFromOutputLookup<TagsProvider<Block>> blockTagsProvider, PackGenerator.ItemTagsProvider itemTagsProvider) {
        this.defaultPackGenerator.createBlockAndItemTags(blockTagsProvider, itemTagsProvider);
    }

    @ApiStatus.Internal
    @FunctionalInterface
    public interface GatherDataEventGenerator {
        GatherDataEvent create(final ModContainer mc, final DataGenerator dataGenerator, final DataGeneratorConfig dataGeneratorConfig, final DatapackRegistrySets<CompletableFuture<HolderLookup.Provider>> registries);
    }
}
