/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.filters;

import static net.minecraft.network.Connection.ATTRIBUTE_CLIENTBOUND_PROTOCOL;
import static net.minecraft.network.Connection.ATTRIBUTE_SERVERBOUND_PROTOCOL;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.CompressionDecoder;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.connection.ConnectionPhase;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.SplitPacketPayload;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

/**
 * A generic packet splitter that can be used to split packets that are too large to be sent in one go.
 */
@Mod.EventBusSubscriber(modid = NeoForgeVersion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ApiStatus.Internal
public class GenericPacketSplitter extends MessageToMessageEncoder<Packet<?>> implements DynamicChannelHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int MAX_PACKET_SIZE = CompressionDecoder.MAXIMUM_UNCOMPRESSED_LENGTH;
    private static final int MAX_PART_SIZE = determineMaxPayloadSize(
            ConnectionProtocol.CONFIGURATION,
            PacketFlow.SERVERBOUND);

    private static final byte STATE_FIRST = 1;
    private static final byte STATE_LAST = 2;

    private final AttributeKey<ConnectionProtocol.CodecData<?>> codecKey;
    private final ConnectionType connectionType;
    private static final AttributeKey<GenericPacketSplitter> SPLITTER_ATTRIBUTE = AttributeKey.valueOf("neoforge:splitter");

    public GenericPacketSplitter(Connection connection, ConnectionType connectionType) {
        this(getProtocolKey(connection.getDirection().getOpposite()), connectionType);

        connection.channel().attr(SPLITTER_ATTRIBUTE).set(this);
    }

    public GenericPacketSplitter(AttributeKey<ConnectionProtocol.CodecData<?>> codecKey, ConnectionType connectionType) {
        this.codecKey = codecKey;
        this.connectionType = connectionType;
    }

    @SubscribeEvent
    private static void register(final RegisterPayloadHandlerEvent event) {
        event.registrar(NeoForgeVersion.MOD_ID)
                .versioned(NeoForgeVersion.getSpec())
                .optional()
                .common(
                        SplitPacketPayload.ID,
                        SplitPacketPayload::new,
                        (payload, context) -> {
                            final GenericPacketSplitter splitter = context.channelHandlerContext().channel().attr(SPLITTER_ATTRIBUTE).get();
                            if (splitter != null) {
                                splitter.receivedPacket(payload, context);
                            } else {
                                LOGGER.error("Received split packet without a splitter");
                                context.disconnect(Component.translatable("neoforge.network.packet_splitter.unknown"));
                            }
                        });
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet<?> packet, List<Object> out) throws Exception {
        if (packet instanceof ClientboundCustomPayloadPacket clientboundCustomPayloadPacket && clientboundCustomPayloadPacket.payload() instanceof SplitPacketPayload) {
            // Don't split our own split packets
            out.add(packet);
            return;
        }

        if (packet instanceof ServerboundCustomPayloadPacket serverboundCustomPayloadPacket && serverboundCustomPayloadPacket.payload() instanceof SplitPacketPayload) {
            // Don't split our own split packets
            out.add(packet);
            return;
        }

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        if (buf.readableBytes() <= MAX_PACKET_SIZE) {
            buf.release();
            out.add(packet);
        } else {
            int parts = (int) Math.ceil(((double) buf.readableBytes()) / MAX_PART_SIZE);
            if (parts == 1) {
                buf.release();
                out.add(packet);
            } else {

                Attribute<ConnectionProtocol.CodecData<?>> attribute = ctx.channel().attr(this.codecKey);
                ConnectionProtocol.CodecData<?> codecdata = attribute.get();

                final byte[] packetData = buf.array();
                for (int part = 0; part < parts; part++) {
                    final ByteBuf partPrefix;
                    if (part == 0) {
                        partPrefix = Unpooled.buffer(5);
                        partPrefix.writeByte(STATE_FIRST);

                        VarInt.write(partPrefix, codecdata.packetId(packet));
                    } else {
                        partPrefix = Unpooled.buffer(1);
                        partPrefix.writeByte(part == parts - 1 ? STATE_LAST : 0);
                    }

                    final int partSize = Math.min(MAX_PART_SIZE, packetData.length - (part * MAX_PART_SIZE));
                    final int prefixSize = partPrefix.readableBytes();
                    final byte[] payloadSlice = new byte[partSize + prefixSize];

                    partPrefix.readBytes(payloadSlice, 0, prefixSize);
                    System.arraycopy(packetData, part * MAX_PART_SIZE, payloadSlice, prefixSize, partSize);

                    out.add(createPacket(codecdata.flow(), payloadSlice));

                    partPrefix.release();
                }
                // We cloned all the data into arrays, no need to retain the buffer anymore
                buf.release();
            }
        }
    }

    private final List<byte[]> receivedBuffers = new ArrayList<>();

    private void receivedPacket(SplitPacketPayload payload, IPayloadContext context) {
        final ConnectionProtocol protocol = context.protocol();
        final PacketFlow flow = context.flow();
        final ChannelHandlerContext channelHandlerContext = context.channelHandlerContext();

        byte state = payload.payload()[0];
        if (state == STATE_FIRST) {
            if (!receivedBuffers.isEmpty()) {
                LOGGER.warn("neoforge:split received out of order - inbound buffer not empty when receiving first");
                receivedBuffers.clear();
            }
        }

        int contentSize = payload.payload().length - 1;
        byte[] buffer = new byte[contentSize];
        System.arraycopy(payload.payload(), 1, buffer, 0, contentSize); // We cut of the initial byte here that indicates the state
        receivedBuffers.add(buffer);

        if (state == STATE_LAST) {
            final byte[][] buffers = receivedBuffers.toArray(new byte[0][]);
            FriendlyByteBuf full = new FriendlyByteBuf(Unpooled.wrappedBuffer(buffers));
            int packetId = full.readVarInt();

            Packet<?> packet = protocol.codec(flow).createPacket(packetId, full, channelHandlerContext);
            if (packet == null) {
                LOGGER.error("Received invalid packet ID {} in neoforge:split", packetId);
            } else {
                receivedBuffers.clear();
                full.release();
                context.enqueueWork(() -> context.handle(packet));
            }
        }
    }

    private static Packet<?> createPacket(PacketFlow flow, byte[] payload) {
        return switch (flow) {
            case SERVERBOUND -> new ServerboundCustomPayloadPacket(new SplitPacketPayload(payload));
            case CLIENTBOUND -> new ClientboundCustomPayloadPacket(new SplitPacketPayload(payload));
        };
    }

    @Override
    public boolean isNecessary(Connection manager) {
        return !manager.isMemoryConnection() && isRemoteCompatible(manager);
    }

    public enum RemoteCompatibility {
        ABSENT,
        PRESENT
    }

    public static RemoteCompatibility getRemoteCompatibility(Connection manager) {
        return NetworkRegistry.isConnected(manager, ConnectionPhase.ANY, SplitPacketPayload.ID) ? RemoteCompatibility.PRESENT : RemoteCompatibility.ABSENT;
    }

    public static boolean isRemoteCompatible(Connection manager) {
        return getRemoteCompatibility(manager) != RemoteCompatibility.ABSENT;
    }

    public static int determineMaxPayloadSize(ConnectionProtocol protocol, PacketFlow flow) {
        final FriendlyByteBuf temporaryBuf = new FriendlyByteBuf(Unpooled.buffer());
        int packetId = switch (flow) {
            case SERVERBOUND -> protocol.codec(flow).packetId(new ServerboundCustomPayloadPacket(new SplitPacketPayload(new byte[0])));
            case CLIENTBOUND -> protocol.codec(flow).packetId(new ClientboundCustomPayloadPacket(new SplitPacketPayload(new byte[0])));
        };

        //Simulate writing our split packet with a full byte array
        //First write the packet id, as does the vanilla packet encoder.
        temporaryBuf.writeVarInt(packetId);

        //Then write the payload id, as does the custom payload packet, regardless of flow.
        temporaryBuf.writeResourceLocation(SplitPacketPayload.ID);

        //Then write the byte prefix to indicate the state of the packet.
        temporaryBuf.writeByte(STATE_FIRST);

        //Then write the potential packet id of the split packet
        temporaryBuf.writeVarInt(Integer.MAX_VALUE);

        //Now write a max int value for the maximum length of the byte[]
        temporaryBuf.writeInt(Integer.MAX_VALUE);

        //During normal write operations, this is the prefix content that is written before the actual payload.
        //This is the same for both clientbound and serverbound packets.
        final int prefixSize = temporaryBuf.readableBytes();
        return CompressionDecoder.MAXIMUM_UNCOMPRESSED_LENGTH - prefixSize;
    }

    private static AttributeKey<ConnectionProtocol.CodecData<?>> getProtocolKey(PacketFlow flow) {
        return switch (flow) {
            case CLIENTBOUND -> ATTRIBUTE_CLIENTBOUND_PROTOCOL;
            case SERVERBOUND -> ATTRIBUTE_SERVERBOUND_PROTOCOL;
        };
    }
}
