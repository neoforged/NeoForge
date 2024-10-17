/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class FeatureFlagAcknowledgePayload implements CustomPacketPayload {
    public static final Type<FeatureFlagAcknowledgePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("neoforge", "feature_flags_ack"));
    public static final FeatureFlagAcknowledgePayload INSTANCE = new FeatureFlagAcknowledgePayload();
    public static final StreamCodec<ByteBuf, FeatureFlagAcknowledgePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private FeatureFlagAcknowledgePayload() {}

    @Override
    public Type<FeatureFlagAcknowledgePayload> type() {
        return TYPE;
    }
}
