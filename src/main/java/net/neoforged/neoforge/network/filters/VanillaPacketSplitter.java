/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.filters;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.CompressionDecoder;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.SplitPacketPayload;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A custom payload channel that allows sending vanilla server-to-client packets, even if they would normally
 * be too large for the vanilla protocol. This is achieved by splitting them into multiple custom payload packets.
 */
@Mod.EventBusSubscriber(modid = NeoForgeVersion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VanillaPacketSplitter {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int PROTOCOL_MAX = CompressionDecoder.MAXIMUM_UNCOMPRESSED_LENGTH;

    private static final int PAYLOAD_TO_CLIENT_MAX = 1048576;
    // 1 byte for state, 5 byte for VarInt PacketID
    private static final int PART_SIZE = PAYLOAD_TO_CLIENT_MAX - 1 - 5;

    private static final byte STATE_FIRST = 1;
    private static final byte STATE_LAST = 2;

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlerEvent event) {
        event.registrar(NeoForgeVersion.MOD_ID)
                .versioned(NeoForgeVersion.getSpec())
                .optional()
                .common(
                        SplitPacketPayload.ID,
                        SplitPacketPayload::new,
                        VanillaPacketSplitter::receivedPacket);
    }

    /**
     * Append the given packet to the given list. If the packet needs to be split, multiple packets will be appened.
     * Otherwise only the packet itself.
     */
    public static void appendPackets(ConnectionProtocol protocol, PacketFlow direction, Packet<?> packet, List<? super Packet<?>> out) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        if (buf.readableBytes() <= PROTOCOL_MAX) {
            buf.release();
            out.add(packet);
        } else {
            int parts = (int) Math.ceil(((double) buf.readableBytes()) / PART_SIZE);
            if (parts == 1) {
                buf.release();
                out.add(packet);
            } else {
                for (int part = 0; part < parts; part++) {
                    ByteBuf partPrefix;
                    if (part == 0) {
                        partPrefix = Unpooled.buffer(5);
                        partPrefix.writeByte(STATE_FIRST);
                        new FriendlyByteBuf(partPrefix).writeVarInt(protocol.codec(direction).packetId(packet));
                    } else {
                        partPrefix = Unpooled.buffer(1);
                        partPrefix.writeByte(part == parts - 1 ? STATE_LAST : 0);
                    }

                    int partSize = Math.min(PART_SIZE, buf.readableBytes());
                    final byte[] prefix = partPrefix.array();
                    final byte[] payloadSlice = buf.slice(buf.readerIndex(), partSize).array();

                    byte[] payload = new byte[prefix.length + payloadSlice.length];
                    System.arraycopy(prefix, 0, payload, 0, prefix.length);
                    System.arraycopy(payloadSlice, 0, payload, prefix.length, payloadSlice.length);

                    out.add(new ClientboundCustomPayloadPacket(new SplitPacketPayload(payload)));

                    partPrefix.release();
                }
                // We cloned all the data into arrays, no need to retain the buffer anymore
                buf.release();
            }
        }
    }

    private static final List<FriendlyByteBuf> receivedBuffers = new ArrayList<>();

    private static void receivedPacket(SplitPacketPayload payload, IPayloadContext context) {
        final ConnectionProtocol protocol = context.protocol();
        final PacketFlow flow = context.flow();
        final ChannelHandlerContext channelHandlerContext = context.channelHandlerContext();

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.payload()));

        byte state = buf.readByte();
        if (state == STATE_FIRST) {
            if (!receivedBuffers.isEmpty()) {
                LOGGER.warn("neoforge:split received out of order - inbound buffer not empty when receiving first");
                receivedBuffers.clear();
            }
        }
        buf.retain(); // retain the buffer, it is released after this handler otherwise
        receivedBuffers.add(buf);
        if (state == STATE_LAST) {
            FriendlyByteBuf full = new FriendlyByteBuf(Unpooled.wrappedBuffer(receivedBuffers.toArray(new FriendlyByteBuf[0])));
            int packetId = full.readVarInt();
            Packet<?> packet = protocol.codec(flow).createPacket(packetId, full, channelHandlerContext);
            if (packet == null) {
                LOGGER.error("Received invalid packet ID {} in neoforge:split", packetId);
            } else {
                receivedBuffers.clear();
                full.release();

                context.workHandler()
                        .submitAsync(() -> context.packetHandler().handle(packet))
                        .exceptionally(throwable -> {
                            LOGGER.error("Error handling packet", throwable);
                            return null;
                        });
            }
        }
    }

    public enum RemoteCompatibility {
        ABSENT,
        PRESENT
    }

    public static RemoteCompatibility getRemoteCompatibility(Connection manager) {
        return NetworkRegistry.getInstance().isVanillaConnection(manager) ? RemoteCompatibility.ABSENT : RemoteCompatibility.PRESENT;
    }

    public static boolean isRemoteCompatible(Connection manager) {
        return getRemoteCompatibility(manager) != RemoteCompatibility.ABSENT;
    }
}
