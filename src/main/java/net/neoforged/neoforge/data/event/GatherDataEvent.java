/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.event;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.IModBusEvent;
import org.jspecify.annotations.Nullable;

public abstract sealed class GatherDataEvent extends Event implements IModBusEvent {
    private final DataGenerator dataGenerator;
    private final DataGeneratorConfig config;
    private final ModContainer modContainer;
    private final PackGenerator defaultPackGenerator;

    @Nullable
    CompletableFuture<HolderLookup.Provider> worldRegistriesWithModdedEntries = null;
    @Nullable
    CompletableFuture<HolderLookup.Provider> reloadableRegistries = null;
    @Nullable
    CompletableFuture<HolderLookup.Provider> reloadableRegistriesWithModdedEntries = null;

    public GatherDataEvent(final ModContainer mc, final DataGenerator dataGenerator, final DataGeneratorConfig dataGeneratorConfig) {
        this.modContainer = mc;
        this.dataGenerator = dataGenerator;
        this.config = dataGeneratorConfig;
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

    public CompletableFuture<HolderLookup.Provider> getWorldLookupProvider() {
        return Objects.requireNonNullElse(this.worldRegistriesWithModdedEntries, this.config.worldLookupProvider);
    }

    public CompletableFuture<HolderLookup.Provider> getReloadableLookupProvider() {
        if (this.reloadableRegistriesWithModdedEntries != null) {
            return this.reloadableRegistriesWithModdedEntries;
        }
        if (this.reloadableRegistries == null) {
            this.reloadableRegistries = getWorldLookupProvider().thenApply(VanillaRegistries::createReloadableLookup);
        }
        return this.reloadableRegistries;
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
        public Server(ModContainer mc, DataGenerator dataGenerator, DataGeneratorConfig dataGeneratorConfig) {
            super(mc, dataGenerator, dataGeneratorConfig);
        }
    }

    public static final class Client extends GatherDataEvent {
        public Client(ModContainer mc, DataGenerator dataGenerator, DataGeneratorConfig dataGeneratorConfig) {
            super(mc, dataGenerator, dataGeneratorConfig);
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

    public void createWorldRegistryObjects(RegistrySetBuilder entriesBuilder) {
        this.defaultPackGenerator.createWorldRegistryObjects(entriesBuilder);
    }

    public void createWorldRegistryObjects(RegistrySetBuilder entriesBuilder, Set<String> modIds) {
        this.defaultPackGenerator.createWorldRegistryObjects(entriesBuilder, modIds);
    }

    public void createWorldRegistryObjects(RegistrySetBuilder entriesBuilder, Set<String> modIds, String name) {
        this.defaultPackGenerator.createWorldRegistryObjects(entriesBuilder, modIds, name);
    }

    public void createReloadableRegistryObjects(RegistrySetBuilder entriesBuilder) {
        this.defaultPackGenerator.createReloadableRegistryObjects(entriesBuilder);
    }

    public void createReloadableRegistryObjects(RegistrySetBuilder entriesBuilder, Set<String> modIds) {
        this.defaultPackGenerator.createReloadableRegistryObjects(entriesBuilder, modIds);
    }

    public void createReloadableRegistryObjects(RegistrySetBuilder entriesBuilder, Set<String> modIds, String name) {
        this.defaultPackGenerator.createReloadableRegistryObjects(entriesBuilder, modIds, name);
    }

    @FunctionalInterface
    public interface GatherDataEventGenerator {
        GatherDataEvent create(final ModContainer mc, final DataGenerator dataGenerator, final DataGeneratorConfig dataGeneratorConfig);
    }
}
