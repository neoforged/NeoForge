/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.loading;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.internal.CommonModLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatagenModLoader extends CommonModLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static GatherDataEvent.DataGeneratorConfig dataGeneratorConfig;
    private static ExistingFileHelper existingFileHelper;
    private static boolean runningDataGen;

    public static boolean isRunningDataGen() {
        return runningDataGen;
    }

    public static void begin(final Set<String> mods, final Path path, final Collection<Path> inputs, Collection<Path> existingPacks,
            Set<String> existingMods, final boolean serverGenerators, final boolean clientGenerators, final boolean devToolGenerators, final boolean reportsGenerator,
            final boolean structureValidator, final boolean flat, final String assetIndex, final File assetsDir) {
        if (mods.contains("minecraft") && mods.size() == 1)
            return;
        LOGGER.info("Initializing Data Gatherer for mods {}", mods);
        runningDataGen = true;
        Bootstrap.bootStrap();
        begin(() -> {});
        CompletableFuture<HolderLookup.Provider> lookupProvider = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());
        dataGeneratorConfig = new GatherDataEvent.DataGeneratorConfig(mods, path, inputs, lookupProvider, serverGenerators,
                clientGenerators, devToolGenerators, reportsGenerator, structureValidator, flat);
        if (!mods.contains("neoforge")) {
            // If we aren't generating data for forge, automatically add forge as an existing so mods can access forge's data
            existingMods.add("neoforge");
        }
        if (clientGenerators) {
            ClientHooks.registerSpriteSourceTypes();
        }
        existingFileHelper = new ExistingFileHelper(existingPacks, existingMods, structureValidator, assetIndex, assetsDir);
        ModLoader.runEventGenerator(mc -> new GatherDataEvent(mc, dataGeneratorConfig.makeGenerator(p -> dataGeneratorConfig.isFlat() ? p : p.resolve(mc.getModId()),
                dataGeneratorConfig.getMods().contains(mc.getModId())), dataGeneratorConfig, existingFileHelper));
        dataGeneratorConfig.runAll();
    }
}
