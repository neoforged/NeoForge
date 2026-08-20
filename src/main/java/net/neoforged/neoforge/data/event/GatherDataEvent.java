/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.event;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public abstract class GatherDataEvent extends Event implements IModBusEvent {
    private final DataGenerator dataGenerator;
    private final DataGeneratorConfig config;
    private final ModContainer modContainer;

    @Nullable
    private CompletableFuture<HolderLookup.Provider> worldRegistriesWithModdedEntries = null;
    @Nullable
    private CompletableFuture<HolderLookup.Provider> reloadableRegistries = null;
    @Nullable
    private CompletableFuture<HolderLookup.Provider> reloadableRegistriesWithModdedEntries = null;

    public GatherDataEvent(final ModContainer mc, final DataGenerator dataGenerator, final DataGeneratorConfig dataGeneratorConfig) {
        this.modContainer = mc;
        this.dataGenerator = dataGenerator;
        this.config = dataGeneratorConfig;
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

    public static class Server extends GatherDataEvent {
        public Server(ModContainer mc, DataGenerator dataGenerator, DataGeneratorConfig dataGeneratorConfig) {
            super(mc, dataGenerator, dataGeneratorConfig);
        }
    }

    public static class Client extends GatherDataEvent {
        public Client(ModContainer mc, DataGenerator dataGenerator, DataGeneratorConfig dataGeneratorConfig) {
            super(mc, dataGenerator, dataGeneratorConfig);
        }
    }

    @ApiStatus.Internal
    public static class DataGeneratorConfig {
        private final Set<String> mods;
        private final Path path;
        private final Collection<Path> inputs;
        private final CompletableFuture<HolderLookup.Provider> worldLookupProvider;
        private final boolean dev;
        private final boolean reports;
        private final boolean validate;
        private final boolean flat;
        private final List<DataGenerator> generators = new ArrayList<>();
        private final ResourceManager clientResourceManager;
        private final ResourceManager serverResourceManager;

        public DataGeneratorConfig(
                final Set<String> mods,
                final Path path,
                final Collection<Path> inputs,
                final CompletableFuture<HolderLookup.Provider> worldLookupProvider,
                final boolean dev,
                final boolean reports,
                final boolean validate,
                final boolean flat,
                final DataGenerator vanillaGenerator,
                Collection<Path> existingPacks,
                Consumer<Consumer<PackResources>> vanillaClientAssets) {
            this.mods = mods;
            this.path = path;
            this.inputs = inputs;
            this.worldLookupProvider = worldLookupProvider;
            this.dev = dev;
            this.reports = reports;
            this.validate = validate;
            this.flat = flat;

            clientResourceManager = createResourceManager(PackType.CLIENT_RESOURCES, mods::contains, existingPacks, vanillaClientAssets);

            serverResourceManager = createResourceManager(PackType.SERVER_DATA, mods::contains, existingPacks, consumer -> consumer.accept(ServerPacksSource.createVanillaPackSource().fullResources()));

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

        public DataGenerator makeGenerator(final Function<Path, Path> pathEnhancer, boolean uncached) {
            final DataGenerator generator = uncached ? new DataGenerator.Uncached(pathEnhancer.apply(path)) : new DataGenerator.Cached(pathEnhancer.apply(path), DetectedVersion.tryDetectVersion(), true);
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

        private static ResourceManager createResourceManager(PackType packType, Predicate<String> isGeneratedMod, Collection<Path> existingPacks, Consumer<Consumer<PackResources>> consumer) {
            var packs = Lists.<PackResources>newArrayList();
            // include vanilla resource packs first
            consumer.accept(packs::add);

            // include existing packs
            existingPacks.forEach(path -> {
                var packInfo = new PackLocationInfo(path.getFileName().toString(), Component.empty(), PackSource.BUILT_IN, Optional.empty());
                packs.add(new PathPackResources(packInfo, path));
            });

            // include mod resources last
            PackFormat packVersion = SharedConstants.getCurrentVersion().packVersion(packType);
            ModList.get().getSortedMods().stream()
                    // ignore 'minecraft' mod, this is added via `[Server|Client]PackSource`
                    .filter(Predicate.not(mod -> mod.getModId().equals("minecraft")))
                    // ignore actively generated models, their resource packs should be included using `--existing <packPath>`
                    // this is to workaround accidentally including resources being actively generated
                    .filter(Predicate.not(mod -> isGeneratedMod.test(mod.getModId())))
                    .flatMap(mod -> {
                        var owningFile = mod.getModInfo().getOwningFile();
                        var packInfo = new PackLocationInfo("mod/" + mod.getModId(), Component.empty(), PackSource.BUILT_IN, Optional.empty());
                        Pack.ResourcesSupplier packSupplier = ResourcePackLoader.createPackForMod(owningFile);
                        Pack.Metadata metadata = Pack.readPackMetadata(packInfo, packSupplier, packVersion, packType);
                        return metadata != null ? packSupplier.openResources(packInfo, metadata) : null;
                    })
                    .filter(Objects::nonNull)
                    .forEach(packs::add);

            return new MultiPackResourceManager(packType, packs);
        }
    }

    public <T extends DataProvider> T addProvider(T provider) {
        return dataGenerator.addProvider(true, provider);
    }

    public <T extends DataProvider> T createProvider(DataProviderFromOutput<T> builder) {
        return addProvider(builder.create(dataGenerator.getPackOutput()));
    }

    public <T extends DataProvider> T createProvider(DataProviderFromOutputLookup<T> builder) {
        return addProvider(builder.create(dataGenerator.getPackOutput(), this.getReloadableLookupProvider()));
    }

    public void createBlockAndItemTags(DataProviderFromOutputLookup<TagsProvider<Block>> blockTagsProvider, ItemTagsProvider itemTagsProvider) {
        var blockTags = createProvider(blockTagsProvider);
        addProvider(itemTagsProvider.create(this.getGenerator().getPackOutput(), this.getReloadableLookupProvider(), blockTags.contentsGetter()));
    }

    public void createWorldRegistryObjects(RegistrySetBuilder entriesBuilder) {
        this.createWorldRegistryObjects(entriesBuilder, Set.of(this.modContainer.getModId()));
    }

    public void createWorldRegistryObjects(RegistrySetBuilder entriesBuilder, Set<String> modIds) {
        this.createWorldRegistryObjects(entriesBuilder, modIds, "world");
    }

    public void createWorldRegistryObjects(RegistrySetBuilder entriesBuilder, Set<String> modIds, String name) {
        var registries = this.createProvider((output) -> DatapackBuiltinEntriesProvider.forWorldLayer(output, name, getWorldLookupProvider(), entriesBuilder, modIds));
        this.worldRegistriesWithModdedEntries = registries.getRegistryProvider();
    }

    public void createReloadableRegistryObjects(RegistrySetBuilder entriesBuilder) {
        this.createReloadableRegistryObjects(entriesBuilder, Set.of(this.modContainer.getModId()));
    }

    public void createReloadableRegistryObjects(RegistrySetBuilder entriesBuilder, Set<String> modIds) {
        this.createReloadableRegistryObjects(entriesBuilder, modIds, "reloadable");
    }

    public void createReloadableRegistryObjects(RegistrySetBuilder entriesBuilder, Set<String> modIds, String name) {
        var registries = this.createProvider((output) -> DatapackBuiltinEntriesProvider.forReloadableLayer(output, name, getWorldLookupProvider(), getReloadableLookupProvider(), entriesBuilder, modIds));
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
    public interface GatherDataEventGenerator {
        GatherDataEvent create(final ModContainer mc, final DataGenerator dataGenerator, final DataGeneratorConfig dataGeneratorConfig);
    }

    @FunctionalInterface
    public interface ItemTagsProvider {
        TagsProvider<Item> create(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> contentsGetter);
    }
}
