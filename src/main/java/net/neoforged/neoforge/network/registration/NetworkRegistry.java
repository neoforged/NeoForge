/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.configuration.CommonRegisterTask;
import net.neoforged.neoforge.network.configuration.CommonVersionTask;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.filters.NetworkFilters;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;
import net.neoforged.neoforge.network.negotiation.NegotiableNetworkComponent;
import net.neoforged.neoforge.network.negotiation.NegotiationResult;
import net.neoforged.neoforge.network.negotiation.NetworkComponentNegotiator;
import net.neoforged.neoforge.network.payload.CommonRegisterPayload;
import net.neoforged.neoforge.network.payload.CommonVersionPayload;
import net.neoforged.neoforge.network.payload.MinecraftRegisterPayload;
import net.neoforged.neoforge.network.payload.MinecraftUnregisterPayload;
import net.neoforged.neoforge.network.payload.ModdedNetworkPayload;
import net.neoforged.neoforge.network.payload.ModdedNetworkQueryComponent;
import net.neoforged.neoforge.network.payload.ModdedNetworkQueryPayload;
import net.neoforged.neoforge.network.payload.ModdedNetworkSetupFailedPayload;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;

/**
 * Core registry for all modded networking.
 * <p>
 * This registry is responsible for storing all known modded payloads, and for handling the negotiation of modded network channels between the client and the server.
 * <p>
 * Additionally, this registry is responsible for handling all packets that are not natively known once they arrive at the receiving end.
 * <p>
 * To prevent payloads from being send to a client that has no idea what to do with them, the registry provides endpoints for the vanilla code base to check if a packet can be send to a client.
 */
@ApiStatus.Internal
public class NetworkRegistry {
    /**
     * Declared common networking versions currently supported by NeoForge.
     * <p>
     * At the time of writing, the only version in existence is 1.
     */
    public static final List<Integer> SUPPORTED_COMMON_NETWORKING_VERSIONS = List.of(1);

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Map of NeoForge payloads that may be sent before channel negotiation.
     * TODO: Separate by protocol + flow.
     */
    protected static final Map<ResourceLocation, StreamCodec<FriendlyByteBuf, ? extends CustomPacketPayload>> BUILTIN_PAYLOADS = ImmutableMap.of(
            MinecraftRegisterPayload.ID, MinecraftRegisterPayload.STREAM_CODEC,
            MinecraftUnregisterPayload.ID, MinecraftUnregisterPayload.STREAM_CODEC,
            ModdedNetworkQueryPayload.ID, ModdedNetworkQueryPayload.STREAM_CODEC,
            ModdedNetworkPayload.ID, ModdedNetworkPayload.STREAM_CODEC,
            ModdedNetworkSetupFailedPayload.ID, ModdedNetworkSetupFailedPayload.STREAM_CODEC,
            CommonVersionPayload.ID, CommonVersionPayload.STREAM_CODEC,
            CommonRegisterPayload.ID, CommonRegisterPayload.STREAM_CODEC);

    /**
     * Registry of all custom payload handlers. The initial state of this map should reflect the protocols which support custom payloads.
     * TODO: Change key type to a combination of protocol + flow.
     */
    protected static final Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration<?>>> PAYLOAD_REGISTRATIONS = ImmutableMap.of(
            ConnectionProtocol.CONFIGURATION, new HashMap<>(),
            ConnectionProtocol.PLAY, new HashMap<>());
    protected static final Map<ConnectionProtocol, Map<ResourceLocation, IPayloadHandler<?>>> SERVERBOUND_HANDLERS = ImmutableMap.of(
            ConnectionProtocol.CONFIGURATION, new HashMap<>(),
            ConnectionProtocol.PLAY, new HashMap<>());
    protected static final Map<ConnectionProtocol, Map<ResourceLocation, IPayloadHandler<?>>> CLIENTBOUND_HANDLERS = ImmutableMap.of(
            ConnectionProtocol.CONFIGURATION, new HashMap<>(),
            ConnectionProtocol.PLAY, new HashMap<>());

    protected static boolean setup = false;

    protected NetworkRegistry() {}

