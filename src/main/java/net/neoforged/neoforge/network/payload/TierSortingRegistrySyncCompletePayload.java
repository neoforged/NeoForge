/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * This payload is sent by the server to the client when the tier sorting registry has been fully synced.
 */
@ApiStatus.Internal
public record TierSortingRegistrySyncCompletePayload() implements CustomPacketPayload {
    public static final Type<TierSortingRegistrySyncCompletePayload> TYPE = new Type<>(new ResourceLocation(NeoForgeVersion.MOD_ID, "tier_sorting_registry_sync_complete"));
    public static final TierSortingRegistrySyncCompletePayload INSTANCE = new TierSortingRegistrySyncCompletePayload();
    public static final StreamCodec<FriendlyByteBuf, TierSortingRegistrySyncCompletePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<TierSortingRegistrySyncCompletePayload> type() {
        return TYPE;
    }
}
