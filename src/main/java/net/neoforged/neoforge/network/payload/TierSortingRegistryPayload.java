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
 * The payload for the tier sorting registry packet.
 * <p>
 * This payload is used to send the tier order to the client.
 * </p>
 * 
 * @param tiers The tiers in order.
 */
@ApiStatus.Internal
public record TierSortingRegistryPayload(List<ResourceLocation> tiers) implements CustomPacketPayload {
    public static final Type<TierSortingRegistryPayload> TYPE = new Type<>(new ResourceLocation(NeoForgeVersion.MOD_ID, "tier_sorting"));
    public static final StreamCodec<FriendlyByteBuf, TierSortingRegistryPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TierSortingRegistryPayload::tiers,
            TierSortingRegistryPayload::new);

    @Override
    public Type<TierSortingRegistryPayload> type() {
        return TYPE;
    }
}
