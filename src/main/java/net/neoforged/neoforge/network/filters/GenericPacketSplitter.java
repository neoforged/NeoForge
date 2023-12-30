package net.neoforged.neoforge.network.filters;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import net.minecraft.network.CompressionDecoder;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.SplitPacketPayload;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.network.Connection.ATTRIBUTE_CLIENTBOUND_PROTOCOL;
import static net.minecraft.network.Connection.ATTRIBUTE_SERVERBOUND_PROTOCOL;

@Mod.EventBusSubscriber(modid = NeoForgeVersion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ApiStatus.Internal
public class GenericPacketSplitter extends MessageToMessageEncoder<Packet<?>> implements DynamicChannelHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int MAX_PACKET_SIZE = CompressionDecoder.MAXIMUM_UNCOMPRESSED_LENGTH;
    private static final int MAX_PART_SIZE = determineMaxPayloadSize(
            ConnectionProtocol.CONFIGURATION,
            PacketFlow.SERVERBOUND,
            MAX_PACKET_SIZE
    );

    private static final byte STATE_FIRST = 1;
    private static final byte STATE_LAST = 2;

    private final AttributeKey<ConnectionProtocol.CodecData<?>> codecKey;

    public GenericPacketSplitter(Connection connection) {
        this(
                getProtocolKey(connection.getDirection().getOpposite())
        );
    }

    public GenericPacketSplitter(AttributeKey<ConnectionProtocol.CodecData<?>> codecKey) {
        this.codecKey = codecKey;
    }

    @SubscribeEvent
    private static void register(final RegisterPayloadHandlerEvent event) {
        event.registrar(NeoForgeVersion.MOD_ID)
                .versioned(NeoForgeVersion.getSpec())
                .optional()
                .common(
                        SplitPacketPayload.ID,
                        SplitPacketPayload::new,
                        GenericPacketSplitter::receivedPacket);
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

                for (int part = 0; part < parts; part++) {
                    ByteBuf partPrefix;
                    if (part == 0) {
                        partPrefix = Unpooled.buffer(5);
                        partPrefix.writeByte(STATE_FIRST);

                        new FriendlyByteBuf(partPrefix).writeVarInt(codecdata.packetId(packet));
                    } else {
                        partPrefix = Unpooled.buffer(1);
                        partPrefix.writeByte(part == parts - 1 ? STATE_LAST : 0);
                    }

                    int partSize = Math.min(MAX_PART_SIZE, buf.readableBytes());
                    final byte[] prefix = partPrefix.array();
                    final byte[] payloadSlice = buf.slice(buf.readerIndex(), partSize).array();

                    byte[] payload = new byte[prefix.length + payloadSlice.length];
                    System.arraycopy(prefix, 0, payload, 0, prefix.length);
                    System.arraycopy(payloadSlice, 0, payload, prefix.length, payloadSlice.length);

                    out.add(createPacket(codecdata.flow(), payload));

                    partPrefix.release();
                }
                // We cloned all the data into arrays, no need to retain the buffer anymore
                buf.release();
            }
        }
    }

    private static final List<byte[]> receivedBuffers = new ArrayList<>();

    private static void receivedPacket(SplitPacketPayload payload, IPayloadContext context) {
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

        receivedBuffers.add(payload.payload());

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

                context.workHandler()
                        .submitAsync(() -> context.packetHandler().handle(packet))
                        .exceptionally(throwable -> {
                            LOGGER.error("Error handling packet", throwable);
                            return null;
                        });
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
        return NetworkRegistry.getInstance().isVanillaConnection(manager) ? RemoteCompatibility.ABSENT : RemoteCompatibility.PRESENT;
    }

    public static boolean isRemoteCompatible(Connection manager) {
        return getRemoteCompatibility(manager) != RemoteCompatibility.ABSENT;
    }

    public static int determineMaxPayloadSize(ConnectionProtocol protocol, PacketFlow flow, int defaultMaxPayloadSize) {
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

        //Now write a max int value for the maximum length of the byte[]
        temporaryBuf.writeInt(Integer.MAX_VALUE);

        //During normal write operations, this is the prefix content that is written before the actual payload.
        //This is the same for both clientbound and serverbound packets.
        final int prefixSize = temporaryBuf.readableBytes();
        return CompressionDecoder.MAXIMUM_UNCOMPRESSED_LENGTH - prefixSize;
    }

    private static AttributeKey<ConnectionProtocol.CodecData<?>> getProtocolKey(PacketFlow p_294385_) {
        return switch(p_294385_) {
            case CLIENTBOUND -> ATTRIBUTE_CLIENTBOUND_PROTOCOL;
            case SERVERBOUND -> ATTRIBUTE_SERVERBOUND_PROTOCOL;
        };
    }
}
