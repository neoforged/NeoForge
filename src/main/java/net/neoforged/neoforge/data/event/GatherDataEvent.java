/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.event;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.IModBusEvent;

// TODO-ASH: Document
public abstract sealed class GatherDataEvent extends Event implements IModBusEvent {
    private final ModContainer modContainer;

    public GatherDataEvent(final ModContainer mc) {
        this.modContainer = mc;
    }

    public ModContainer getModContainer() {
        return this.modContainer;
    }

    public static final class Registries extends GatherDataEvent implements GlobalDatapackRegistryGatherer {
        private final GlobalDatapackRegistryGatherer delegate;

        public Registries(ModContainer mc, GlobalDatapackRegistryGatherer delegate) {
            super(mc);
            this.delegate = delegate;
        }

        @Override
        public DatapackRegistryGatherer generateFor(String modId, String... modIds) {
            return this.delegate.generateFor(modId, modIds);
        }

        @Override
        public <T> DatapackRegistryGatherer add(ResourceKey<? extends Registry<T>> registryKey, SingleRegistryBootstrap<T> bootstrap) throws IllegalArgumentException {
            return this.delegate.add(registryKey, bootstrap);
        }

        @Override
        public <T> DatapackRegistryGatherer add(MultiRegistryBootstrap bootstrap) throws IllegalArgumentException {
            return this.delegate.add(bootstrap);
        }
    }

    public static abstract sealed class Providers extends GatherDataEvent {
        private final DataGenerator dataGenerator;
        private final DataGeneratorConfig config;
        private final PackGenerator defaultPackGenerator;
        private final RegistrySets<CompletableFuture<HolderLookup.Provider>> registries;

        public Providers(ModContainer mc, DataGenerator dataGenerator, DataGeneratorConfig dataGeneratorConfig, RegistrySets<CompletableFuture<HolderLookup.Provider>> registries) {
            super(mc);
            this.dataGenerator = dataGenerator;
            this.config = dataGeneratorConfig;
            this.registries = registries;
            this.defaultPackGenerator = new PackGenerator(this, dataGenerator.getPackOutput());
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

        public CompletableFuture<HolderLookup.Provider> getWorldLookupProvider() {
            return this.registries.worldAndDimension();
        }

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
    }

    public static final class Server extends Providers {
        public Server(ModContainer mc, DataGenerator dataGenerator, DataGeneratorConfig dataGeneratorConfig, RegistrySets<CompletableFuture<HolderLookup.Provider>> registries) {
            super(mc, dataGenerator, dataGeneratorConfig, registries);
        }
    }

    public static final class Client extends Providers {
        public Client(ModContainer mc, DataGenerator dataGenerator, DataGeneratorConfig dataGeneratorConfig, RegistrySets<CompletableFuture<HolderLookup.Provider>> registries) {
            super(mc, dataGenerator, dataGeneratorConfig, registries);
        }
    }

    @FunctionalInterface
    public interface GatherDataEventGenerator {
        GatherDataEvent create(final ModContainer mc, final DataGenerator dataGenerator, final DataGeneratorConfig dataGeneratorConfig, final RegistrySets<CompletableFuture<HolderLookup.Provider>> registries);
    }
}
