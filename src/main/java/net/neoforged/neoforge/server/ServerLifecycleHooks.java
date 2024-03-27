/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class ServerLifecycleHooks {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker SERVERHOOKS = MarkerManager.getMarker("SERVERHOOKS");
    private static final LevelResource SERVERCONFIG = new LevelResource("serverconfig");
    private static volatile CountDownLatch exitLatch = null;
    private static MinecraftServer currentServer;

    private static Path getServerConfigPath(final MinecraftServer server) {
        final Path serverConfig = server.getWorldPath(SERVERCONFIG);
        if (!Files.isDirectory(serverConfig)) {
            try {
                Files.createDirectories(serverConfig);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        final Path explanation = serverConfig.resolve("readme.txt");
        if (!Files.exists(explanation)) {
            try {
                Files.writeString(explanation, """
                        Any server configs put in this folder will override the corresponding server config from <instance path>/config/<config path>.
                        If the config being transferred is in a subfolder of the base config folder make sure to include that folder here in the path to the file you are overwriting.
                        For example if you are overwriting a config with the path <instance path>/config/ExampleMod/config-server.toml, you would need to put it in serverconfig/ExampleMod/config-server.toml
                        """, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return serverConfig;
    }

    public static void handleServerAboutToStart(final MinecraftServer server) {
        currentServer = server;
        // on the dedi server we need to force the stuff to setup properly
        LogicalSidedProvider.setServer(() -> server);
        ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.SERVER, FMLPaths.CONFIGDIR.get(), getServerConfigPath(server));
        runModifiers(server);
        NeoForge.EVENT_BUS.post(new ServerAboutToStartEvent(server));
    }

    public static void handleServerStarting(final MinecraftServer server) {
        if (FMLEnvironment.dist.isDedicatedServer()) {
            LanguageHook.loadModLanguages(server);
            // GameTestServer requires the gametests to be registered earlier, so it is done in main and should not be done twice.
            if (!(server instanceof GameTestServer))
                GameTestHooks.registerGametests();
        }
        PermissionAPI.initializePermissionAPI();
        NeoForge.EVENT_BUS.post(new ServerStartingEvent(server));
    }

    public static void handleServerStarted(final MinecraftServer server) {
        NeoForge.EVENT_BUS.post(new ServerStartedEvent(server));
    }

    public static void handleServerStopping(final MinecraftServer server) {
        NeoForge.EVENT_BUS.post(new ServerStoppingEvent(server));
    }

    public static void expectServerStopped() {
        exitLatch = new CountDownLatch(1);
    }

    public static void handleServerStopped(final MinecraftServer server) {
        if (!server.isDedicatedServer()) RegistryManager.revertToFrozen();
        NeoForge.EVENT_BUS.post(new ServerStoppedEvent(server));
        currentServer = null;
        LogicalSidedProvider.setServer(null);
        CountDownLatch latch = exitLatch;

        if (latch != null) {
            latch.countDown();
            exitLatch = null;
        }
        ConfigTracker.INSTANCE.unloadConfigs(ModConfig.Type.SERVER);
    }

    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }

    public static void handleExit(int retVal) {
        System.exit(retVal);
    }

    private static void runModifiers(final MinecraftServer server) {
        final RegistryAccess registries = server.registryAccess();

        // The order of holders() is the order modifiers were loaded in.
        final List<BiomeModifier> biomeModifiers = registries.registryOrThrow(NeoForgeRegistries.Keys.BIOME_MODIFIERS)
                .holders()
                .map(Holder::value)
                .toList();
        final List<StructureModifier> structureModifiers = registries.registryOrThrow(Keys.STRUCTURE_MODIFIERS)
                .holders()
                .map(Holder::value)
                .toList();

        final Set<EntityType<?>> entitiesWithoutPlacements = new HashSet<>();

        // Apply sorted biome modifiers to each biome.
        registries.registryOrThrow(Registries.BIOME).holders().forEach(biomeHolder -> {
            final Biome biome = biomeHolder.value();
            biome.modifiableBiomeInfo().applyBiomeModifiers(biomeHolder, biomeModifiers);

            final MobSpawnSettings mobSettings = biome.getMobSettings();
            mobSettings.getSpawnerTypes().forEach(category -> {
                mobSettings.getMobs(category).unwrap().forEach(data -> {
                    if (SpawnPlacements.hasPlacement(data.type)) return;
                    entitiesWithoutPlacements.add(data.type);
                });
            });
        });
        // Apply sorted structure modifiers to each structure.
        registries.registryOrThrow(Registries.STRUCTURE).holders().forEach(structureHolder -> {
            structureHolder.value().modifiableStructureInfo().applyStructureModifiers(structureHolder, structureModifiers);
        });

        if (!entitiesWithoutPlacements.isEmpty() && !FMLLoader.isProduction()) {
            LOGGER.error("The following entities have not registered to the SpawnPlacementRegisterEvent, but a spawn entry was found. This will mean that the entity doesn't have restrictions on its spawn location, please register a spawn placement for the entity, you can register with NO_RESTRICTIONS if you don't want any restrictions."
                    + entitiesWithoutPlacements.stream().map(EntityType::getKey).map(ResourceLocation::toString).collect(Collectors.joining("\n\t - ", "\n\t - ", "")));
        }
    }
}
