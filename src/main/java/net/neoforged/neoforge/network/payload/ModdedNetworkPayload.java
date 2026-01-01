/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForgeMod;
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
    public static final Identifier ID = Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "network");
    public static final Type<ModdedNetworkPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ModdedNetworkPayload> STREAM_CODEC = StreamCodec.composite(
            NetworkPayloadSetup.STREAM_CODEC, ModdedNetworkPayload::setup,
            ModdedNetworkPayload::new);

    @Override
    public Type<ModdedNetworkPayload> type() {
        return TYPE;
    }
}
