/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.configuration;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletedPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncStartPayload;
import net.neoforged.neoforge.registries.RegistryManager;

/**
 * Syncs registries to the client
 */
@ApiStatus.Internal
public record SyncRegistries() implements ConfigurationTask {
    private static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "sync_registries");
    public static final Type TYPE = new Type(ID);

    @Override
    public void start(Consumer<Packet<?>> sender) {
        sender.accept(new ClientboundCustomPayloadPacket(new FrozenRegistrySyncStartPayload(RegistryManager.getRegistryNamesForSyncToClient())));
        RegistryManager.generateRegistryPackets(false).forEach(payload -> sender.accept(new ClientboundCustomPayloadPacket(payload)));
        sender.accept(new ClientboundCustomPayloadPacket(new FrozenRegistrySyncCompletedPayload()));
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
