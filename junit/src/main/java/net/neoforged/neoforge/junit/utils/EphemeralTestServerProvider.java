/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.junit.utils;

import com.google.common.base.Stopwatch;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;

public class EphemeralTestServerProvider implements ParameterResolver, Extension {
    public static final AtomicReference<MinecraftServer> SERVER = new AtomicReference<>();
    public static final AtomicBoolean IN_CONSTRUCTION = new AtomicBoolean();

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == MinecraftServer.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return grabServer();
    }

    public static MinecraftServer grabServer() {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            return ServerLifecycleHooks.getCurrentServer();
        }

        if (!IN_CONSTRUCTION.get()) {
            IN_CONSTRUCTION.set(true);
            try {
                final var tempDir = Files.createTempDirectory("test-mc-server-");
                LevelStorageSource storage = LevelStorageSource.createDefault(tempDir.resolve("world"));
                LevelStorageSource.LevelStorageAccess storageAccess = storage.validateAndCreateAccess("main");
                PackRepository packrepository = ServerPacksSource.createPackRepository(storageAccess);
                final MinecraftServer server = MinecraftServer.spin(
                        thread -> JUnitServer.create(thread, tempDir, storageAccess, packrepository));

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    server.stopServer();
                    LogManager.shutdown();
                }));
            } catch (Exception ex) {
                LogUtils.getLogger().error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", ex);
                throw new RuntimeException(ex);
            }
        }

        while (SERVER.get() == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return ServerLifecycleHooks.getCurrentServer();
    }

    public static class JUnitServer extends MinecraftServer {
        private static final Logger LOGGER = LogUtils.getLogger();
        private static final Services NO_SERVICES = new Services(null, ServicesKeySet.EMPTY, null, null);
        private static final GameRules TEST_GAME_RULES = Util.make(new GameRules(), rules -> {
            rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
            rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
        });
        private static final WorldOptions WORLD_OPTIONS = new WorldOptions(0L, false, false);

        public static JUnitServer create(
                Thread thread, Path tempDir, LevelStorageSource.LevelStorageAccess access, PackRepository resources) {
            resources.reload();
            WorldDataConfiguration config = new WorldDataConfiguration(
                    new DataPackConfig(new ArrayList<>(resources.getAvailableIds()), List.of()), FeatureFlags.REGISTRY.allFlags());
            LevelSettings levelsettings = new LevelSettings(
                    "Test Level", GameType.CREATIVE, false, Difficulty.NORMAL, true, TEST_GAME_RULES, config);
            WorldLoader.PackConfig worldloader$packconfig = new WorldLoader.PackConfig(resources, config, false, true);
            WorldLoader.InitConfig worldloader$initconfig = new WorldLoader.InitConfig(worldloader$packconfig, Commands.CommandSelection.DEDICATED, 4);

            try {
                LOGGER.debug("Starting resource loading");
                Stopwatch stopwatch = Stopwatch.createStarted();
                WorldStem worldstem = Util.blockUntilDone(
                        exec -> WorldLoader.load(
                                worldloader$initconfig,
                                ctx -> {
                                    Registry<LevelStem> registry = new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.stable()).freeze();
                                    WorldDimensions.Complete worlddimensions$complete = ctx.datapackWorldgen()
                                            .registryOrThrow(Registries.WORLD_PRESET)
                                            .getHolderOrThrow(WorldPresets.FLAT)
                                            .value()
                                            .createWorldDimensions()
                                            .bake(registry);
                                    return new WorldLoader.DataLoadOutput<>(
                                            new PrimaryLevelData(
                                                    levelsettings, WORLD_OPTIONS, worlddimensions$complete.specialWorldProperty(), worlddimensions$complete.lifecycle()),
                                            worlddimensions$complete.dimensionsRegistryAccess());
                                },
                                WorldStem::new,
                                Util.backgroundExecutor(),
                                exec))
                        .get();
                stopwatch.stop();
                LOGGER.debug("Finished resource loading after {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new JUnitServer(thread, access, resources, worldstem, tempDir);
            } catch (Exception exception) {
                LOGGER.warn("Failed to load vanilla datapack, bit oops", exception);
                System.exit(-1);
                throw new IllegalStateException();
            }
        }

        private final Path tempDir;

        public JUnitServer(
                Thread thread,
                LevelStorageSource.LevelStorageAccess access,
                PackRepository pack,
                WorldStem stem,
                Path tempDir) {
            super(thread, access, pack, stem, Proxy.NO_PROXY, DataFixers.getDataFixer(), NO_SERVICES, LoggerChunkProgressListener::new);
            this.tempDir = tempDir;
        }

        @Override
        public boolean initServer() {
            this.setPlayerList(new PlayerList(this, this.registries(), this.playerDataStorage, 1) {});
            net.neoforged.neoforge.server.ServerLifecycleHooks.handleServerAboutToStart(this);
            LOGGER.info("Started ephemeral JUnit server");
            net.neoforged.neoforge.server.ServerLifecycleHooks.handleServerStarting(this);
            return true;
        }

        @Override
        public void tickServer(BooleanSupplier sup) {
            super.tickServer(sup);
            // Consider the server started the first time it ticks
            SERVER.set(this);
        }

        @Override
        public boolean saveEverything(boolean p_195515_, boolean p_195516_, boolean p_195517_) {
            // The server is ephemeral
            return false;
        }

        @Override
        public void stopServer() {
            LOGGER.info("Stopping server");
            this.getConnection().stop();
            getPlayerList().removeAll();

            try {
                storageSource.deleteLevel();
                this.storageSource.close();

                Files.delete(tempDir);
            } catch (IOException ioexception) {
                LOGGER.error("Failed to unlock level {}", this.storageSource.getLevelId(), ioexception);
            }
        }

        @Override
        public void waitUntilNextTick() {
            this.runAllTasks();
        }

        @Override
        public SystemReport fillServerSystemReport(SystemReport report) {
            report.setDetail("Type", "Test ephemeral server");
            return report;
        }

        @Override
        public boolean isHardcore() {
            return false;
        }

        @Override
        public int getOperatorUserPermissionLevel() {
            return 0;
        }

        @Override
        public int getFunctionCompilationLevel() {
            return 4;
        }

        @Override
        public boolean shouldRconBroadcast() {
            return false;
        }

        @Override
        public boolean isDedicatedServer() {
            return false;
        }

        @Override
        public int getRateLimitPacketsPerSecond() {
            return 0;
        }

        @Override
        public boolean isEpollEnabled() {
            return false;
        }

        @Override
        public boolean isCommandBlockEnabled() {
            return true;
        }

        @Override
        public boolean isPublished() {
            return false;
        }

        @Override
        public boolean shouldInformAdmins() {
            return false;
        }

        @Override
        public boolean isSingleplayerOwner(GameProfile profile) {
            return false;
        }
    }
}
