/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.configuration;

import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.common.TierSortingRegistry;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * Syncs the tier sorting registry to the client
 * 
 * @param listener the listener to indicate the check if it is a vanilla connection
 */
@ApiStatus.Internal
public record SyncTierSortingRegistry(ServerConfigurationPacketListener listener) implements ConfigurationTask {
    private static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "sync_tier_sorting");
    public static final Type TYPE = new Type(ID);

    @Override
    public void start(Consumer<Packet<?>> sender) {
        TierSortingRegistry.sync(listener(), payload -> sender.accept(new ClientboundCustomPayloadPacket(payload)));
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
