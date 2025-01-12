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
import net.minecraft.data.DataGenerator;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.internal.CommonModLoader;
import net.neoforged.neoforge.internal.RegistrationEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class DatagenModLoader extends CommonModLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static GatherDataEvent.DataGeneratorConfig dataGeneratorConfig;
    private static boolean runningDataGen;

    public static boolean isRunningDataGen() {
        return runningDataGen;
    }

    @ApiStatus.Internal
    public static void begin(final Set<String> mods, final Path path, final Collection<Path> inputs, Collection<Path> existingPacks,
            final boolean devToolGenerators, final boolean reportsGenerator,
            final boolean structureValidator, final boolean flat, @Nullable final String assetIndex, @Nullable final File assetsDir, Runnable setup, GatherDataEvent.GatherDataEventGenerator eventGenerator,
            DataGenerator vanillaGenerator) {
        if (mods.contains("minecraft") && mods.size() == 1)
            return;
        LOGGER.info("Initializing Data Gatherer for mods {}", mods);
        runningDataGen = true;
        Bootstrap.bootStrap();
        begin(() -> {}, true);
        // Modify components as the (modified) defaults may be required in datagen, i.e. stack size
        RegistrationEvents.modifyComponents();
        CompletableFuture<HolderLookup.Provider> lookupProvider = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());
        dataGeneratorConfig = new GatherDataEvent.DataGeneratorConfig(mods, path, inputs, lookupProvider, devToolGenerators, reportsGenerator, structureValidator, flat, vanillaGenerator, assetIndex, assetsDir, existingPacks);
        setup.run();
        ModLoader.runEventGenerator(mc -> eventGenerator.create(mc, dataGeneratorConfig.makeGenerator(p -> dataGeneratorConfig.isFlat() ? p : p.resolve(mc.getModId()),
                dataGeneratorConfig.getMods().contains(mc.getModId())), dataGeneratorConfig));
        dataGeneratorConfig.runAll();
    }
}