    /**
     * Sets up the network registry by firing {@link RegisterPayloadHandlersEvent}, storing the resulting payload registrations in {@link #PAYLOAD_REGISTRATIONS}.
     */
    public static void setup() {
        if (setup) {
            throw new IllegalStateException("The network registry can only be setup once.");
        }

        ModLoader.postEvent(new RegisterPayloadHandlersEvent());

        setup = true;
    }

    /**
     * Registers a new payload.
     * 
     * @param <T>           The class of the payload.
     * @param <B>           The class of the ByteBuf. Only {@link ConnectionProtocol#PLAY play} payloads may use {@link RegistryFriendlyByteBuf}.
     * @param type          The type of the payload.
     * @param codec         The codec for the payload.
     * @param serverHandler The server-side handler for the payload. This handler will be executed on the network thread.
     * @param clientHandler The client-side handler for the payload. This handler will be executed on the network thread.
     * @param protocols     The protocols this payload supports being sent over. Only {@link ConnectionProtocol#CONFIGURATION configuration} and {@link ConnectionProtocol#PLAY play} are supported.
     * @param flow          The flow of this payload. Specify {@link Optional#empty()} to support sending in both directions.
     * @param version       The version of the payload. Increase the payload version if the codec logic or handler logic changes. Neo-Neo connections with mismatched versions are denied.
     * @param optional      If the payload is optional. Any connection with missing non-optional payloads is denied.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T extends CustomPacketPayload, B extends FriendlyByteBuf> void register(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super B, T> codec,
            @Nullable IPayloadHandler<T> serverHandler,
            @Nullable IPayloadHandler<T> clientHandler,
            List<ConnectionProtocol> protocols,
            Optional<PacketFlow> flow,
            String version,
            boolean optional) {
        if (setup) {
            throw new UnsupportedOperationException("Cannot register payload " + type.id() + " after registration phase.");
        }

        if (protocols.isEmpty()) {
            throw new UnsupportedOperationException("Cannot register payload " + type.id() + " with no protocols.");
        }

        if (version.isBlank()) {
            throw new UnsupportedOperationException("Cannot register payload " + type.id() + " with a blank version.");
        }

        if ("minecraft".equals(type.id().getNamespace())) {
            throw new UnsupportedOperationException("Cannot register payload " + type.id() + " using the domain \"minecraft\".");
        }

        PayloadRegistration<T> reg = new PayloadRegistration(type, codec, protocols, flow, version.strip(), optional);

        if (reg.matchesFlow(PacketFlow.SERVERBOUND) && serverHandler == null) {
            throw new IllegalArgumentException("Cannot register serverbound payload " + type.id() + " without a server-side handler");
        }

        for (ConnectionProtocol protocol : protocols) {
            Map<ResourceLocation, PayloadRegistration<?>> byProtocol = PAYLOAD_REGISTRATIONS.get(protocol);

            if (byProtocol == null) {
                throw new UnsupportedOperationException("Cannot register payload " + type.id() + " for unsupported protocol: " + protocol.name());
            }

            if (byProtocol.containsKey(type.id())) {
                throw new UnsupportedOperationException("Cannot register payload " + type.id() + " as it is already registered.");
            }

            byProtocol.put(type.id(), reg);

            if (serverHandler != null && reg.matchesFlow(PacketFlow.SERVERBOUND)) {
                registerHandler(SERVERBOUND_HANDLERS, protocol, PacketFlow.SERVERBOUND, type, serverHandler);
            }
            if (clientHandler != null && reg.matchesFlow(PacketFlow.CLIENTBOUND)) {
                registerHandler(CLIENTBOUND_HANDLERS, protocol, PacketFlow.CLIENTBOUND, type, clientHandler);
            }
        }
    }

    protected static <T extends CustomPacketPayload> void registerHandler(
            Map<ConnectionProtocol, Map<ResourceLocation, IPayloadHandler<?>>> handlers,
            ConnectionProtocol protocol,
            PacketFlow flow,
            CustomPacketPayload.Type<T> type,
            IPayloadHandler<T> handler) {
        Map<ResourceLocation, IPayloadHandler<?>> byProtocol = handlers.get(protocol);
        if (byProtocol == null) {
            throw new UnsupportedOperationException("Cannot register handler for payload " + type.id() + " for unsupported protocol: " + protocol.name());
        }

        if (byProtocol.containsKey(type.id())) {
            throw new UnsupportedOperationException("Duplicate " + flow.id() + " handler registration for payload " + type.id());
        }

        byProtocol.put(type.id(), handler);
    }

    /**
     * Attempts to retrieve the {@link StreamCodec} for a non-vanilla payload.
     * <p>
     * This method hardcodes NeoForge custom packets, stored in {@link #BUILTIN_PAYLOADS}, which may be sent before negotiation.
     * <p>
     * If none of the hardcoded matches succeed, we instead query the registered handlers.
     * <p>
     * The only validation this method performs is that the {@link PacketFlow} is correct. Other checks should be done externally.
     *
     * @param id       The id of the payload.
     * @param protocol The protocol of the connection.
     * @param flow     The flow of the connection.
     * @return A codec for the payload, or null if the payload should be discarded on receipt.
     * 
     * @see {@link #hasChannel(Connection, ConnectionProtocol, ResourceLocation)} to check if a packet can be sent/received.
     * @apiNote This method must not throw exceptions, as it is called within another codec on the network thread.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static StreamCodec<? super FriendlyByteBuf, ? extends CustomPacketPayload> getCodec(ResourceLocation id, ConnectionProtocol protocol, PacketFlow flow) {
        // Custom modded packets which can be sent before a payload setup is negotiated.
        if (BUILTIN_PAYLOADS.containsKey(id)) {
            return BUILTIN_PAYLOADS.get(id);
        }

        // Now ask the protocol what kind of payload is being sent and get the channel for it.
        if (PAYLOAD_REGISTRATIONS.containsKey(protocol)) {
            PayloadRegistration<?> registration = PAYLOAD_REGISTRATIONS.get(protocol).get(id);

            // These two checks can only be hit on receipt of a payload, as senders will be checked before reaching this method.
            if (registration == null) {
                LOGGER.warn("No registration for payload {}; refusing to decode.", id);
                return null;
            }

            if (registration.flow().isPresent() && registration.flow().get() != flow) {
                LOGGER.warn("Received {} on the {}, expected to receive on the {}; refusing to decode.", id, flow.getReceptionSide(), registration.flow().get().getReceptionSide());
                return null;
            }

            return (StreamCodec<? super FriendlyByteBuf, ? extends CustomPacketPayload>) registration.codec();
        } else {
            // Log a loud warning here. This should be impossible, as this method is only invoked from CustomPacketPayload#codec
            LOGGER.error("A packet was received while not in the configuration or play phase.");
            dumpStackToLog();
            return null;
        }
    }

    /**
     * Checks if a payload is a modded payload. A modded payload is any payload that does not have `minecraft` as the domain, and is not a discarded payload.
     * <p>
     * The special handling for {@link DiscardedPayload} is because it falsely reports its type as the type that failed to decode.
     * 
     * @return True if the payload is modded, and modded payload handling should be invoked for it.
     */
    public static boolean isModdedPayload(CustomPacketPayload payload) {
        return !(payload instanceof DiscardedPayload) && !"minecraft".equals(payload.type().id().getNamespace());
    }

