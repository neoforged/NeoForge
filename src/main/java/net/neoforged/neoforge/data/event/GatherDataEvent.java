/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.event;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.data.loading.RegistryPackGatherer;
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
public abstract sealed class GatherDataEvent extends Event implements IModBusEvent, PackGatherer.WithRegistries {
    private final DataGenerator dataGenerator;
    private final DataGeneratorConfig config;
    private final ModContainer modContainer;
    private final PackGatherer.WithRegistries mainPack;
    private final DatapackRegistrySets<CompletableFuture<HolderLookup.Provider>> registries;
    private int packCounter = 0;

    @ApiStatus.Internal
    public GatherDataEvent(final ModContainer mc, final DataGenerator dataGenerator, final PackGatherer mainPack, final DataGeneratorConfig dataGeneratorConfig, final DatapackRegistrySets<CompletableFuture<HolderLookup.Provider>> registries) {
        this.modContainer = mc;
        this.dataGenerator = dataGenerator;
        this.config = dataGeneratorConfig;
        this.registries = registries;
        this.mainPack = RegistryPackGatherer.create(mainPack, registries.reloadable());
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

    /// Creates a subpack generator that generates the desired providers at the given path
    /// relative to the output directory.
    ///
    /// If there are custom datapack registry entries that should be generated for this
    /// subpack, use [#subpack(String, Consumer)] instead.
    ///
    /// @param path The path relative to the output directory.
    /// @return The subpack provider gatherer.
    public PackGatherer.WithRegistries subpack(String path) {
        return RegistryPackGatherer.create(this.dataGenerator.getPackGenerator(true, this.nextProviderPrefix(), path), this.registries.reloadable());
    }

    /// Creates a subpack generator that generates the desired providers at the given output.
    ///
    /// If there are custom datapack registry entries that should be generated for this
    /// subpack, use [#subpack(PackOutput, Consumer)] instead.
    ///
    /// @param output The output to generate the providers to.
    /// @return The subpack provider gatherer.
    public PackGatherer.WithRegistries subpack(PackOutput output) {
        return RegistryPackGatherer.create(this.dataGenerator.getPackGenerator(true, this.nextProviderPrefix(), output), this.registries.reloadable());
    }

    /// Creates a subpack generator that generates the desired providers at the given path
    /// relative to the output directory.
    ///
    /// Datapack registry entries generated for the subpack are ONLY available to the subpack
    /// providers. If the registry entry should be accessible from the main pack, add the entries
    /// via [GatherDataRegistryEntriesEvent].
    ///
    /// @param path     The path relative to the output directory.
    /// @param gatherer A consumer that gathers the datapack registry entries to generate for the subpack.
    /// @return The subpack provider gatherer.
    public PackGatherer.WithRegistries subpack(String path, Consumer<GlobalDatapackRegistryGatherer> gatherer) {
        var providerPrefix = this.nextProviderPrefix();
        return RegistryPackGatherer.createFromRegistries(this.modContainer, providerPrefix, this.dataGenerator.getPackGenerator(true, providerPrefix, path), this.registries, gatherer);
    }

    /// Creates a subpack generator that generates the desired providers at the given output.
    ///
    /// Datapack registry entries generated for the subpack are ONLY available to the subpack
    /// providers. If the registry entry should be accessible from the main pack, add the entries
    /// via [GatherDataRegistryEntriesEvent].
    ///
    /// @param output   The output to generate the providers to.
    /// @param gatherer A consumer that gathers the datapack registry entries to generate for the subpack.
    /// @return The subpack provider gatherer.
    public PackGatherer.WithRegistries subpack(PackOutput output, Consumer<GlobalDatapackRegistryGatherer> gatherer) {
        var providerPrefix = this.nextProviderPrefix();
        return RegistryPackGatherer.createFromRegistries(this.modContainer, providerPrefix, this.dataGenerator.getPackGenerator(true, providerPrefix, output), this.registries, gatherer);
    }

    private String nextProviderPrefix() {
        return this.modContainer.getModId() + "-pack[" + (packCounter++) + "]";
    }

    @Override
    public CompletableFuture<HolderLookup.Provider> registries() {
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
        public Server(ModContainer mc, DataGenerator dataGenerator, PackGatherer mainPack, DataGeneratorConfig dataGeneratorConfig, DatapackRegistrySets<CompletableFuture<HolderLookup.Provider>> registries) {
            super(mc, dataGenerator, mainPack, dataGeneratorConfig, registries);
        }
    }

    public static final class Client extends GatherDataEvent {
        @ApiStatus.Internal
        public Client(ModContainer mc, DataGenerator dataGenerator, PackGatherer mainPack, DataGeneratorConfig dataGeneratorConfig, DatapackRegistrySets<CompletableFuture<HolderLookup.Provider>> registries) {
            super(mc, dataGenerator, mainPack, dataGeneratorConfig, registries);
        }
    }

    @Override
    public <T extends DataProvider> T addProvider(DataProvider.Factory<T> factory) {
        return this.mainPack.addProvider(factory);
    }

    @ApiStatus.Internal
    @FunctionalInterface
    public interface GatherDataEventGenerator {
        GatherDataEvent create(final ModContainer mc, final DataGenerator dataGenerator, final PackGatherer mainPack, final DataGeneratorConfig dataGeneratorConfig, final DatapackRegistrySets<CompletableFuture<HolderLookup.Provider>> registries);
    }
}
