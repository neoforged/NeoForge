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
import net.neoforged.neoforge.registries.RegistrySnapshot;
import org.jetbrains.annotations.ApiStatus;

/**
 * Packet payload for sending a frozen registry to the client
 *
 * @param registryName The name of the registry
 * @param snapshot     The snapshot of the registry
 */
@ApiStatus.Internal
public record FrozenRegistryPayload(Identifier registryName, RegistrySnapshot snapshot) implements CustomPacketPayload {
    public static final Type<FrozenRegistryPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "frozen_registry"));
    public static final StreamCodec<FriendlyByteBuf, FrozenRegistryPayload> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            FrozenRegistryPayload::registryName,
            RegistrySnapshot.STREAM_CODEC,
            FrozenRegistryPayload::snapshot,
            FrozenRegistryPayload::new);

    @Override
    public Type<FrozenRegistryPayload> type() {
        return TYPE;
    }
}
