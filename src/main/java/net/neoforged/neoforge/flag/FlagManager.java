/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class FlagManager {
    public static Set<ResourceLocation> getKnownFlags() {
        return INSTANCE.flagsView;
    }

    public static Set<ResourceLocation> getEnabledFlags() {
        return INSTANCE.enabledFlagsView;
    }

    public static boolean isKnown(ResourceLocation flag) {
        return getKnownFlags().contains(flag);
    }

    public static boolean isEnabled(ResourceLocation flag) {
        return isKnown(flag) && getEnabledFlags().contains(flag);
    }

    public static boolean isEnabled(ResourceLocation... flags) {
        for (var flag : flags) {
            if (!isEnabled(flag))
                return false;
        }

        return true;
    }

    public static boolean isEnabled(Iterable<ResourceLocation> flags) {
        for (var flag : flags) {
            if (!isEnabled(flag))
                return false;
        }

        return true;
    }

    // region: Internal
    @ApiStatus.Internal
    public static final FlagManager INSTANCE = new FlagManager();
    public static final Logger LOGGER = LogManager.getLogger();

    private final Set<ResourceLocation> flags = Sets.newHashSet();
    private final Set<ResourceLocation> enabledFlags = Sets.newHashSet();
    private final Set<ResourceLocation> flagsView = Collections.unmodifiableSet(flags);
    private final Set<ResourceLocation> enabledFlagsView = Collections.unmodifiableSet(enabledFlags);

    private FlagManager() {}

    public void setup() {
        NeoForge.EVENT_BUS.addListener(AddReloadListenerEvent.class, event -> event.addListener(new FlagLoader()));

        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player)
                syncToClient(player);
        });
    }

    public void setEnabled(ResourceLocation flag, boolean enabled) {
        if (!flags.contains(flag))
            return;

        var sync = false;

        if (enabled) {
            if (enabledFlags.add(flag)) {
                LOGGER.debug("Enabled flag: {}", flag);
                sync = true;
            }
        } else {
            if (enabledFlags.remove(flag)) {
                sync = true;
                LOGGER.debug("Disabled flag: {}", flag);
            }
        }

        if (sync)
            syncToClient(null);
    }

    private void syncToClient(@Nullable ServerPlayer player) {
        if (player == null) {
            var server = ServerLifecycleHooks.getCurrentServer();

            if (server == null)
                return;

            PacketDistributor.sendToAllPlayers(new FlagPayloads.Known(flags), new FlagPayloads.Enabled(enabledFlags));
            return;
        }

        PacketDistributor.sendToPlayer(player, new FlagPayloads.Known(flags), new FlagPayloads.Enabled(enabledFlags));
    }

    void loadFromJson(Iterable<FlagLoader.FlagData> flagData) {
        var oldEnabledFlags = Set.copyOf(enabledFlags);

        flags.clear();
        enabledFlags.clear();

        for (var flag : flagData) {
            var name = flag.name();

            flags.add(name);

            if (flag.enabledByDefault() || oldEnabledFlags.contains(name))
                enabledFlags.add(name);
        }

        LOGGER.debug("Loaded {} flags from json (Enabled {} flags by default)", flags.size(), enabledFlags.size());

        if (FMLEnvironment.dist.isDedicatedServer())
            syncToClient(null);
    }

    void loadKnownFromRemote(Collection<ResourceLocation> remote) {
        flags.clear();
        flags.addAll(remote);

        LOGGER.debug("Synced {} known flags from remote", flags.size());
    }

    void loadEnabledFromRemote(Collection<ResourceLocation> remote) {
        enabledFlags.clear();
        enabledFlags.addAll(remote);

        LOGGER.debug("Synced {} enabled flags from remote", enabledFlags.size());
    }
    // endregion
}