    /**
     * Handles modded payloads on the server. Invoked after built-in handling.
     * <p>
     * Called on the network thread.
     *
     * @param listener The listener which received the packet.
     * @param packet   The packet that was received.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void handleModdedPayload(ServerCommonPacketListener listener, ServerboundCustomPayloadPacket packet) {
        NetworkPayloadSetup payloadSetup = ChannelAttributes.getPayloadSetup(listener.getConnection());
        // Check if channels were negotiated.
        if (payloadSetup == null) {
            LOGGER.warn("Received a modded payload before channel negotiation; disconnecting.");
            listener.disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s (No Payload Setup)".formatted(NeoForgeVersion.getVersion())));
            return;
        }

        ServerPayloadContext context = new ServerPayloadContext(listener, packet.payload().type().id());

        if (SERVERBOUND_HANDLERS.containsKey(listener.protocol())) {
            // Get the configuration channel for the packet.
            NetworkChannel channel = payloadSetup.getChannel(listener.protocol(), context.payloadId());

            // Check if the channel should even be processed.
            if (channel == null && !hasAdhocChannel(listener.protocol(), context.payloadId(), PacketFlow.SERVERBOUND)) {
                LOGGER.warn("Received a modded payload {} with an unknown or unaccepted channel; disconnecting.", context.payloadId());
                listener.disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s (No Channel for %s)".formatted(NeoForgeVersion.getVersion(), context.payloadId().toString())));
                return;
            }

            IPayloadHandler handler = SERVERBOUND_HANDLERS.get(listener.protocol()).get(context.payloadId());
            if (handler == null) {
                LOGGER.error("Received a modded payload {} with no registration; disconnecting.", context.payloadId());
                listener.disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s (No Handler for %s)".formatted(NeoForgeVersion.getVersion(), context.payloadId().toString())));
                dumpStackToLog(); // This case is only likely when handling packets without serialization, i.e. from a compound listener, so this can help debug why.
                return;
            }

            handler.handle(packet.payload(), context);
        } else {
            LOGGER.error("Received a modded payload {} while not in the configuration or play phase; disconnecting.", context.payloadId());
            listener.disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s (Invalid Protocol %s)".formatted(NeoForgeVersion.getVersion(), listener.protocol().name())));
        }
    }

    /**
     * Invoked by the server when it completes the negotiation with the client during the configuration phase.
     * <p>
     * This method determines what the versions of each of the channels are, and checks if the client and server have a compatible set of network channels.
     * <p>
     * If the negotiation fails, a custom packet is sent to the client to inform it of the failure, and which will allow the client to disconnect gracefully with an indicative error screen.
     * <p>
     * This method should only be invoked for modded connections.
     * Use {@link #initializeOtherConnection(ServerConfigurationPacketListener)} to indicate that during the configuration phase of the network handshake between a client and the server, a vanilla connection was detected.
     * <p>
     * Invoked on the network thread.
     *
     * @param listener       The listener which completed the negotiation.
     * @param clientChannels The network channels that the client has available.
     */
    public static void initializeNeoForgeConnection(ServerConfigurationPacketListener listener, Map<ConnectionProtocol, Set<ModdedNetworkQueryComponent>> clientChannels) {
        ChannelAttributes.setPayloadSetup(listener.getConnection(), NetworkPayloadSetup.empty());
        ChannelAttributes.setConnectionType(listener.getConnection(), listener.getConnectionType());

        Map<ConnectionProtocol, NegotiationResult> results = new IdentityHashMap<>();

        for (ConnectionProtocol protocol : PAYLOAD_REGISTRATIONS.keySet()) {
            NegotiationResult negotiationResult = NetworkComponentNegotiator.negotiate(
                    PAYLOAD_REGISTRATIONS.get(protocol).values().stream().map(NegotiableNetworkComponent::new).toList(),
                    clientChannels.getOrDefault(protocol, Collections.emptySet()).stream().map(NegotiableNetworkComponent::new).toList());

            // Negotiation failed. Disconnect the client.
            if (!negotiationResult.success()) {
                if (!negotiationResult.failureReasons().isEmpty()) {
                    listener.send(new ModdedNetworkSetupFailedPayload(negotiationResult.failureReasons()));
                }

                listener.disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s".formatted(NeoForgeVersion.getVersion())));
                return;
            }
            results.put(protocol, negotiationResult);
        }

        NetworkPayloadSetup setup = NetworkPayloadSetup.from(results);

        ChannelAttributes.setPayloadSetup(listener.getConnection(), setup);
        NetworkFilters.injectIfNecessary(listener.getConnection());

        listener.send(new ModdedNetworkPayload(setup));
        ImmutableSet.Builder<ResourceLocation> nowListeningOn = ImmutableSet.builder();
        nowListeningOn.addAll(getInitialListeningChannels(listener.flow()));
        nowListeningOn.addAll(setup.getChannels(ConnectionProtocol.CONFIGURATION).keySet());
        listener.send(new MinecraftRegisterPayload(nowListeningOn.build()));
    }

