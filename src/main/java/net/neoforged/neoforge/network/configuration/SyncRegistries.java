/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.configuration;

import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletedPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncStartPayload;
import net.neoforged.neoforge.registries.RegistryManager;
import org.jetbrains.annotations.ApiStatus;

/**
 * Syncs registries to the client
 */
@ApiStatus.Internal
public record SyncRegistries() implements ICustomConfigurationTask {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "sync_registries");
    public static final Type TYPE = new Type(ID);

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        sender.accept(new FrozenRegistrySyncStartPayload(RegistryManager.getRegistryNamesForSyncToClient()));
        RegistryManager.generateRegistryPackets(false).forEach(sender);
        sender.accept(FrozenRegistrySyncCompletedPayload.INSTANCE);
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
