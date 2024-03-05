/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.configuration;

import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagNetworkSerialization;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletedPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncStartPayload;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.ApiStatus;

/**
 * Syncs registries to the client
 */
@ApiStatus.Internal
public record SyncRegistries() implements ICustomConfigurationTask {
    private static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "sync_registries");
    public static final Type TYPE = new Type(ID);

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        sender.accept(new FrozenRegistrySyncStartPayload(RegistryManager.getRegistryNamesForSyncToClient()));
        RegistryManager.generateRegistryPackets(false).forEach(sender);
        sender.accept(new FrozenRegistrySyncCompletedPayload());
    }

    @Override
    public void start(Consumer<Packet<?>> sender) {
        ICustomConfigurationTask.super.start(sender);
        // Sync tags a second time. This is needed in 20.4 because tags use integer IDs for syncing, and we do the
        // registry sync after vanilla sends the tags packet.
        // TODO remove in 20.5, where this is fixed properly
        sender.accept(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(ServerLifecycleHooks.getCurrentServer().registries())));
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