    /**
     * Invoked by the {@link ServerConfigurationPacketListenerImpl} when a vanilla or other connection is detected.
     *
     * @param listener The listener which detected the vanilla connection during the configuration phase.
     * @return True if the vanilla connection should be handled by the server, false otherwise.
     */
    public static boolean initializeOtherConnection(ServerConfigurationPacketListener listener) {
        NetworkFilters.cleanIfNecessary(listener.getConnection());

        // Because we are in vanilla land, no matter what we are not able to support any custom channels.
        ChannelAttributes.setPayloadSetup(listener.getConnection(), NetworkPayloadSetup.empty());
        ChannelAttributes.setConnectionType(listener.getConnection(), listener.getConnectionType());

        for (ConnectionProtocol protocol : PAYLOAD_REGISTRATIONS.keySet()) {
            NegotiationResult negotiationResult = NetworkComponentNegotiator.negotiate(
                    PAYLOAD_REGISTRATIONS.get(protocol).entrySet().stream()
                            .map(entry -> new NegotiableNetworkComponent(entry.getKey(), entry.getValue().version(), entry.getValue().flow(), entry.getValue().optional()))
                            .toList(),
                    List.of());

            //Negotiation failed. Disconnect the client.
            if (!negotiationResult.success()) {
                listener.disconnect(Component.translatableWithFallback("neoforge.network.negotiation.failure.vanilla.client.not_supported",
                        "You are trying to connect to a server that is running NeoForge, but you are not. Please install NeoForge Version: %s to connect to this server.", NeoForgeVersion.getVersion()));
                return false;
            }
        }

        NetworkFilters.injectIfNecessary(listener.getConnection());

        ImmutableSet.Builder<ResourceLocation> nowListeningOn = ImmutableSet.builder();
        nowListeningOn.addAll(getInitialListeningChannels(listener.flow()));
        PAYLOAD_REGISTRATIONS.get(ConnectionProtocol.CONFIGURATION).entrySet().stream()
                .filter(registration -> registration.getValue().matchesFlow(listener.flow()))
                .filter(registration -> registration.getValue().optional())
                .forEach(registration -> nowListeningOn.add(registration.getKey()));
        listener.send(new MinecraftRegisterPayload(nowListeningOn.build()));

        return true;
    }

