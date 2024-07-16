/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Main brains for the whole flag system.
 * <p>
 * Not to be used directly, modders should use {@linkplain Flags}.
 */
@ApiStatus.Internal
public final class FlagManager {
    public static final FlagManager INSTANCE = new FlagManager();

    final Object2BooleanMap<ResourceLocation> enabledFlags = new Object2BooleanOpenHashMap<>();
    final Object2BooleanMap<ResourceLocation> enabledFlagsView = Object2BooleanMaps.unmodifiable(enabledFlags);

    private FlagManager() {}

    void markDirty(boolean savedData, boolean sync) {
        if (savedData)
            FlagSavedData.accept(SavedData::setDirty);
        if (sync)
            syncToClient(null);
    }

    boolean setEnabled(ResourceLocation flag, boolean enabled) {
        var wasEnabled = enabledFlags.getOrDefault(flag, false);
        enabledFlags.put(flag, enabled);
        var isEnabled = enabledFlags.getOrDefault(flag, false);
        return wasEnabled != isEnabled;
    }

    boolean setEnabled(Object2BooleanMap<ResourceLocation> flags, boolean reset) {
        var changed = false;

        if (reset) {
            enabledFlags.clear();
            changed = true;
        }

        for (var entry : flags.object2BooleanEntrySet()) {
            if (setEnabled(entry.getKey(), entry.getBooleanValue()))
                changed = true;
        }

        return changed;
    }

    private void syncToClient(@Nullable ServerPlayer player) {
        if (player == null) {
            if (ServerLifecycleHooks.getCurrentServer() == null)
                return;

            PacketDistributor.sendToAllPlayers(new SyncFlagsPayload(enabledFlags));
            return;
        }

        PacketDistributor.sendToPlayer(player, new SyncFlagsPayload(enabledFlags));
    }

    public static void setup() {
        NeoForge.EVENT_BUS.addListener(AddReloadListenerEvent.class, event -> event.addListener(new FlagLoader()));

        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player)
                INSTANCE.syncToClient(player);
        });

        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, event -> {
            // exists purely to load the saved data
            FlagSavedData.get(event.getServer());
        });
    }
}
