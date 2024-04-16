/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

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
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.registries.GameData;
import net.neoforged.neoforge.registries.RegistryManager;

/**
 * Internal class for handling the steps of mod loading that are common for client, data and server runs.
 */
public abstract class CommonModLoader {
    private static boolean registriesLoaded = false;

    public static boolean areRegistriesLoaded() {
        return registriesLoaded;
    }

    protected static void begin(Runnable periodicTask) {
        var syncExecutor = ModWorkManager.syncExecutor();

        ModLoader.get().gatherAndInitializeMods(syncExecutor, ModWorkManager.parallelExecutor(), periodicTask);

        ModLoader.get().runInitTask("Registry initialization", syncExecutor, periodicTask, () -> {
            RegistryManager.postNewRegistryEvent();
            GameData.unfreezeData();
            GameData.postRegisterEvents();
            GameData.freezeData();
            registriesLoaded = true;
        });
    }

    protected static void load(Executor syncExecutor, Executor parallelExecutor) {
        Runnable periodicTask = () -> {}; // no need to pass something else for now

        if (!ModLoader.isLoadingStateValid()) {
            return;
        }

        ModLoader.get().runInitTask("Config loading", syncExecutor, periodicTask, () -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.CLIENT, FMLPaths.CONFIGDIR.get());
            }
            ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.COMMON, FMLPaths.CONFIGDIR.get());
        });

        ModLoader.get().dispatchParallelEvent("Common setup", syncExecutor, parallelExecutor, periodicTask, FMLCommonSetupEvent::new);
        ModLoader.get().dispatchParallelEvent("Sided setup", syncExecutor, parallelExecutor, periodicTask,
                FMLEnvironment.dist.isClient() ? FMLClientSetupEvent::new : FMLDedicatedServerSetupEvent::new);

        ModLoader.get().runInitTask("Registration events", syncExecutor, periodicTask, RegistrationEvents::init);
    }

    protected static void finish(Executor syncExecutor, Executor parallelExecutor) {
        Runnable periodicTask = () -> {}; // no need to pass something else for now

        if (!ModLoader.isLoadingStateValid()) {
            return;
        }

        ModLoader.get().dispatchParallelEvent("Enqueue IMC", syncExecutor, parallelExecutor, periodicTask, InterModEnqueueEvent::new);
        ModLoader.get().dispatchParallelEvent("Process IMC", syncExecutor, parallelExecutor, periodicTask, InterModProcessEvent::new);
        ModLoader.get().dispatchParallelEvent("Complete loading of %d mods".formatted(ModList.get().size()), syncExecutor, parallelExecutor, periodicTask, FMLLoadCompleteEvent::new);

        ModLoader.get().runInitTask("Network registry lock", syncExecutor, periodicTask, NetworkRegistry.getInstance()::setup);
    }
}
