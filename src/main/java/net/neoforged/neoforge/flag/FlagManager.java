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
import net.neoforged.neoforge.common.NeoForgeMod;
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

    public void markDirty() {
        var server = ServerLifecycleHooks.getCurrentServer();

        if (server == null)
            return;

        var level = server.overworld();
        level.setData(NeoForgeMod.LEVEL_FLAG_DATA, new FlagAttachment(enabledFlagsView));

        if (!level.isClientSide)
            syncToClient(null);
    }

    // must invoke markDirty if result == true
    public boolean setEnabledBatched(ResourceLocation flag, boolean enabled) {
        var wasEnabled = enabledFlags.getOrDefault(flag, false);
        enabledFlags.put(flag, enabled);
        var isEnabled = enabledFlags.getOrDefault(flag, false);
        return wasEnabled != isEnabled;
    }

    public boolean setEnabled(ResourceLocation flag, boolean enabled) {
        if (setEnabledBatched(flag, enabled)) {
            markDirty();
            return true;
        }

        return false;
    }

    public void setEnabled(Object2BooleanMap<ResourceLocation> flags, boolean reset) {
        var changed = false;

        if (reset) {
            enabledFlags.clear();
            changed = true;
        }

        for (var entry : flags.object2BooleanEntrySet()) {
            if (setEnabledBatched(entry.getKey(), entry.getBooleanValue()))
                changed = true;
        }

        if (changed)
            markDirty();
    }

    public void syncToClient(@Nullable ServerPlayer player) {
        if (player == null) {
            if (ServerLifecycleHooks.getCurrentServer() == null)
                return;

            PacketDistributor.sendToAllPlayers(new SyncFlagsPayload(enabledFlags));
            return;
        }

        PacketDistributor.sendToPlayer(player, new SyncFlagsPayload(enabledFlags));
    }
}
