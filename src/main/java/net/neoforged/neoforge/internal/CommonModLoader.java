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
import net.neoforged.neoforge.client.loading.ClientModLoader;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.registries.GameData;
import net.neoforged.neoforge.registries.RegistryManager;

/**
 * Internal class for handling the steps of mod loading that are common for client, data and server runs.
 *
 * <p><ul>
 * <li>Client runs {@link #begin}, {@link #load} and {@link #finish} at different timings, see {@link ClientModLoader}.</li>
 * <li>Server runs all 3 consecutively.</li>
 * <li>Datagen only runs {@link #begin}.</li>
 * </ul>
 */
public abstract class CommonModLoader {
    private static boolean registriesLoaded = false;

    public static boolean areRegistriesLoaded() {
        return registriesLoaded;
    }

    protected static void begin(Runnable periodicTask) {
        var syncExecutor = ModWorkManager.syncExecutor();

        ModLoader.gatherAndInitializeMods(syncExecutor, ModWorkManager.parallelExecutor(), periodicTask);

        ModLoader.runInitTask("Registry initialization", syncExecutor, periodicTask, () -> {
            RegistryManager.postNewRegistryEvent();
            GameData.unfreezeData();
            GameData.postRegisterEvents();
            GameData.freezeData();
            registriesLoaded = true;
        });
    }

    protected static void load(Executor syncExecutor, Executor parallelExecutor) {
        Runnable periodicTask = () -> {}; // server: no progress screen; client: minecraft has already opened its loading screen and ticks it for us

        ModLoader.runInitTask("Config loading", syncExecutor, periodicTask, () -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.CLIENT, FMLPaths.CONFIGDIR.get());
            }
            ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.COMMON, FMLPaths.CONFIGDIR.get());
        });

        ModLoader.dispatchParallelEvent("Common setup", syncExecutor, parallelExecutor, periodicTask, FMLCommonSetupEvent::new);
        ModLoader.dispatchParallelEvent("Sided setup", syncExecutor, parallelExecutor, periodicTask,
                FMLEnvironment.dist.isClient() ? FMLClientSetupEvent::new : FMLDedicatedServerSetupEvent::new);

        ModLoader.runInitTask("Registration events", syncExecutor, periodicTask, RegistrationEvents::init);
    }

    protected static void finish(Executor syncExecutor, Executor parallelExecutor) {
        Runnable periodicTask = () -> {}; // server: no progress screen; client: minecraft has already opened its loading screen and ticks it for us

        ModLoader.dispatchParallelEvent("Enqueue IMC", syncExecutor, parallelExecutor, periodicTask, InterModEnqueueEvent::new);
        ModLoader.dispatchParallelEvent("Process IMC", syncExecutor, parallelExecutor, periodicTask, InterModProcessEvent::new);
        ModLoader.dispatchParallelEvent("Complete loading of %d mods".formatted(ModList.get().size()), syncExecutor, parallelExecutor, periodicTask, FMLLoadCompleteEvent::new);

        ModLoader.runInitTask("Network registry lock", syncExecutor, periodicTask, NetworkRegistry::setup);
    }
}
