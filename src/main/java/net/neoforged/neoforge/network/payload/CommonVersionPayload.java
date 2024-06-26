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

/**
 * Unsupported common version payload. Must be sent to fabric to support their implementation of c:register.
 * Only legal version is 1.
 */
public record CommonVersionPayload(List<Integer> versions) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("c", "version");
    public static final CustomPacketPayload.Type<CommonVersionPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, CommonVersionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), CommonVersionPayload::versions,
            CommonVersionPayload::new);

    public CommonVersionPayload() {
        this(List.of(1));
    }

    @Override
    public Type<CommonVersionPayload> type() {
        return TYPE;
    }
}
