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
import io.netty.util.AttributeKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.connection.ConnectionUtils;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.filters.NetworkFilters;
import net.neoforged.neoforge.network.handling.ClientPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;
import net.neoforged.neoforge.network.negotiation.NegotiableNetworkComponent;
import net.neoforged.neoforge.network.negotiation.NegotiationResult;
import net.neoforged.neoforge.network.negotiation.NetworkComponentNegotiator;
import net.neoforged.neoforge.network.payload.MinecraftRegisterPayload;
import net.neoforged.neoforge.network.payload.MinecraftUnregisterPayload;
import net.neoforged.neoforge.network.payload.ModdedNetworkPayload;
import net.neoforged.neoforge.network.payload.ModdedNetworkQueryComponent;
import net.neoforged.neoforge.network.payload.ModdedNetworkQueryPayload;
import net.neoforged.neoforge.network.payload.ModdedNetworkSetupFailedPayload;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Core registry for all modded networking.
 * <p>
 * This registry is responsible for storing all known modded payloads, and for handling the negotiation of modded network channels between the client and the server.
 * <p>
 * Additionally, this registry is responsible for handling all packets that are not natively known once they arrive at the receiving end.
 * <p>
 * To prevent payloads from being send to a client that has no idea what to do with them, the registry provides endpoints for the vanilla code base to check if a packet can be send to a client.
 * </p>
 */
