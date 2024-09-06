/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
import java.util.Set;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.WorldDataConfiguration;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ServerFlagManager implements FlagManager {
    private final ReferenceSet<Flag> enabledFlags = new ReferenceOpenHashSet<>();
    private final Set<Flag> enabledFlagsView = ReferenceSets.unmodifiable(enabledFlags);

    private final MinecraftServer server;

    public ServerFlagManager(MinecraftServer server, Set<Flag> initialEnabledFlags) {
        this.server = server;
        enabledFlags.addAll(initialEnabledFlags);
    }

    private void flagChanged(Flag flag, boolean state) {
        // notify clients of state change
        PacketDistributor.sendToAllPlayers(new ClientboundSyncFlags(enabledFlags));

        var data = server.getWorldData();
        var config = data.getDataConfiguration();

        // update backing config file
        // will be saved to disk later during autosave/server shutdown
        data.setDataConfiguration(new WorldDataConfiguration(config.dataPacks(), config.enabledFeatures(), enabledFlagsView));
    }

    @Override
    public boolean set(Flag flag, boolean state) {
        var changed = state ? enabledFlags.add(flag) : enabledFlags.remove(flag);

        if (changed)
            flagChanged(flag, state);

        return changed;
    }

    @Override
    public Set<Flag> getEnabledFlags() {
        return enabledFlagsView;
    }
}
