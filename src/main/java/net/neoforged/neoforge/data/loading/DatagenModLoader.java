/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.loading;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.packs.PackResources;
import net.minecraft.util.Util;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.data.event.DataGeneratorConfig;
import net.neoforged.neoforge.data.event.DatapackRegistryGatherer;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;
import net.neoforged.neoforge.data.event.GlobalDatapackRegistryGatherer;
import net.neoforged.neoforge.internal.CommonModLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

public class DatagenModLoader extends CommonModLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static DataGeneratorConfig dataGeneratorConfig;
    private static boolean runningDataGen;

    public static boolean isRunningDataGen() {
        return runningDataGen;
    }

    @ApiStatus.Internal
    public static void begin(
            final Set<String> mods,
            final Path path,
            final Collection<Path> inputs,
            Collection<Path> existingPacks,
            final boolean devToolGenerators,
            final boolean reportsGenerator,
            final boolean structureValidator,
            final boolean flat,
            boolean uncached,
            Runnable setup,
            GatherDataEvent.GatherDataEventGenerator eventGenerator,
            DataGenerator vanillaGenerator,
            Consumer<Consumer<PackResources>> vanillaClientAssets) {
        if (mods.contains("minecraft") && mods.size() == 1)
            return;
        LOGGER.info("Initializing Data Gatherer for mods {}", mods);
        runningDataGen = true;
        Bootstrap.bootStrap();
        begin(() -> {}, true);
        CompletableFuture<HolderLookup.Provider> worldLookupProvider = CompletableFuture.supplyAsync(VanillaRegistries::createWorldLookup, Util.backgroundExecutor());
        dataGeneratorConfig = new DataGeneratorConfig(mods, path, inputs, worldLookupProvider, devToolGenerators, reportsGenerator, structureValidator, flat, vanillaGenerator, existingPacks, vanillaClientAssets);
        setup.run();

        // Gather datapack registries first
        var mainGatherer = new DatapackRegistryGathererImpl(dataGeneratorConfig.getMods());
        Map<String, List<DatapackRegistryGathererImpl>> modRegistryProviders = new HashMap<>();
        for (ModContainer mod : ModList.get().getSortedMods()) {
            List<DatapackRegistryGathererImpl> registries = modRegistryProviders.computeIfAbsent(mod.getModId(), _ -> new ArrayList<>());
            var modGatherer = new DatapackRegistryGathererImpl(mainGatherer, mod.getModId(), Set.of(mod.getModId()));
            registries.add(modGatherer);
            mod.acceptEvent(new GatherDataRegistryEntriesEvent(mod, new GlobalDatapackRegistryGatherer() {
                @Override
                public DatapackRegistryGatherer gatherFor(String modId, String... modIds) {
                    var subGatherer = new DatapackRegistryGathererImpl(mainGatherer, mod.getModId(), Stream.concat(Stream.of(modId), Arrays.stream(modIds)).collect(Collectors.toSet()));
                    registries.add(subGatherer);
                    return subGatherer;
                }

                @Override
                public <T> DatapackRegistryGatherer add(ResourceKey<? extends Registry<T>> registryKey, SingleRegistryBootstrap<T> bootstrap) throws IllegalArgumentException {
                    return modGatherer.add(registryKey, bootstrap);
                }

                @Override
                public DatapackRegistryGatherer add(MultiRegistryBootstrap bootstrap) throws IllegalArgumentException {
                    return modGatherer.add(bootstrap);
                }
            }));
        }
        var registries = mainGatherer.createGlobal(worldLookupProvider);

        // Only fire the event for mods that have their generators enabled
        for (ModContainer mod : ModList.get().getSortedMods()) {
            if (dataGeneratorConfig.getMods().contains(mod.getModId())) {
                var generator = dataGeneratorConfig.makeGenerator(p -> dataGeneratorConfig.isFlat() ? p : p.resolve(mod.getModId()), uncached);

                // Add registry providers
                var registryProviders = modRegistryProviders.getOrDefault(mod.getModId(), Collections.emptyList());
                for (int i = 0; i < registryProviders.size(); ++i) {
                    registryProviders.get(i).createSub(generator, registries, i);
                }

                var event = eventGenerator.create(mod, generator, dataGeneratorConfig, registries);
                mod.acceptEvent(event);
            }
        }

        dataGeneratorConfig.runAll();
    }
}