    /**
     * Validates that a {@link ClientboundCustomPayloadPacket} may be sent to the client.
     *
     * @param packet   The packet that is about to be sent.
     * @param listener The listener that wants to send the packet.
     * @throws UnsupportedOperationException if the packet may not be sent.
     */
    public static void checkPacket(Packet<?> packet, ServerCommonPacketListener listener) {
        if (packet instanceof ClientboundCustomPayloadPacket customPayloadPacket) {
            ResourceLocation id = customPayloadPacket.payload().type().id();
            if (BUILTIN_PAYLOADS.containsKey(id) || "minecraft".equals(id.getNamespace())) {
                return;
            }

            if (hasChannel(listener, customPayloadPacket.payload().type().id())) {
                return;
            }

            throw new UnsupportedOperationException("Payload %s may not be sent to the client!".formatted(customPayloadPacket.payload().type().id()));
        }
    }

    /**
     * Validates that a {@link ServerboundCustomPayloadPacket} may be sent to the server.
     *
     * @param packet   The packet that is about to be sent.
     * @param listener The listener that wants to send the packet.
     * @throws UnsupportedOperationException if the packet may not be sent.
     */
    public static void checkPacket(Packet<?> packet, ClientCommonPacketListener listener) {
        if (packet instanceof ServerboundCustomPayloadPacket customPayloadPacket) {
            ResourceLocation id = customPayloadPacket.payload().type().id();
            if (BUILTIN_PAYLOADS.containsKey(id) || "minecraft".equals(id.getNamespace())) {
                return;
            }

            if (hasChannel(listener, customPayloadPacket.payload().type().id())) {
                return;
            }

            throw new UnsupportedOperationException("Payload %s may not be sent to the server!".formatted(customPayloadPacket.payload().type().id()));
        }
    }

