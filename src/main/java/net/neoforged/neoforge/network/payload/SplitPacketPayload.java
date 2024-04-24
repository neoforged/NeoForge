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
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.ApiStatus;

/**
 * A payload that is used to split a packet into multiple payloads.
 * <p>
 * This single payload will contain a slice of the original packet.
 * </p>
 * 
 * @param payload The slice of the original packet.
 */
@ApiStatus.Internal
public record SplitPacketPayload(byte[] payload) implements CustomPacketPayload {
    public static final Type<SplitPacketPayload> TYPE = new Type<>(new ResourceLocation(NeoForgeVersion.MOD_ID, "split"));
    public static final StreamCodec<FriendlyByteBuf, SplitPacketPayload> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.UNBOUNDED_BYTE_ARRAY,
            SplitPacketPayload::payload,
            SplitPacketPayload::new);

    @Override
    public Type<SplitPacketPayload> type() {
        return TYPE;
    }
}
