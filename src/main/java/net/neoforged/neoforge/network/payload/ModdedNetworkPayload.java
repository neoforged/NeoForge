/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.HashMap;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.registration.NetworkChannel;
import net.neoforged.neoforge.network.registration.NetworkPayloadSetup;
import org.jetbrains.annotations.ApiStatus;

/**
 * A payload that contains the modded network configuration and play components.
 *
 * @param configuration The configuration components.
 * @param play          The play components.
 */
@ApiStatus.Internal
public record ModdedNetworkPayload(NetworkPayloadSetup setup) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "network");
    public static final FriendlyByteBuf.Reader<? extends CustomPacketPayload> READER = ModdedNetworkPayload::new;

    public ModdedNetworkPayload(FriendlyByteBuf byteBuf) {
        this(new NetworkPayloadSetup(byteBuf.readMap(b -> b.readEnum(ConnectionProtocol.class), buf -> buf.readMap(HashMap::new, FriendlyByteBuf::readResourceLocation, NetworkChannel::new))));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeMap(this.setup.channels(), FriendlyByteBuf::writeEnum, (b, map) -> b.writeMap(map, FriendlyByteBuf::writeResourceLocation, NetworkChannel::write));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
