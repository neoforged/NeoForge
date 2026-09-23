/*
 * Copyright (c) NeoForged and contributors
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
import net.minecraft.data.DataGenerator;
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
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class DataGeneratorConfig {
    private final Set<String> mods;
    private final Path path;
    private final Collection<Path> inputs;
    final CompletableFuture<HolderLookup.Provider> worldLookupProvider;
    final boolean dev;
    final boolean reports;
    final boolean validate;
    private final boolean flat;
    private final List<DataGenerator> generators = new ArrayList<>();
    final ResourceManager clientResourceManager;
    final ResourceManager serverResourceManager;

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
            for (int x = 1; x < lst.size(); x++) {
                parent.merge(lst.get(x));
            }
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
