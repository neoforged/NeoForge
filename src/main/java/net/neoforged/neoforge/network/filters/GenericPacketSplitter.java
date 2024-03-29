/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.filters;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.CompressionDecoder;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.HandlerNames;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.connection.ChannelAwareFriendlyByteBuf;
import net.neoforged.neoforge.network.connection.ConnectionPhase;
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

    private record SizeLimits(int packet, int part) {
        public SizeLimits(int packet) {
            this(packet, determineMaxPayloadSize(packet));
        }
    }

    // Used for in-memory connections
    private static final SizeLimits compressedSizeLimits = new SizeLimits(CompressionDecoder.MAXIMUM_COMPRESSED_LENGTH);
    // Used for non-in-memory connections
    private static final SizeLimits uncompressedSizeLimits = new SizeLimits(CompressionDecoder.MAXIMUM_UNCOMPRESSED_LENGTH);

    private static final byte STATE_FIRST = 1;
    private static final byte STATE_LAST = 2;

    public static final String CHANNEL_HANDLER_NAME = "neoforge:splitter";

    @SubscribeEvent
    private static void register(final RegisterPayloadHandlerEvent event) {
        event.registrar(NeoForgeVersion.MOD_ID)
                .versioned(NeoForgeVersion.getSpec())
                .optional()
                .common(
                        SplitPacketPayload.TYPE,
                        SplitPacketPayload.STREAM_CODEC,
                        (payload, context) -> {
                            if (context.channelHandlerContext().pipeline().get(CHANNEL_HANDLER_NAME) instanceof GenericPacketSplitter splitter) {
                                splitter.receivedPacket(payload, context);
                            } else {
                                LOGGER.error("Received split packet without a splitter");
                                context.replyHandler().disconnect(Component.translatable("neoforge.network.packet_splitter.unknown"));
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

        if (!((ctx.pipeline().get(HandlerNames.ENCODER) instanceof PacketEncoder<?> encoder))) {
            // No encoder in pipeline, pipeline is probably unbound
            out.add(packet);
            return;
        }

        boolean hasCompressor = ctx.pipeline().get(HandlerNames.COMPRESS) != null;
        // If there IS a compressor, use the NON-compressed limit since the compressor will compress after us!
        var sizeLimits = hasCompressor ? uncompressedSizeLimits : compressedSizeLimits;

        FriendlyByteBuf buf = new ChannelAwareFriendlyByteBuf(Unpooled.buffer(), ctx);
        try {
            @SuppressWarnings("unchecked")
            var codec = (StreamCodec<ByteBuf, Packet<?>>) encoder.getProtocolInfo().codec();
            codec.encode(buf, packet);
            if (buf.readableBytes() <= sizeLimits.packet()) {
                out.add(packet);
                return;
            }

            int parts = (int) Math.ceil(((double) buf.readableBytes()) / sizeLimits.part());
            if (parts == 1) {
                out.add(packet);
                return;
            }

            final byte[] packetData = buf.array();
            for (int part = 0; part < parts; part++) {
                final int partSize = Math.min(sizeLimits.part(), packetData.length - (part * sizeLimits.part()));
                final byte[] payloadSlice = new byte[partSize + 1];

                byte prefix = part == 0 ? STATE_FIRST : part == parts - 1 ? STATE_LAST : 0;
                payloadSlice[0] = prefix;
                System.arraycopy(packetData, part * sizeLimits.part(), payloadSlice, 1, partSize);

                out.add(createPacket(encoder.getProtocolInfo().flow(), payloadSlice));
            }
        } finally {
            buf.release();
        }
    }

    private final List<byte[]> receivedBuffers = new ArrayList<>();

    private void receivedPacket(SplitPacketPayload payload, IPayloadContext context) {
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
            FriendlyByteBuf full = new ChannelAwareFriendlyByteBuf(Unpooled.wrappedBuffer(buffers), context.channelHandlerContext());

            try {
                Packet<?> packet = context.protocolInfo().codec().decode(full);

                context.workHandler()
                        .submitAsync(() -> context.packetHandler().handle(packet))
                        .exceptionally(throwable -> {
                            LOGGER.error("Error handling packet", throwable);
                            return null;
                        });
            } finally {
                receivedBuffers.clear();
                full.release();
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
        return isRemoteCompatible(manager);
    }

    public enum RemoteCompatibility {
        ABSENT,
        PRESENT
    }

    public static RemoteCompatibility getRemoteCompatibility(Connection manager) {
        return NetworkRegistry.getInstance().isConnected(manager, ConnectionPhase.ANY, SplitPacketPayload.TYPE.id()) ? RemoteCompatibility.PRESENT : RemoteCompatibility.ABSENT;
    }

    public static boolean isRemoteCompatible(Connection manager) {
        return getRemoteCompatibility(manager) != RemoteCompatibility.ABSENT;
    }

    public static int determineMaxPayloadSize(int maxPacketSize) {
        final FriendlyByteBuf temporaryBuf = new FriendlyByteBuf(Unpooled.buffer());
        //Simulate writing our split packet with a full byte array

        //First write the packet id, as does the vanilla packet encoder.
        //Write a byte for the packet id. Technically it could be multiple bytes, should the packet id be larger than 127.
        temporaryBuf.writeByte(0);

        //Then write the payload id, as does the custom payload packet, regardless of flow.
        temporaryBuf.writeResourceLocation(SplitPacketPayload.TYPE.id());

        //Then write the byte prefix to indicate the state of the packet.
        temporaryBuf.writeByte(STATE_FIRST);

        //Then write the potential packet id of the split packet
        temporaryBuf.writeVarInt(Integer.MAX_VALUE);

        //Now write a max int value for the maximum length of the byte[]
        temporaryBuf.writeInt(Integer.MAX_VALUE);

        //During normal write operations, this is the prefix content that is written before the actual payload.
        //This is the same for both clientbound and serverbound packets.
        final int prefixSize = temporaryBuf.readableBytes();
        return maxPacketSize - prefixSize;
    }
}