    /**
     * Checks if a given payload is ad-hoc readable, that is, may be read without a negotiated channel being present.
     * <p>
     * This is possible when an optional registration exists for the payload.
     *
     * @param id   The id of the packet.
     * @param flow The flow of the packet.
     * @return True if the packet is ad-hoc readable, false otherwise.
     */
    protected static boolean hasAdhocChannel(ConnectionProtocol protocol, ResourceLocation id, PacketFlow flow) {
        PayloadRegistration<?> reg = PAYLOAD_REGISTRATIONS.getOrDefault(protocol, Collections.emptyMap()).get(id);
        return reg != null && reg.optional() && reg.matchesFlow(flow);
    }

    /**
     * Checks if the packet listener's connection can send/receive the given payload.
     *
     * @param listener  The listener to check.
     * @param payloadId The payload id to check.
     * @return True if the listener has a connection setup that can transmit the given payload id, false otherwise.
     */
    public static boolean hasChannel(ICommonPacketListener listener, ResourceLocation payloadId) {
        return hasChannel(listener.getConnection(), listener.protocol(), payloadId);
    }

    /**
     * Indicates whether the given connection has a connection setup that can transmit the given payload id.
     *
     * @param connection The connection to check.
     * @param protocol   The protocol to check. Pass null to check against all protocols.
     * @param payloadId  The payload id to check.
     * @return True if the connection has a connection setup that can transmit the given payload id, false otherwise.
     */
    public static boolean hasChannel(Connection connection, @Nullable ConnectionProtocol protocol, ResourceLocation payloadId) {
        NetworkPayloadSetup payloadSetup = ChannelAttributes.getPayloadSetup(connection);
        if (payloadSetup != null) {
            // If a protocol is specified, only check against channels for that protocol
            // Otherwise check against all protocols.
            if (protocol != null && payloadSetup.getChannels(protocol).containsKey(payloadId)) {
                return true;
            } else if (protocol == null && payloadSetup.channels().values().stream().anyMatch(map -> map.containsKey(payloadId))) {
                return true;
            }
        }

        // Support declaration of additional channels through c:register
        if (protocol != null && ChannelAttributes.getOrCreateCommonChannels(connection, protocol).contains(payloadId)) {
            return true;
        }

        // Always fall back to ad-hoc channels if we failed to find an entry in the payload setup.
        return ChannelAttributes.getOrCreateAdHocChannels(connection).contains(payloadId);
    }

    /**
     * Filters the given packets for a bundle packet in the game phase of the connection.
     *
     * @param context The context of the connection.
     * @param packets The packets to filter.
     * @param <T>     The type of the listener.
     * @return The filtered packets.
     */
    public static <T extends PacketListener> List<Packet<?>> filterGameBundlePackets(ChannelHandlerContext context, Iterable<Packet<? super T>> packets) {
        NetworkPayloadSetup payloadSetup = context.channel().attr(ChannelAttributes.PAYLOAD_SETUP).get();
        if (payloadSetup == null) {
            LOGGER.trace("Somebody tried to filter bundled packets to a client that has not negotiated with the server. Not filtering.");
            return Lists.newArrayList(packets.iterator());
        }

        final List<Packet<?>> toSend = new ArrayList<>();
        packets.forEach(packet -> {
            if (!(packet instanceof ClientboundCustomPayloadPacket customPayloadPacket)) {
                toSend.add(packet);
                return;
            }

            ResourceLocation id = customPayloadPacket.payload().type().id();
            if (BUILTIN_PAYLOADS.containsKey(id) || "minecraft".equals(id.getNamespace())) {
                toSend.add(packet);
                return;
            }

            NetworkChannel channel = payloadSetup.getChannel(ConnectionProtocol.PLAY, customPayloadPacket.payload().type().id());

            if (channel == null) {
                LOGGER.trace("Somebody tried to send: {} to a client which cannot accept it. Not sending packet.", customPayloadPacket.payload().type().id());
                return;
            }

            toSend.add(packet);
        });

        return toSend;
    }

