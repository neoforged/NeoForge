/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.custom.payload;

import io.netty.buffer.Unpooled;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.ICustomQueryPayloadWithBuffer;
import org.jetbrains.annotations.NotNull;

public final class SimpleQueryPayload implements ICustomQueryPayloadWithBuffer {
    private final FriendlyByteBuf payload;
    private final int packetIndex;
    private final ResourceLocation id;

    private SimpleQueryPayload(byte[] payload, int packetIndex, ResourceLocation id) {
        this.payload = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload));
        this.packetIndex = packetIndex;
        this.id = id;
    }

    private SimpleQueryPayload(FriendlyByteBuf byteBuf, int packetIndex, ResourceLocation id) {
        this.payload = byteBuf;
        this.packetIndex = packetIndex;
        this.id = id;
    }

    public static SimpleQueryPayload outbound(byte[] payload, int packetIndex, ResourceLocation id) {
        return new SimpleQueryPayload(payload, packetIndex, id);
    }

    public static SimpleQueryPayload outbound(FriendlyByteBuf byteBuf, int packetIndex, ResourceLocation id) {
        return new SimpleQueryPayload(byteBuf, packetIndex, id);
    }

    public static SimpleQueryPayload inbound(FriendlyByteBuf byteBuf) {
        final byte[] payload = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(payload);
        final FriendlyByteBuf innerBuf = new FriendlyByteBuf(Unpooled.copiedBuffer(payload));

        return new SimpleQueryPayload(innerBuf, innerBuf.readVarInt(), innerBuf.readResourceLocation());
    }

    public static SimpleQueryPayload inbound(FriendlyByteBuf byteBuf, ResourceLocation verifiableId) {
        final var payload = inbound(byteBuf);
        if (!payload.id().equals(verifiableId)) {
            throw new IllegalStateException("The received payload did not indicate the same channel id as the received packet: %s vs: %s".formatted(payload.id(), verifiableId));
        }

        return payload;
    }

    @Override
    public void write(FriendlyByteBuf p_295179_) {
        p_295179_.writeVarInt(packetIndex);
        p_295179_.writeResourceLocation(id);
        p_295179_.writeBytes(payload.slice());
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

    @Override
    public @NotNull ResourceLocation id() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SimpleQueryPayload) obj;
        return Objects.equals(this.payload, that.payload) &&
                Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payload, id);
    }

    @Override
    public String toString() {
        return "SimpleQueryPayload[" +
                "payload=" + payload + ", " +
                "id=" + id + ']';
    }

}
