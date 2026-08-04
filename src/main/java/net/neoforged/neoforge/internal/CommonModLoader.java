/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import java.nio.file.Path;
import java.util.concurrent.Executor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModWorkManager;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.loading.LoadingConfig;
import net.neoforged.neoforge.loading.adapt.BreakingChangesDatabase;
import net.neoforged.neoforge.loading.adapt.CompatPrecheck;
import net.neoforged.neoforge.loading.cache.ModIndexCache;
import net.neoforged.neoforge.loading.perf.LoadingPerf;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.registries.GameData;
import net.neoforged.neoforge.registries.RegistryManager;
import org.jetbrains.annotations.ApiStatus;

/**
 * Internal class for handling the steps of mod loading that are common for client, data and server runs.
 *
 * <p><ul>
 * <li>Client runs {@link #begin} and {@link #load} at different timings, see {@code ClientModLoader}.</li>
 * <li>Server runs both consecutively.</li>
 * <li>Datagen only runs {@link #begin}.</li>
 * </ul>
 *
 * <p>Loading stages are timed by {@link LoadingPerf}; the compatibility precheck
 * ({@link CompatPrecheck}) runs between mod initialization and event dispatch so that incompatible
 * mods are rejected with a clear diagnostic instead of failing mid-event.</p>
 */
@ApiStatus.Internal
public abstract class CommonModLoader {
    private static boolean registriesLoaded = false;

    public static boolean areRegistriesLoaded() {
        return registriesLoaded;
    }

    protected static void begin(Runnable periodicTask, boolean datagen) {
        var syncExecutor = ModWorkManager.syncExecutor();
        var perf = LoadingPerf.get();

        Path gameDir = FMLLoader.getCurrent().getGameDir();
        LoadingConfig config = LoadingConfig.getOrNull();
        if (config == null) {
            config = LoadingConfig.load(gameDir, perfFlag());
        }
        ModIndexCache indexCache = config.enableIndexCache ? ModIndexCache.initialize(gameDir, config, perf) : null;

        try (var ignored = perf.stage("Discovery & initialization")) {
            ModLoader.gatherAndInitializeMods(syncExecutor, ModWorkManager.parallelExecutor(), periodicTask);
        }

        if (config.compatPrecheck) {
            try (var ignored = perf.stage("Compatibility precheck")) {
                CompatPrecheck.runAndGate(ModList.get(), indexCache, BreakingChangesDatabase.load(gameDir), config, gameDir);
            }
        }

        try (var ignored = perf.stage("Registry initialization")) {
            ModLoader.runInitTask("Registry initialization", syncExecutor, periodicTask, () -> {
                RegistryManager.postNewRegistryEvent();
                GameData.unfreezeData();
                GameData.postRegisterEvents();
                GameData.freezeData();
                registriesLoaded = true;
            });
        }

        if (!datagen) {
            try (var ignored = perf.stage("Config loading")) {
                ModLoader.runInitTask("Config loading", syncExecutor, periodicTask, () -> {
                    if (FMLEnvironment.getDist() == Dist.CLIENT) {
                        ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.CLIENT, FMLPaths.CONFIGDIR.get());
                    }
                    ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.COMMON, FMLPaths.CONFIGDIR.get());
                });
            }
        }

        NeoForge.EVENT_BUS.start();
    }

    protected static void load(Runnable periodicTask) {
        Executor syncExecutor = ModWorkManager.syncExecutor();
        Executor parallelExecutor = ModWorkManager.parallelExecutor();
        var perf = LoadingPerf.get();

        try (var ignored = perf.stage("Common setup")) {
            ModLoader.dispatchParallelEvent("Common setup", syncExecutor, parallelExecutor, periodicTask, FMLCommonSetupEvent::new);
        }
        try (var ignored = perf.stage("Sided setup")) {
            ModLoader.dispatchParallelEvent("Sided setup", syncExecutor, parallelExecutor, periodicTask,
                    FMLEnvironment.getDist().isClient() ? FMLClientSetupEvent::new : FMLDedicatedServerSetupEvent::new);
        }

        try (var ignored = perf.stage("Registration events")) {
            ModLoader.runInitTask("Registration events", syncExecutor, periodicTask, RegistrationEvents::init);
        }

        try (var ignored = perf.stage("Enqueue IMC")) {
            ModLoader.dispatchParallelEvent("Enqueue IMC", syncExecutor, parallelExecutor, periodicTask, InterModEnqueueEvent::new);
        }
        try (var ignored = perf.stage("Process IMC")) {
            ModLoader.dispatchParallelEvent("Process IMC", syncExecutor, parallelExecutor, periodicTask, InterModProcessEvent::new);
        }
        try (var ignored = perf.stage("Complete loading of mods")) {
            ModLoader.dispatchParallelEvent("Complete loading of %d mods".formatted(ModList.get().size()), syncExecutor, parallelExecutor, periodicTask, FMLLoadCompleteEvent::new);
        }

        try (var ignored = perf.stage("Network registry lock")) {
            ModLoader.runInitTask("Network registry lock", syncExecutor, periodicTask, NetworkRegistry::setup);
        }

        perf.finish(FMLLoader.getCurrent().getGameDir(), LoadingConfig.get());
    }

    private static boolean perfFlag() {
        var loader = FMLLoader.getCurrent();
        if (loader == null) {
            return false;
        }
        for (String argument : loader.getProgramArgs().getArguments()) {
            if ("--perf".equals(argument)) {
                return true;
            }
        }
        return false;
    }
}
