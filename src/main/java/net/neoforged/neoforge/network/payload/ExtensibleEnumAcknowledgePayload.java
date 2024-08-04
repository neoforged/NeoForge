/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public final class ExtensibleEnumAcknowledgePayload implements CustomPacketPayload {
    public static final Type<ExtensibleEnumAcknowledgePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("neoforge", "extensible_enum_ack"));
    public static final ExtensibleEnumAcknowledgePayload INSTANCE = new ExtensibleEnumAcknowledgePayload();
    public static final StreamCodec<ByteBuf, ExtensibleEnumAcknowledgePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private ExtensibleEnumAcknowledgePayload() {}

    @Override
    public Type<ExtensibleEnumAcknowledgePayload> type() {
        return TYPE;
    }
}
