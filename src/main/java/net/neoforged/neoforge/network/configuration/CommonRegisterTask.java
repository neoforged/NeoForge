/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.configuration;

import java.util.function.Consumer;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.payload.CommonRegisterPayload;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.jetbrains.annotations.ApiStatus;

/**
 * Common Register configuration task. After completion of {@link CommonVersionTask}, sends a {@link CommonRegisterPayload} to the client
 * containing all known serverbound channels, and awaits a response containing the client's known clientbound channels.
 */
@ApiStatus.Internal
public record CommonRegisterTask() implements ICustomConfigurationTask {
    public static final Type TYPE = new Type(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "common_register"));

    @Override
    public Type type() {
        return TYPE;
    }

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        // There is currently no implementation for a version handshake, and the only existing version is 1, so we only send 1.
        // Version negotiation will have to be implemented properly if a version 2 is ever added.
        sender.accept(new CommonRegisterPayload(1, ConnectionProtocol.PLAY, NetworkRegistry.getCommonPlayChannels(PacketFlow.SERVERBOUND)));
    }
}