@ApiStatus.Internal
public class NetworkRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final AttributeKey<NetworkPayloadSetup> ATTRIBUTE_PAYLOAD_SETUP = AttributeKey.valueOf("neoforge:payload_setup");
    private static final AttributeKey<Set<ResourceLocation>> ATTRIBUTE_ADHOC_CHANNELS = AttributeKey.valueOf("neoforge:adhoc_channels");
    private static final AttributeKey<ConnectionType> ATTRIBUTE_CONNECTION_TYPE = AttributeKey.valueOf("neoforge:connection_type");
    private static final AttributeKey<PacketFlow> ATTRIBUTE_FLOW = AttributeKey.valueOf("neoforge:flow");

    /**
     * Map of NeoForge payloads that may be sent before channel negotiation.
     */
    private static final Map<ResourceLocation, FriendlyByteBuf.Reader<? extends CustomPacketPayload>> BUILTIN_PAYLOADS = ImmutableMap.of(
            MinecraftRegisterPayload.ID, MinecraftRegisterPayload.READER,
            MinecraftUnregisterPayload.ID, MinecraftUnregisterPayload.READER,
            ModdedNetworkQueryPayload.ID, ModdedNetworkQueryPayload.READER,
            ModdedNetworkPayload.ID, ModdedNetworkPayload.READER,
            ModdedNetworkSetupFailedPayload.ID, ModdedNetworkSetupFailedPayload.READER);

    /**
     * Registry of all custom payload handlers. The initial state of this map should reflect the protocols which support custom payloads.
     */
    private static final Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration<?>>> PAYLOAD_REGISTRATIONS = ImmutableMap.of(
            ConnectionProtocol.CONFIGURATION, new HashMap<>(),
            ConnectionProtocol.PLAY, new HashMap<>());

    private static boolean setup = false;

    private NetworkRegistry() {}

    /**
     * Sets up the network registry by firing {@link RegisterPayloadHandlerEvent}, storing the resulting payload registrations in {@link #PAYLOAD_REGISTRATIONS}.
     */
    public static void setup() {
        if (setup)
            throw new IllegalStateException("The network registry can only be setup once.");

        setup = true;

        final Map<String, ModdedPacketRegistrar> registrarsByNamespace = Collections.synchronizedMap(new HashMap<>());
        ModLoader.get().postEvent(new RegisterPayloadHandlerEvent(namespace -> registrarsByNamespace.computeIfAbsent(namespace, ModdedPacketRegistrar::new)));
        registrarsByNamespace.values().forEach(ModdedPacketRegistrar::invalidate);

        registrarsByNamespace.values().forEach(registrar -> registrar.getRegistrations().forEach((protocol, registrations) -> {
            PAYLOAD_REGISTRATIONS.get(protocol).putAll(registrations);
        }));
    }

    /**
     * Retrieves the correct reader for a payload.
     * <p>
     * This method hardcodes two sets of payload readers:
     * <ul>
     * <li>Vanilla custom packets, stored in the <code>knownTypes</code> parameter.</li>
     * <li>NeoForge custom packets, stored in {@link #BUILTIN_PAYLOADS}, which may be sent before negotiation.</li>
     * </ul>
     * <p>
     * If none of the hardcoded matches succeed, we proceed to the normal logic:
     * <p>
     * If the connection is properly configured, the payload is known by the connection, and the payload is known by the registry, we return the registered handler.
     * If any of those conditions fail, null is returned, and the packet is discarded.
     *
     * @param id         The id of the payload.
     * @param context    The context of the channel.
     * @param protocol   The protocol of the connection.
     * @param knownTypes The known types of the connection.
     * @return A reader for the payload, or null if the payload should be discarded.
     */
    @Nullable
    public static FriendlyByteBuf.Reader<? extends CustomPacketPayload> getReader(ResourceLocation id, ChannelHandlerContext context, ConnectionProtocol protocol, Map<ResourceLocation, FriendlyByteBuf.Reader<? extends CustomPacketPayload>> knownTypes) {
        // Vanilla custom packet, let it deal with it.
        if (knownTypes.containsKey(id)) {
            return knownTypes.get(id);
        }

        // Custom modded packets which can be sent before a payload setup is negotiated.
        if (BUILTIN_PAYLOADS.containsKey(id)) {
            return BUILTIN_PAYLOADS.get(id);
        }

        // Check the network setup.
        final NetworkPayloadSetup payloadSetup = context.channel().attr(ATTRIBUTE_PAYLOAD_SETUP).get();
        final PacketFlow flow = context.channel().attr(ATTRIBUTE_FLOW).get();
        if (payloadSetup == null || flow == null) {
            // Network hasn't been setup yet.
            LOGGER.warn("Received a modded custom payload packet {} that has not been negotiated with the server. Not parsing packet.", id);
            return null;
        }

        // Now ask the protocol what kind of payload is being sent and get the channel for it.
        if (PAYLOAD_REGISTRATIONS.containsKey(protocol)) {
            final NetworkChannel channel = payloadSetup.channels().get(protocol).get(id);

            // Validate that everything is okay before attempting to return a reader.
            if (channel == null && !isAdhocChannelReadable(protocol, id, flow)) {
                LOGGER.warn("Received a modded custom payload packet {} with an unknown or unaccepted channel. Not parsing packet.", id);
                return null;
            }

            final PayloadRegistration<?> registration = PAYLOAD_REGISTRATIONS.get(protocol).get(id);
            if (registration == null) {
                LOGGER.error("Received a modded custom payload packet {} with an unknown or unaccepted channel. Not parsing packet.", channel.id());
                throw new IllegalStateException("A payload with an unknown or unaccepted channel was received after negotiation succeeded. Somebody changed the channels known to NeoForge!");
            }

            if (registration.flow().isPresent()) {
                if (registration.flow().get() != flow) {
                    LOGGER.warn("Received a modded custom payload packet {} on the incorrect side. Disconnecting client.", channel.id());
                    final Connection connection = ConnectionUtils.getConnection(context);
                    final PacketListener listener = connection.getPacketListener();
                    if (listener instanceof ServerGamePacketListener serverListener) {
                        serverListener.disconnect(Component.translatableWithFallback("neoforge.network.invalid_flow", "Failed to process a payload that was send with an invalid flow: %s", flow));
                    } else if (listener instanceof ClientGamePacketListener clientListener) {
                        clientListener.getConnection().disconnect(Component.translatableWithFallback("neoforge.network.invalid_flow", "Failed to process a payload that was send with an invalid flow: %s", flow));
                    } else {
                        LOGGER.error("Received a modded custom payload packet {} that is not supposed to be sent to the server. Disconnecting client, but the listener is not a game listener. This should not happen.", channel.id());
                        throw new IllegalStateException("Unknown packet listener type, expected either ServerGamePacketListener or ClientGamePacketListener but got " + listener.getClass().getName());
                    }
                }
            }

            return registration;
        } else {
            // Error case, somebody is trying to send a payload for a protocol that does not support them.
            LOGGER.error("Received a modded custom payload packet from a client that is not in the configuration or play phase. Not parsing packet.");
            throw new IllegalStateException("A packet was received while not in the configuration or play phase.");
        }
    }

    /**
     * Invoked on the server when a modded payload is received on a modded connection.
     * <p>
     * To reach this point, the packet has already been decoded and passed the checks enforced by {@link #getReader}, so we can omit the complex validation.
     *
     * @param listener The listener which received the packet.
     * @param packet   The packet that was received.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void handleCustomPayload(ServerCommonPacketListener listener, ServerboundCustomPayloadPacket packet) {
        if (PAYLOAD_REGISTRATIONS.containsKey(listener.protocol())) {
            NetworkPayloadSetup payloadSetup = listener.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).get();
            ServerPayloadContext context = new ServerPayloadContext(listener, packet.payload().id());
            NetworkChannel channel = payloadSetup.channels().get(listener.protocol()).get(packet.payload().id());
            ResourceLocation id = channel != null ? channel.id() : packet.payload().id();
            PayloadRegistration registration = PAYLOAD_REGISTRATIONS.get(listener.protocol()).get(id);
            registration.handle(packet.payload(), context);
        } else {
            LOGGER.error("Received a modded custom payload packet from a client that is not in the configuration or play phase. Disconnecting client.");
            throw new IllegalStateException("A client sent a packet while not in the configuration or play phase.");
        }
    }

    /**
     * Invoked on the client when a modded payload is received on a modded connection.
     * <p>
     * To reach this point, the packet has already been decoded and passed the checks enforced by {@link #getReader}, so we can omit the complex validation.
     *
     * @param listener The listener which received the packet.
     * @param packet   The packet that was received.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static boolean handleCustomPayload(ClientCommonPacketListener listener, ClientboundCustomPayloadPacket packet) {
        if (packet.payload().id().getNamespace().equals("minecraft")) {
            return false;
        }

        if (PAYLOAD_REGISTRATIONS.containsKey(listener.protocol())) {
            NetworkPayloadSetup payloadSetup = listener.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).get();
            ClientPayloadContext context = new ClientPayloadContext(listener, packet.payload().id());
            NetworkChannel channel = payloadSetup.channels().get(listener.protocol()).get(packet.payload().id());
            ResourceLocation id = channel != null ? channel.id() : packet.payload().id();
            PayloadRegistration registration = PAYLOAD_REGISTRATIONS.get(listener.protocol()).get(id);
            registration.handle(packet.payload(), context);
        } else {
            LOGGER.error("Received a modded custom payload packet from a server that is not in the configuration or play phase. Disconnecting server.");
            throw new IllegalStateException("A server sent a packet while not in the configuration or play phase.");
        }
        return true;
    }

    /**
     * Invoked by the server when it completes the negotiation with the client during the configuration phase.
     * <p>
     * This method determines what the versions of each of the channels are, and checks if the client and server have a compatible set of network channels.
     * <p>
     * If the negotiation fails, a custom packet is sent to the client to inform it of the failure, and which will allow the client to disconnect gracefully with an indicative error screen.
     * <p>
     * This method should only be invoked for modded connections.
     * Use {@link #onVanillaOrOtherConnectionDetectedAtServer} to indicate that during the configuration phase of the network handshake between a client and the server, a vanilla connection was detected.
     *
     * @param sender        The listener which completed the negotiation.
     * @param configuration The configuration channels that the client has available.
     * @param play          The play channels that the client has available.
     */
    public static void onModdedConnectionDetectedAtServer(ServerConfigurationPacketListener sender, Map<ConnectionProtocol, Set<ModdedNetworkQueryComponent>> clientChannels) {
        sender.getConnection().channel().attr(ATTRIBUTE_CONNECTION_TYPE).set(sender.getConnectionType());
        sender.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).set(NetworkPayloadSetup.empty());
        sender.getConnection().channel().attr(ATTRIBUTE_FLOW).set(PacketFlow.SERVERBOUND);

        Map<ConnectionProtocol, NegotiationResult> results = new IdentityHashMap<>();

        for (ConnectionProtocol protocol : PAYLOAD_REGISTRATIONS.keySet()) {
            final NegotiationResult negotiationResult = NetworkComponentNegotiator.negotiate(
                    PAYLOAD_REGISTRATIONS.get(protocol).entrySet().stream()
                            .map(entry -> new NegotiableNetworkComponent(entry.getKey(), entry.getValue().version(), entry.getValue().flow(), entry.getValue().optional()))
                            .toList(),
                    clientChannels.getOrDefault(protocol, Collections.emptySet()).stream()
                            .map(entry -> new NegotiableNetworkComponent(entry.id(), entry.version(), entry.flow(), entry.optional()))
                            .toList());

            // Negotiation failed. Disconnect the client.
            if (!negotiationResult.success()) {
                if (!negotiationResult.failureReasons().isEmpty()) {
                    sender.send(new ModdedNetworkSetupFailedPayload(negotiationResult.failureReasons()));
                }

                sender.disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s".formatted(NeoForgeVersion.getVersion())));
                return;
            }
            results.put(protocol, negotiationResult);
        }

        NetworkPayloadSetup setup = NetworkPayloadSetup.from(results);

        sender.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).set(setup);

        NetworkFilters.injectIfNecessary(sender.getConnection(), sender.getConnectionType());

        sender.send(new ModdedNetworkPayload(setup));
        ImmutableSet.Builder<ResourceLocation> nowListeningOn = ImmutableSet.builder();
        nowListeningOn.addAll(getInitialServerListeningChannels());
        nowListeningOn.addAll(setup.channels().get(ConnectionProtocol.CONFIGURATION).keySet());
        sender.send(new MinecraftRegisterPayload(nowListeningOn.build()));
    }

    /**
     * Invoked by the {@link ServerConfigurationPacketListenerImpl} when a vanilla or other connection is detected.
     *
     * @param sender The listener which detected the vanilla connection during the configuration phase.
     * @return True if the vanilla connection should be handled by the server, false otherwise.
     */
    public static boolean onVanillaOrOtherConnectionDetectedAtServer(ServerConfigurationPacketListener sender) {
        NetworkFilters.cleanIfNecessary(sender.getConnection());

        // Because we are in vanilla land, no matter what we are not able to support any custom channels.
        sender.getConnection().channel().attr(ATTRIBUTE_CONNECTION_TYPE).set(sender.getConnectionType());
        sender.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).set(NetworkPayloadSetup.empty());
        sender.getConnection().channel().attr(ATTRIBUTE_FLOW).set(PacketFlow.SERVERBOUND);

        for (ConnectionProtocol protocol : PAYLOAD_REGISTRATIONS.keySet()) {
            NegotiationResult negotiationResult = NetworkComponentNegotiator.negotiate(
                    PAYLOAD_REGISTRATIONS.get(protocol).entrySet().stream()
                            .map(entry -> new NegotiableNetworkComponent(entry.getKey(), entry.getValue().version(), entry.getValue().flow(), entry.getValue().optional()))
                            .toList(),
                    List.of());

            //Negotiation failed. Disconnect the client.
            if (!negotiationResult.success()) {
                sender.disconnect(Component.translatableWithFallback("neoforge.network.negotiation.failure.vanilla.client.not_supported",
                        "You are trying to connect to a server that is running NeoForge, but you are not. Please install NeoForge Version: %s to connect to this server.", NeoForgeVersion.getVersion()));
                return false;
            }
        }

        NetworkFilters.injectIfNecessary(sender.getConnection(), sender.getConnectionType());

        ImmutableSet.Builder<ResourceLocation> nowListeningOn = ImmutableSet.builder();
        nowListeningOn.addAll(getInitialClientListeningChannels());
        PAYLOAD_REGISTRATIONS.get(ConnectionProtocol.CONFIGURATION).entrySet().stream()
                .filter(registration -> registration.getValue().flow().isEmpty() || registration.getValue().flow().get() == PacketFlow.SERVERBOUND)
                .filter(registration -> registration.getValue().optional())
                .forEach(registration -> nowListeningOn.add(registration.getKey()));
        sender.send(new MinecraftRegisterPayload(nowListeningOn.build()));

        return true;
    }

    /**
     * Indicates if the given packet can be sent via the given listener.
     * <p>
     * This method is invoked by the vanilla code base to check if any packet can be sent to a client.
     * It will always return true for a packet that is not a {@link ClientboundCustomPayloadPacket}.
     * For a custom payload packet, it will check if the packet is known to the client, and if it is not, it will return false.
     * <p>
     * If this method is invoked before the negotiation during the configuration phase has completed, and as such no {@link NetworkPayloadSetup} is present then it
     * will only allow {@link ModdedNetworkQueryPayload} packets to be sent.
     *
     * @param packet   The packet that is about to be sent.
     * @param listener The listener that wants to send the packet.
     * @return True if the packet can be sent, false otherwise.
     */
    public static boolean canSendPacket(Packet<?> packet, ServerCommonPacketListener listener) {
        if (!(packet instanceof ClientboundCustomPayloadPacket customPayloadPacket)) {
            return true;
        }

        if (shouldSendPacketRaw(packet)) {
            return true;
        }

        if (hasChannel(listener, customPayloadPacket.payload().id())) {
            return true;
        }

        LOGGER.warn("Tried to send {} packet to a client that does not support it. Not sending the packet.", customPayloadPacket.payload().id());
        return false;
    }

    public static boolean shouldSendPacketRaw(Packet<?> packet) {
        if (!(packet instanceof ClientboundCustomPayloadPacket customPayloadPacket)) {
            return true;
        }

        ResourceLocation id = customPayloadPacket.payload().id();
        return BUILTIN_PAYLOADS.containsKey(id) || ClientboundCustomPayloadPacket.KNOWN_TYPES.containsKey(id);
    }

    /**
     * Indicates if the given packet can be sent via the given listener.
     * <p>
     * This method is invoked by the vanilla code base to check if any packet can be sent to a server.
     * It will always return true for a packet that is not a {@link ServerboundCustomPayloadPacket}.
     * For a custom payload packet, it will check if the packet is known to the server, and if it is not, it will return false.
     * <p>
     * If this method is invoked before the negotiation during the configuration phase has completed, and as
     * such no {@link NetworkPayloadSetup} is present then it will only allow {@link ModdedNetworkQueryPayload} packets to be sent.
     *
     * @param packet   The packet that is about to be sent.
     * @param listener The listener that wants to send the packet.
     * @return True if the packet can be sent, false otherwise.
     */
    public static boolean canSendPacket(Packet<?> packet, ClientCommonPacketListener listener) {
        if (!(packet instanceof ServerboundCustomPayloadPacket customPayloadPacket)) {
            return true;
        }

        ResourceLocation id = customPayloadPacket.payload().id();
        if (BUILTIN_PAYLOADS.containsKey(id) || ServerboundCustomPayloadPacket.KNOWN_TYPES.containsKey(id)) {
            return true;
        }

        if (hasChannel(listener, customPayloadPacket.payload().id())) {
            return true;
        }

        LOGGER.warn("Tried to send {} packet to a server that does not support it. Not sending the packet.", customPayloadPacket.payload().id());
        return false;
    }

    /**
     * Returns a mutable map of the currently known ad-hoc channels.
     */
    private static Set<ResourceLocation> getKnownAdHocChannelsOfOtherEnd(Connection connection) {
        var map = connection.channel().attr(ATTRIBUTE_ADHOC_CHANNELS).get();

        if (map == null) {
            map = new HashSet<>();
            connection.channel().attr(ATTRIBUTE_ADHOC_CHANNELS).set(map);
        }

        return map;
    }

    /**
     * Indicates if the given packet is ad-hoc readable for us.
     *
     * @param id   The id of the packet.
     * @param flow The flow of the packet.
     * @return True if the packet is ad-hoc readable, false otherwise.
     */
    private static boolean isAdhocChannelReadable(ConnectionProtocol protocol, ResourceLocation id, PacketFlow flow) {
        PayloadRegistration<?> known = PAYLOAD_REGISTRATIONS.getOrDefault(protocol, Collections.emptyMap()).get(id);
        if (known == null) {
            return false;
        }

        if (!known.optional()) {
            return false;
        }

        return known.flow().isEmpty() || known.flow().get() == flow;
    }

    /**
     * Invoked by the client when a modded server queries it for its available channels. The negotiation happens solely on the server side, and the result is later transmitted to the client.
     *
     * @param listener The listener which received the query.
     */
    public static void onNetworkQuery(ClientConfigurationPacketListener listener) {
        listener.send(ModdedNetworkQueryPayload.fromRegistry(PAYLOAD_REGISTRATIONS));
    }

    /**
     * Invoked by the client to indicate that it detect a connection to a modded server, by receiving a {@link ModdedNetworkPayload}.
     * This will configure the active connection to the server to use the channels that were negotiated.
     * <p>
     * Once this method completes a {@link NetworkPayloadSetup} will be present on the connection.
     *
     * @param listener      The listener which received the payload.
     * @param configuration The configuration channels that were negotiated.
     * @param play          The play channels that were negotiated.
     */
    public static void onModdedNetworkConnectionEstablished(ClientConfigurationPacketListener listener, NetworkPayloadSetup setup) {
        NetworkFilters.injectIfNecessary(listener.getConnection(), listener.getConnectionType());

        listener.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).set(setup);
        listener.getConnection().channel().attr(ATTRIBUTE_CONNECTION_TYPE).set(listener.getConnectionType());
        listener.getConnection().channel().attr(ATTRIBUTE_FLOW).set(PacketFlow.CLIENTBOUND);

        final ImmutableSet.Builder<ResourceLocation> nowListeningOn = ImmutableSet.builder();
        nowListeningOn.addAll(getInitialClientListeningChannels());
        nowListeningOn.addAll(setup.channels().get(ConnectionProtocol.CONFIGURATION).keySet());
        listener.send(new MinecraftRegisterPayload(nowListeningOn.build()));
    }

    /**
     * Invoked by the client when no {@link ModdedNetworkQueryPayload} has been received, but instead a {@link BrandPayload} has been received as the first packet during negotiation in the configuration phase.
     * <p>
     * If this happens then the client will do a negotiation of its own internal channel configuration, to check if any mods are installed that require a modded connection to the server.
     * If those are found then the connection is aborted and the client disconnects from the server.
     * <p>
     * This method should never be invoked on a connection where the server is modded.
     *
     * @param sender The listener which received the brand payload.
     * @return True if the vanilla connection should be handled by the client, false otherwise.
     */
    public static boolean onVanillaNetworkConnectionEstablished(ClientConfigurationPacketListener sender) {
        NetworkFilters.cleanIfNecessary(sender.getConnection());

        //Because we are in vanilla land, no matter what we are not able to support any custom channels.
        sender.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).set(NetworkPayloadSetup.empty());
        sender.getConnection().channel().attr(ATTRIBUTE_CONNECTION_TYPE).set(sender.getConnectionType());
        sender.getConnection().channel().attr(ATTRIBUTE_FLOW).set(PacketFlow.CLIENTBOUND);

        for (ConnectionProtocol protocol : PAYLOAD_REGISTRATIONS.keySet()) {
            NegotiationResult negotiationResult = NetworkComponentNegotiator.negotiate(
                    List.of(),
                    PAYLOAD_REGISTRATIONS.get(protocol).entrySet().stream()
                            .map(entry -> new NegotiableNetworkComponent(entry.getKey(), entry.getValue().version(), entry.getValue().flow(), entry.getValue().optional()))
                            .toList());

            //Negotiation failed. Disconnect the client.
            if (!negotiationResult.success()) {
                sender.getConnection().disconnect(Component.translatableWithFallback("neoforge.network.negotiation.failure.vanilla.server.not_supported",
                        "You are trying to connect to a server that is not running NeoForge, but you have mods that require it. A connection could not be established.", NeoForgeVersion.getVersion()));
                return false;
            }
        }

        //We are on the client, connected to a vanilla server, We have to load the default configs.
        ConfigTracker.INSTANCE.loadDefaultServerConfigs();

        NetworkFilters.injectIfNecessary(sender.getConnection(), sender.getConnectionType());

        ImmutableSet.Builder<ResourceLocation> nowListeningOn = ImmutableSet.builder();
        nowListeningOn.addAll(getInitialClientListeningChannels());
        PAYLOAD_REGISTRATIONS.get(ConnectionProtocol.CONFIGURATION).entrySet().stream()
                .filter(registration -> registration.getValue().flow().isEmpty() || registration.getValue().flow().get() == PacketFlow.CLIENTBOUND)
                .filter(registration -> registration.getValue().optional())
                .forEach(registration -> nowListeningOn.add(registration.getKey()));
        sender.send(new MinecraftRegisterPayload(nowListeningOn.build()));

        return true;
    }

    /**
     * Indicates whether the server listener has a connection setup that can transmit the given payload id.
     *
     * @param listener  The listener to check.
     * @param payloadId The payload id to check.
     * @return True if the listener has a connection setup that can transmit the given payload id, false otherwise.
     */
    public static boolean hasChannel(ServerCommonPacketListener listener, ResourceLocation payloadId) {
        return hasChannel(listener.getConnection(), listener.protocol(), payloadId);
    }

    /**
     * Indicates whether the client listener has a connection setup that can transmit the given payload id.
     *
     * @param listener  The listener to check.
     * @param payloadId The payload id to check.
     * @return True if the listener has a connection setup that can transmit the given payload id, false otherwise.
     */
    public static boolean hasChannel(ClientCommonPacketListener listener, ResourceLocation payloadId) {
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
    public static boolean hasChannel(final Connection connection, @Nullable ConnectionProtocol protocol, ResourceLocation payloadId) {
        final NetworkPayloadSetup payloadSetup = connection.channel().attr(ATTRIBUTE_PAYLOAD_SETUP).get();
        if (payloadSetup == null) {
            return getKnownAdHocChannelsOfOtherEnd(connection).contains(payloadId);
        }

        // If a protocol is specified, only check against channels for that protocol
        // Otherwise check against all protocols.
        if (protocol != null && payloadSetup.channels().get(protocol).containsKey(payloadId)) {
            return true;
        } else if (protocol == null && payloadSetup.channels().values().stream().anyMatch(map -> map.containsKey(payloadId))) {
            return true;
        }

        return getKnownAdHocChannelsOfOtherEnd(connection).contains(payloadId);
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
        final NetworkPayloadSetup payloadSetup = context.channel().attr(ATTRIBUTE_PAYLOAD_SETUP).get();
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

            if (shouldSendPacketRaw(packet)) {
                toSend.add(packet);
                return;
            }

            final NetworkChannel channel = payloadSetup.channels().get(ConnectionProtocol.PLAY).get(customPayloadPacket.payload().id());

            if (channel == null) {
                LOGGER.trace("Somebody tried to send: {} to a client which cannot accept it. Not sending packet.", customPayloadPacket.payload().id());
                return;
            }

            toSend.add(packet);
        });

        return toSend;
    }

    /**
     * Configures a mock connection.
     *
     * @param connection The connection to configure.
     */
    public static void configureMockConnection(final Connection connection) {
        connection.channel().attr(ATTRIBUTE_CONNECTION_TYPE).set(ConnectionType.NEOFORGE);
        connection.channel().attr(ATTRIBUTE_FLOW).set(PacketFlow.SERVERBOUND);
        connection.channel().attr(ATTRIBUTE_PAYLOAD_SETUP).set(NetworkPayloadSetup.empty());

        NetworkPayloadSetup setup = new NetworkPayloadSetup(
                PAYLOAD_REGISTRATIONS.entrySet().stream()
                        .map(entry -> Map.entry(entry.getKey(),
                                entry.getValue().values().stream().map(reg -> new NetworkChannel(reg.id(), reg.version())).collect(Collectors.toMap(NetworkChannel::id, Function.identity()))))
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

        connection.channel().attr(ATTRIBUTE_PAYLOAD_SETUP).set(setup);

        NetworkFilters.injectIfNecessary(connection, ConnectionType.NEOFORGE);
    }

    /**
     * Invoked to add to the known ad-hoc channels on a connection.
     *
     * @param resourceLocations The resource locations to add.
     * @param connection        The connection to add the channels to.
     */
    public static void onMinecraftRegister(Set<ResourceLocation> resourceLocations, Connection connection) {
        getKnownAdHocChannelsOfOtherEnd(connection).addAll(resourceLocations);
    }

    /**
     * Invoked to remove from the known ad-hoc channels on a connection.
     *
     * @param resourceLocations The resource locations to remove.
     * @param connection        The connection to remove the channels from.
     */
    public static void onMinecraftUnregister(Set<ResourceLocation> resourceLocations, Connection connection) {
        getKnownAdHocChannelsOfOtherEnd(connection).removeAll(resourceLocations);
    }

    /**
     * {@return the initial channels that the server listens on during the configuration phase.}
     */
    public static Set<ResourceLocation> getInitialServerListeningChannels() {
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

    private static Set<ResourceLocation> getInitialClientListeningChannels() {
        return BUILTIN_PAYLOADS.keySet();
    }

    public static void onConfigurationFinished(ServerConfigurationPacketListener serverConfigurationPacketListener) {
        final NetworkPayloadSetup setup = serverConfigurationPacketListener.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).get();
        if (setup == null) {
            LOGGER.error("Somebody tried to finish the configuration phase of a connection that has not negotiated with the client. Not finishing configuration.");
            return;
        }

        final ImmutableSet.Builder<ResourceLocation> notListeningAnymoreOn = ImmutableSet.builder();
        notListeningAnymoreOn.addAll(getInitialServerListeningChannels());
        notListeningAnymoreOn.addAll(setup.channels().get(ConnectionProtocol.CONFIGURATION).keySet());
        serverConfigurationPacketListener.send(new MinecraftUnregisterPayload(notListeningAnymoreOn.build()));

        final ImmutableSet.Builder<ResourceLocation> nowListeningOn = ImmutableSet.builder();
        nowListeningOn.add(MinecraftRegisterPayload.ID);
        nowListeningOn.add(MinecraftUnregisterPayload.ID);
        if (serverConfigurationPacketListener.getConnectionType().isNotVanilla()) {
            nowListeningOn.add(ModdedNetworkQueryPayload.ID);
        } else {
            PAYLOAD_REGISTRATIONS.get(ConnectionProtocol.PLAY).entrySet().stream()
                    .filter(registration -> registration.getValue().flow().isEmpty() || registration.getValue().flow().get() == PacketFlow.SERVERBOUND)
                    .filter(registration -> registration.getValue().optional())
                    .forEach(registration -> nowListeningOn.add(registration.getKey()));
        }
        serverConfigurationPacketListener.send(new MinecraftRegisterPayload(nowListeningOn.build()));
    }

    public static void onConfigurationFinished(ClientConfigurationPacketListener listener) {
        final NetworkPayloadSetup setup = listener.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).get();
        if (setup == null) {
            LOGGER.error("Somebody tried to finish the configuration phase of a connection that has not negotiated with the server. Not finishing configuration.");
            return;
        }

        final ImmutableSet.Builder<ResourceLocation> notListeningAnymoreOn = ImmutableSet.builder();
        notListeningAnymoreOn.addAll(getInitialClientListeningChannels());
        notListeningAnymoreOn.addAll(setup.channels().get(ConnectionProtocol.CONFIGURATION).keySet());
        listener.send(new MinecraftUnregisterPayload(notListeningAnymoreOn.build()));

        final ImmutableSet.Builder<ResourceLocation> nowListeningOn = ImmutableSet.builder();
        nowListeningOn.add(MinecraftRegisterPayload.ID);
        nowListeningOn.add(MinecraftUnregisterPayload.ID);
        if (listener.getConnectionType().isNotVanilla()) {
            nowListeningOn.add(ModdedNetworkQueryPayload.ID);
        } else {
            PAYLOAD_REGISTRATIONS.get(ConnectionProtocol.PLAY).entrySet().stream()
                    .filter(registration -> registration.getValue().flow().isEmpty() || registration.getValue().flow().get() == PacketFlow.CLIENTBOUND)
                    .filter(registration -> registration.getValue().optional())
                    .forEach(registration -> nowListeningOn.add(registration.getKey()));
        }
        listener.send(new MinecraftRegisterPayload(nowListeningOn.build()));
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
}