    /**
     * Configures a mock connection for use in game tests. The mock connection will act as if the server and client are fully compatible and both NeoForge.
     *
     * @param connection The connection to configure.
     */
    @VisibleForTesting
    public static void configureMockConnection(Connection connection) {
        ChannelAttributes.setPayloadSetup(connection, NetworkPayloadSetup.empty());
        ChannelAttributes.setConnectionType(connection, ConnectionType.NEOFORGE);

        NetworkPayloadSetup setup = new NetworkPayloadSetup(
                PAYLOAD_REGISTRATIONS.entrySet().stream()
                        .map(entry -> Map.entry(entry.getKey(),
                                entry.getValue().values().stream().map(reg -> new NetworkChannel(reg.id(), reg.version())).collect(Collectors.toMap(NetworkChannel::id, Function.identity()))))
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

        ChannelAttributes.setPayloadSetup(connection, setup);

        NetworkFilters.injectIfNecessary(connection);
    }

    /**
     * Invoked to add to the known ad-hoc channels on a connection.
     * <p>
     * Invoked on the network thread.
     *
     * @param connection        The connection to add the channels to.
     * @param resourceLocations The resource locations to add.
     */
    public static void onMinecraftRegister(Connection connection, Set<ResourceLocation> resourceLocations) {
        ChannelAttributes.getOrCreateAdHocChannels(connection).addAll(resourceLocations);
    }

    /**
     * Invoked to remove from the known ad-hoc channels on a connection.
     * <p>
     * Invoked on the network thread.
     *
     * @param connection        The connection to remove the channels from.
     * @param resourceLocations The resource locations to remove.
     */
    public static void onMinecraftUnregister(Connection connection, Set<ResourceLocation> resourceLocations) {
        ChannelAttributes.getOrCreateAdHocChannels(connection).removeAll(resourceLocations);
    }

    /**
     * {@return the initial channels for the configuration phase.}
     */
    public static Set<ResourceLocation> getInitialListeningChannels(PacketFlow flow) {
        // TODO: Separate builtins by flow and return them appropriately here.
        return BUILTIN_PAYLOADS.keySet();
    }

    public static Set<ResourceLocation> getInitialServerUnregisterChannels() {
        final ImmutableSet.Builder<ResourceLocation> nowForgottenChannels = ImmutableSet.builder();
        nowForgottenChannels.add(MinecraftRegisterPayload.ID);
        nowForgottenChannels.add(MinecraftUnregisterPayload.ID);
        PAYLOAD_REGISTRATIONS.get(ConnectionProtocol.PLAY).entrySet().stream()
                .filter(registration -> registration.getValue().flow().isEmpty() || registration.getValue().flow().get() == PacketFlow.SERVERBOUND)
                .filter(registration -> registration.getValue().optional())
                .forEach(registration -> nowForgottenChannels.add(registration.getKey()));
        return nowForgottenChannels.build();
    }

    /**
     * Called when a {@link CommonVersionPayload} is received.
     * Triggers a disconnect if none of the supplied version match our supported ones.
     * Since we only support one version, we don't need to do further handling or record the "active" version just yet.
     * <p>
     * Invoked on the network thread.
     * 
     * @param listener The receiving listener.
     * @param payload  The incoming version payload.
     */
    public static void checkCommonVersion(ICommonPacketListener listener, CommonVersionPayload payload) {
        List<Integer> otherVersions = payload.versions();
        if (otherVersions.stream().noneMatch(SUPPORTED_COMMON_NETWORKING_VERSIONS::contains)) {
            String versions = String.join(", ", SUPPORTED_COMMON_NETWORKING_VERSIONS.stream().map(i -> i.toString()).toList());
            listener.disconnect(Component.literal("Unsupported common network version. This installation of NeoForge only supports: " + versions));
        }

        if (listener.protocol() == ConnectionProtocol.CONFIGURATION) {
            if (listener.flow() == PacketFlow.SERVERBOUND) {
                ((ServerConfigurationPacketListener) listener).finishCurrentTask(CommonVersionTask.TYPE);
            } else {
                listener.send(new CommonVersionPayload());
            }
        }
    }

    /**
     * Replaces any existing common channels with the incoming ones from a {@link CommonRegisterPayload}.
     * <p>
     * Invoked on the network thread.
     *
     * @param listener The receiving listener.
     * @param payload  The incoming register payload.
     */
    public static void onCommonRegister(ICommonPacketListener listener, CommonRegisterPayload payload) {
        Set<ResourceLocation> channels = ChannelAttributes.getOrCreateCommonChannels(listener.getConnection(), payload.protocol());
        channels.clear();
        channels.addAll(payload.channels());

        if (listener.protocol() == ConnectionProtocol.CONFIGURATION) {
            if (listener.flow() == PacketFlow.SERVERBOUND) {
                ((ServerConfigurationPacketListener) listener).finishCurrentTask(CommonRegisterTask.TYPE);
            } else {
                listener.send(new CommonRegisterPayload(1, ConnectionProtocol.PLAY, getCommonPlayChannels(PacketFlow.CLIENTBOUND)));
            }
        }
    }

    public static Set<ResourceLocation> getCommonPlayChannels(PacketFlow flow) {
        return PAYLOAD_REGISTRATIONS.get(ConnectionProtocol.PLAY)
                .entrySet().stream()
                .filter(registration -> registration.getValue().matchesFlow(flow))
                .filter(registration -> registration.getValue().optional())
                .map(registration -> registration.getKey())
                .collect(Collectors.toSet());
    }

    /**
     * Invoked when the configuration phase of a connection is completed.
     * <p>
     * Updates the ad-hoc channels to prepare for the game phase by removing the initial channels and building a new list based on the connection type.
     * 
     * @param listener The packet listener finishing the configuration phase
     */
    public static void onConfigurationFinished(ICommonPacketListener listener) {
        NetworkPayloadSetup setup = ChannelAttributes.getPayloadSetup(listener.getConnection());
        if (setup == null) {
            LOGGER.error("Somebody tried to finish the configuration phase of a connection that has not performed channel negotiation. Not finishing configuration.");
            return;
        }

        final ImmutableSet.Builder<ResourceLocation> notListeningAnymoreOn = ImmutableSet.builder();
        notListeningAnymoreOn.addAll(getInitialListeningChannels(listener.flow()));
        notListeningAnymoreOn.addAll(setup.getChannels(ConnectionProtocol.CONFIGURATION).keySet());
        listener.send(new MinecraftUnregisterPayload(notListeningAnymoreOn.build()));

        final ImmutableSet.Builder<ResourceLocation> nowListeningOn = ImmutableSet.builder();
        nowListeningOn.add(MinecraftRegisterPayload.ID);
        nowListeningOn.add(MinecraftUnregisterPayload.ID);
        if (listener.getConnectionType().isNeoForge()) {
            nowListeningOn.add(ModdedNetworkQueryPayload.ID);
        } else {
            // For non-Neo connections, send the registered channels
            nowListeningOn.addAll(getCommonPlayChannels(listener.flow()));
        }
        listener.send(new MinecraftRegisterPayload(nowListeningOn.build()));
    }

    public static ConnectionType getConnectionType(Connection connection) {
        return Objects.requireNonNull(ChannelAttributes.getConnectionType(connection), "no connection type on connection!");
    }

    /**
     * Helper to guard futures generated by {@link IPayloadContext} against exceptions.
     */
    public static <T> CompletableFuture<T> guard(CompletableFuture<T> future, ResourceLocation payloadId) {
        return future.exceptionally(
                ex -> {
                    NetworkRegistry.LOGGER.error("Failed to process a synchronized task of the payload: %s".formatted(payloadId), ex);
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    public static <T extends PacketListener> void handlePacketUnchecked(Packet<T> packet, PacketListener listener) {
        try {
            packet.handle((T) listener);
        } catch (ClassCastException exception) {
            throw new IllegalStateException("Attempted to handle a packet in a listener that does not support it.", exception);
        }
    }

    /**
     * Used in place of {@link Thread#dumpStack()} as that logs to {@link System#err}.
     */
    private static void dumpStackToLog() {
        LOGGER.error("", new Exception("Stack Trace"));
    }
}
