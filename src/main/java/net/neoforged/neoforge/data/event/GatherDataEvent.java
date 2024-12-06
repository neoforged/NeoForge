/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.event;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public abstract class GatherDataEvent extends Event implements IModBusEvent {
    private final DataGenerator dataGenerator;
    private final DataGeneratorConfig config;
    private final ExistingFileHelper existingFileHelper;
    private final ModContainer modContainer;

    public GatherDataEvent(final ModContainer mc, final DataGenerator dataGenerator, final DataGeneratorConfig dataGeneratorConfig, ExistingFileHelper existingFileHelper) {
        this.modContainer = mc;
        this.dataGenerator = dataGenerator;
        this.config = dataGeneratorConfig;
        this.existingFileHelper = existingFileHelper;
    }

    public ModContainer getModContainer() {
        return this.modContainer;
    }

    public Collection<Path> getInputs() {
        return this.config.getInputs();
    }

    public DataGenerator getGenerator() {
        return this.dataGenerator;
    }

    public ExistingFileHelper getExistingFileHelper() {
        return existingFileHelper;
    }

    public CompletableFuture<HolderLookup.Provider> getLookupProvider() {
        return this.config.lookupProvider;
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

    public static class Server extends GatherDataEvent {
        public Server(ModContainer mc, DataGenerator dataGenerator, DataGeneratorConfig dataGeneratorConfig, ExistingFileHelper existingFileHelper) {
            super(mc, dataGenerator, dataGeneratorConfig, existingFileHelper);
        }
    }

    public static class Client extends GatherDataEvent {
        public Client(ModContainer mc, DataGenerator dataGenerator, DataGeneratorConfig dataGeneratorConfig, ExistingFileHelper existingFileHelper) {
            super(mc, dataGenerator, dataGeneratorConfig, existingFileHelper);
        }
    }

    public static class DataGeneratorConfig {
        private final Set<String> mods;
        private final Path path;
        private final Collection<Path> inputs;
        private final CompletableFuture<HolderLookup.Provider> lookupProvider;
        private final boolean dev;
        private final boolean reports;
        private final boolean validate;
        private final boolean flat;
        private final List<DataGenerator> generators = new ArrayList<>();

        public DataGeneratorConfig(final Set<String> mods, final Path path, final Collection<Path> inputs, final CompletableFuture<HolderLookup.Provider> lookupProvider,
                final boolean dev, final boolean reports, final boolean validate, final boolean flat, final DataGenerator vanillaGenerator) {
            this.mods = mods;
            this.path = path;
            this.inputs = inputs;
            this.lookupProvider = lookupProvider;
            this.dev = dev;
            this.reports = reports;
            this.validate = validate;
            this.flat = flat;
            if (mods.contains("minecraft") || mods.isEmpty()) {
                this.generators.add(vanillaGenerator);
            }
        }

        public Collection<Path> getInputs() {
            return this.inputs;
        }

        public Set<String> getMods() {
            return mods;
        }

        public boolean isFlat() {
            return flat || getMods().size() == 1;
        }

        public DataGenerator makeGenerator(final Function<Path, Path> pathEnhancer, final boolean shouldExecute) {
            final DataGenerator generator = new DataGenerator(pathEnhancer.apply(path), DetectedVersion.tryDetectVersion(), shouldExecute);
            if (shouldExecute)
                generators.add(generator);
            return generator;
        }

        public void runAll() {
            Map<Path, List<DataGenerator>> paths = generators.stream().collect(Collectors.groupingBy(gen -> gen.getPackOutput().getOutputFolder(), LinkedHashMap::new, Collectors.toList()));

            paths.values().forEach(lst -> {
                DataGenerator parent = lst.get(0);
                for (int x = 1; x < lst.size(); x++)
                    parent.merge(lst.get(x));
                try {
                    parent.run();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        }
    }

    public <T extends DataProvider> T addProvider(T provider) {
        return dataGenerator.addProvider(true, provider);
    }

    public <T extends DataProvider> T createProvider(DataProviderFromOutput<T> builder) {
        return addProvider(builder.create(dataGenerator.getPackOutput()));
    }

    public <T extends DataProvider> T createProvider(DataProviderFromOutputFileHelper<T> builder) {
        return addProvider(builder.create(dataGenerator.getPackOutput(), existingFileHelper));
    }

    public <T extends DataProvider> T createProvider(DataProviderFromOutputLookup<T> builder) {
        return addProvider(builder.create(dataGenerator.getPackOutput(), config.lookupProvider));
    }

    public <T extends DataProvider> T createProvider(DataProviderFromOutputLookupFileHelper<T> builder) {
        return addProvider(builder.create(dataGenerator.getPackOutput(), config.lookupProvider, existingFileHelper));
    }

    public void createBlockAndItemTags(DataProviderFromOutputLookupFileHelper<TagsProvider<Block>> blockTagsProvider, ItemTagsProvider itemTagsProvider) {
        var blockTags = createProvider(blockTagsProvider);
        addProvider(itemTagsProvider.create(this.getGenerator().getPackOutput(), this.getLookupProvider(), blockTags.contentsGetter(), this.getExistingFileHelper()));
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
    public interface DataProviderFromOutputFileHelper<T extends DataProvider> {
        T create(PackOutput output, ExistingFileHelper existingFileHelper);
    }

    @FunctionalInterface
    public interface DataProviderFromOutputLookupFileHelper<T extends DataProvider> {
        T create(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper);
    }

    @FunctionalInterface
    public interface GatherDataEventGenerator {
        GatherDataEvent create(final ModContainer mc, final DataGenerator dataGenerator, final DataGeneratorConfig dataGeneratorConfig, ExistingFileHelper existingFileHelper);
    }

    @FunctionalInterface
    public interface ItemTagsProvider {
        TagsProvider<Item> create(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> contentsGetter, ExistingFileHelper existingFileHelper);
    }
}
