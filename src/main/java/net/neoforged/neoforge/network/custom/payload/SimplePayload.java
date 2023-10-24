/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.custom.payload;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.ICustomPacketPayloadWithBuffer;

public record SimplePayload(FriendlyByteBuf payload, ResourceLocation id, int packetIndex) implements ICustomPacketPayloadWithBuffer {
    public SimplePayload(byte[] payload, ResourceLocation id, int packetIndex) {
        this(new FriendlyByteBuf(Unpooled.wrappedBuffer(payload)), id, packetIndex);
    }

    public static SimplePayload outbound(byte[] payload, int packetIndex, ResourceLocation id) {
        return new SimplePayload(payload, id, packetIndex);
    }

    public static SimplePayload outbound(FriendlyByteBuf byteBuf, int packetIndex, ResourceLocation id) {
        return new SimplePayload(byteBuf, id, packetIndex);
    }

    public static SimplePayload inbound(FriendlyByteBuf byteBuf, ResourceLocation id) {
        final byte[] payload = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(payload);
        final FriendlyByteBuf innerBuf = new FriendlyByteBuf(Unpooled.copiedBuffer(payload));
        
        return new SimplePayload(innerBuf, id, innerBuf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(packetIndex);
        buf.writeBytes(payload.slice());
    }

    @Override
    public FriendlyByteBuf buffer() {
        return new FriendlyByteBuf(Unpooled.copiedBuffer(payload));
    }

    @Override
    public int packetIndex() {
        return packetIndex;
    }

    public FriendlyByteBuf payload() {
        return payload;
    }

}
