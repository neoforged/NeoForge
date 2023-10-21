/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.fml.DistExecutor;
import net.neoforged.fml.Logging;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingStage;
import net.neoforged.fml.ModLoadingWarning;
import net.neoforged.neoforge.network.ConnectionType;
import net.neoforged.neoforge.network.NetworkConstants;
import net.neoforged.neoforge.network.NetworkHooks;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.common.MinecraftForge;
import net.neoforged.neoforge.gametest.ForgeGameTestHooks;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforge.registries.ForgeRegistries.Keys;
import net.neoforged.neoforge.resource.PathPackResources;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.MinecraftServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.GameData;
import org.jetbrains.annotations.ApiStatus;

public class ServerLifecycleHooks
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker SERVERHOOKS = MarkerManager.getMarker("SERVERHOOKS");
    private static final LevelResource SERVERCONFIG = new LevelResource("serverconfig");
    private static volatile CountDownLatch exitLatch = null;
    private static MinecraftServer currentServer;

    private static Path getServerConfigPath(final MinecraftServer server)
    {
        final Path serverConfig = server.getWorldPath(SERVERCONFIG);
        if (!Files.isDirectory(serverConfig)) {
            try {
                Files.createDirectories(serverConfig);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return serverConfig;
    }

    public static void handleServerAboutToStart(final MinecraftServer server)
    {
        currentServer = server;
        // on the dedi server we need to force the stuff to setup properly
        LogicalSidedProvider.setServer(()->server);
        ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.SERVER, getServerConfigPath(server));
        runModifiers(server);
        MinecraftForge.EVENT_BUS.post(new ServerAboutToStartEvent(server));
    }

    public static void handleServerStarting(final MinecraftServer server)
    {
        DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, ()->()->{
            LanguageHook.loadLanguagesOnServer(server);
            // GameTestServer requires the gametests to be registered earlier, so it is done in main and should not be done twice.
            if (!(server instanceof GameTestServer))
                ForgeGameTestHooks.registerGametests();
        });
        PermissionAPI.initializePermissionAPI();
        MinecraftForge.EVENT_BUS.post(new ServerStartingEvent(server));
    }

    public static void handleServerStarted(final MinecraftServer server)
    {
        MinecraftForge.EVENT_BUS.post(new ServerStartedEvent(server));
        allowLogins.set(true);
    }

    public static void handleServerStopping(final MinecraftServer server)
    {
        allowLogins.set(false);
        MinecraftForge.EVENT_BUS.post(new ServerStoppingEvent(server));
    }

    public static void expectServerStopped()
    {
        exitLatch = new CountDownLatch(1);
    }

    public static void handleServerStopped(final MinecraftServer server)
    {
        if (!server.isDedicatedServer()) GameData.revertToFrozen();
        MinecraftForge.EVENT_BUS.post(new ServerStoppedEvent(server));
        currentServer = null;
        LogicalSidedProvider.setServer(null);
        CountDownLatch latch = exitLatch;

        if (latch != null)
        {
            latch.countDown();
            exitLatch = null;
        }
        ConfigTracker.INSTANCE.unloadConfigs(ModConfig.Type.SERVER, getServerConfigPath(server));
    }

    public static MinecraftServer getCurrentServer()
    {
        return currentServer;
    }
    private static AtomicBoolean allowLogins = new AtomicBoolean(false);

    public static boolean handleServerLogin(final ClientIntentionPacket packet, final Connection manager) {
        if (!allowLogins.get())
        {
            MutableComponent text = Component.literal("Server is still starting! Please wait before reconnecting.");
            LOGGER.info(SERVERHOOKS,"Disconnecting Player (server is still starting): {}", text.getContents());
            manager.send(new ClientboundLoginDisconnectPacket(text));
            manager.disconnect(text);
            return false;
        }

        if (packet.intention() == ClientIntent.LOGIN) {
            final ConnectionType connectionType = ConnectionType.forVersionFlag(packet.getFMLVersion());
            final int versionNumber = connectionType.getFMLVersionNumber(packet.getFMLVersion());

            if (connectionType == ConnectionType.MODDED && versionNumber != NetworkConstants.FMLNETVERSION) {
                rejectConnection(manager, connectionType, "This modded server is not impl compatible with your modded client. Please verify your NeoForge version closely matches the server. Got net version " + versionNumber + " this server is net version " + NetworkConstants.FMLNETVERSION);
                return false;
            }

            if (connectionType == ConnectionType.VANILLA && !NetworkRegistry.acceptsVanillaClientConnections()) {
                rejectConnection(manager, connectionType, "This server has mods that require NeoForge to be installed on the client. Contact your server admin for more details.");
                return false;
            }
        }

        if (packet.intention() == ClientIntent.STATUS) return true;

        NetworkHooks.registerServerLoginChannel(manager, packet);
        return true;

    }

    private static void rejectConnection(final Connection manager, ConnectionType type, String message) {
        manager.setClientboundProtocolAfterHandshake(ClientIntent.LOGIN);
        String ip = "local";
        if (manager.getRemoteAddress() != null)
           ip = manager.getRemoteAddress().toString();
        LOGGER.info(SERVERHOOKS, "[{}] Disconnecting {} connection attempt: {}", ip, type, message);
        MutableComponent text = Component.literal(message);
        manager.send(new ClientboundLoginDisconnectPacket(text));
        manager.disconnect(text);
    }

    public static void handleExit(int retVal)
    {
        System.exit(retVal);
    }

    @ApiStatus.Internal
    public static RepositorySource buildPackFinder(Map<IModFile, ? extends PathPackResources> modResourcePacks) {
        return packAcceptor -> serverPackFinder(modResourcePacks, packAcceptor);
    }

    private static void serverPackFinder(Map<IModFile, ? extends PathPackResources> modResourcePacks, Consumer<Pack> packAcceptor) {
        for (Entry<IModFile, ? extends PathPackResources> e : modResourcePacks.entrySet())
        {
            IModInfo mod = e.getKey().getModInfos().get(0);
            if (Objects.equals(mod.getModId(), "minecraft")) continue; // skip the minecraft "mod"
            final String name = "mod:" + mod.getModId();
            final Pack modPack = Pack.readMetaAndCreate(name, Component.literal(e.getValue().packId()), false, BuiltInPackSource.fixedResources(e.getValue()), PackType.SERVER_DATA, Pack.Position.BOTTOM, PackSource.DEFAULT);
            if (modPack == null) {
                // Vanilla only logs an error, instead of propagating, so handle null and warn that something went wrong
                ModLoader.get().addWarning(new ModLoadingWarning(mod, ModLoadingStage.ERROR, "fml.modloading.brokenresources", e.getKey()));
                continue;
            }
            LOGGER.debug(Logging.CORE, "Generating PackInfo named {} for mod file {}", name, e.getKey().getFilePath());
            packAcceptor.accept(modPack);
        }
    }

    private static void runModifiers(final MinecraftServer server)
    {
        final RegistryAccess registries = server.registryAccess();

        // The order of holders() is the order modifiers were loaded in.
        final List<BiomeModifier> biomeModifiers = registries.registryOrThrow(ForgeRegistries.Keys.BIOME_MODIFIERS)
            .holders()
            .map(Holder::value)
            .toList();
        final List<StructureModifier> structureModifiers = registries.registryOrThrow(Keys.STRUCTURE_MODIFIERS)
              .holders()
              .map(Holder::value)
              .toList();

        // Apply sorted biome modifiers to each biome.
        registries.registryOrThrow(Registries.BIOME).holders().forEach(biomeHolder ->
        {
            biomeHolder.value().modifiableBiomeInfo().applyBiomeModifiers(biomeHolder, biomeModifiers);
        });
        // Apply sorted structure modifiers to each structure.
        registries.registryOrThrow(Registries.STRUCTURE).holders().forEach(structureHolder ->
        {
            structureHolder.value().modifiableStructureInfo().applyStructureModifiers(structureHolder, structureModifiers);
        });
    }
}
