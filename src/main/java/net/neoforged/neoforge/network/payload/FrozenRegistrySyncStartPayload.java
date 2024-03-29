/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * Packet payload sent to the client to start the frozen registry sync.
 * <p>
 * It indicates to the client which registries it should expect to receive.
 * </p>
 * 
 * @param toAccess The registries to access.
 */
@ApiStatus.Internal
public record FrozenRegistrySyncStartPayload(List<ResourceLocation> toAccess) implements CustomPacketPayload {
    public static final Type<FrozenRegistrySyncStartPayload> TYPE = new Type<>(new ResourceLocation(NeoForgeVersion.MOD_ID, "frozen_registry_sync_start"));
    public static final StreamCodec<FriendlyByteBuf, FrozenRegistrySyncStartPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()),
            FrozenRegistrySyncStartPayload::toAccess,
            FrozenRegistrySyncStartPayload::new);

    @Override
    public Type<FrozenRegistrySyncStartPayload> type() {
        return TYPE;
    }
}
