/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.classloading.transformation.ClassTransformStatistics;
import net.neoforged.neoforge.common.crafting.RecipePriorityManager;
import net.neoforged.neoforge.common.loot.LootModifierManager;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.internal.NeoForgeProxy;
import net.neoforged.neoforge.network.ConfigSync;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.payload.RegistryDataMapSyncPayload;
import net.neoforged.neoforge.registries.DataMapLoader;
import net.neoforged.neoforge.registries.DataPackRegistriesHooks;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.resource.NeoForgeReloadListeners;
import net.neoforged.neoforge.server.command.ConfigCommand;
import net.neoforged.neoforge.server.command.NeoForgeCommand;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NeoForgeEventHandler {
    private static LootModifierManager LOOT_MODIFIER_MANAGER;
    private static DataMapLoader DATA_MAP_LOADER;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity.getClass().equals(ItemEntity.class)) {
            ItemStack stack = ((ItemEntity) entity).getItem();
            Item item = stack.getItem();
            if (item.hasCustomEntity(stack)) {
                Entity newEntity = item.createEntity(event.getLevel(), entity, stack);
                if (newEntity != null) {
                    entity.discard();
                    event.setCanceled(true);
                    BlockableEventLoop<? super TickTask> executor = event.getLevel() instanceof ServerLevel serverLevel ? serverLevel.getServer() : NeoForgeProxy.INSTANCE.getClientExecutor();
                    executor.schedule(new TickTask(0, () -> event.getLevel().addFreshEntity(newEntity)));
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDimensionUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel)
            FakePlayerFactory.unloadLevel((ServerLevel) event.getLevel());
    }

    @SubscribeEvent
    public void postServerTick(ServerTickEvent.Post event) {
        ConfigSync.syncPendingConfigs(event.getServer());
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getLevel().isClientSide())
            FarmlandWaterManager.removeTickets(event.getChunk());
    }

    /*
    @SubscribeEvent
    public void playerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (event.getPlayer() instanceof ServerPlayerEntity)
            DimensionManager.rebuildPlayerMap(((ServerPlayerEntity)event.getPlayer()).server.getPlayerList(), true);
    }
    */

    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        UsernameCache.setUsername(event.getEntity().getUUID(), event.getEntity().getGameProfile().name());
    }

    @SubscribeEvent
    public void tagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            DATA_MAP_LOADER.apply();
        }
    }

    @SubscribeEvent
    public void onDpSync(final OnDatapackSyncEvent event) {
        RegistryManager.getDataMaps().forEach((registry, values) -> {
            final var regOpt = event.getPlayerList().getServer().overworld().registryAccess()
                    .lookup(registry);
            if (regOpt.isEmpty()) return;
            event.getRelevantPlayers().forEach(player -> {
                if (!player.connection.hasChannel(RegistryDataMapSyncPayload.TYPE)) {
                    return;
                }

                // Note: don't send data maps over in-memory connections for normal registries, else the client-side handling will wipe non-synced data maps.
                // Sending them for synced datapack registries is fine and required as those registries are recreated on the client
                if (player.connection.getConnection().isMemoryConnection() && DataPackRegistriesHooks.getSyncedRegistry((ResourceKey) registry) == null) {
                    return;
                }
                final var playerMaps = player.connection.getConnection().channel().attr(RegistryManager.ATTRIBUTE_KNOWN_DATA_MAPS).get();
                if (playerMaps == null) return; // Skip gametest players for instance
                handleSync(player, regOpt.get(), playerMaps.getOrDefault(registry, List.of()));
            });
        });
    }

    private <T> void handleSync(ServerPlayer player, Registry<T> registry, Collection<ResourceLocation> attachments) {
        if (attachments.isEmpty()) return;
        final Map<ResourceLocation, Map<ResourceKey<T>, ?>> att = new HashMap<>();
        attachments.forEach(key -> {
            final var attach = RegistryManager.getDataMap(registry.key(), key);
            if (attach == null || attach.networkCodec() == null) return;
            att.put(key, registry.getDataMap(attach));
        });
        if (!att.isEmpty()) {
            PacketDistributor.sendToPlayer(player, new RegistryDataMapSyncPayload<>(registry.key(), att));
        }
    }

    @SubscribeEvent
    public void onCommandsRegister(RegisterCommandsEvent event) {
        NeoForgeCommand.register(event.getDispatcher());
        ConfigCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onResourceReload(AddServerReloadListenersEvent event) {
        event.addListener(NeoForgeReloadListeners.LOOT_MODIFIERS, LOOT_MODIFIER_MANAGER = new LootModifierManager());
        event.addListener(NeoForgeReloadListeners.RECIPE_PRIORITIES, new RecipePriorityManager(event.getServerResources().getRecipeManager()));
        event.addListener(NeoForgeReloadListeners.DATA_MAPS, DATA_MAP_LOADER = new DataMapLoader(event.getConditionContext(), event.getRegistryAccess()));
        event.addListener(NeoForgeReloadListeners.CREATIVE_TABS, CreativeModeTabRegistry.getReloadListener());
    }

    static LootModifierManager getLootModifierManager() {
        if (LOOT_MODIFIER_MANAGER == null)
            throw new IllegalStateException("Can not retrieve LootModifierManager until resources have loaded once.");
        return LOOT_MODIFIER_MANAGER;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void builtinMobSpawnBlocker(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Mob mob && mob.isSpawnCancelled()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void logTransformationsOnGameShutdown(GameShuttingDownEvent event) {
        ClassTransformStatistics.logTransformationSummary();
        // Also check if anyone appears to be performing mass-ASM and log a warning if so
        ClassTransformStatistics.checkTransformationBehavior();
    }
}
