/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
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
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.connection.ConnectionUtils;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.filters.NetworkFilters;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.handling.IPacketHandler;
import net.neoforged.neoforge.network.handling.ISynchronizedWorkHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.negotiation.NegotiableNetworkComponent;
import net.neoforged.neoforge.network.negotiation.NegotiationResult;
import net.neoforged.neoforge.network.negotiation.NetworkComponentNegotiator;
import net.neoforged.neoforge.network.payload.ModdedNetworkComponent;
import net.neoforged.neoforge.network.payload.ModdedNetworkPayload;
import net.neoforged.neoforge.network.payload.ModdedNetworkQueryComponent;
import net.neoforged.neoforge.network.payload.ModdedNetworkQueryPayload;
import net.neoforged.neoforge.network.payload.ModdedNetworkSetupFailedPayload;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

/**
 * Defines the registry for all modded network packets.
 * <p>
 * This registry is responsible for storing all known modded network packets, and for handling the negotiation of modded network packets between the client and the server.
 * </p>
 * <p>
 * Additionally, this registry is responsible for handling all modded network packets that are not natively known once they arrive at the receiving end.
 * </p>
 * <p>
 * To prevent payloads from being send to a client that has no idea what to do with them, the registry provides endpoints for the vanilla code base to check if a packet can be send to a client.
 * </p>
 */
@ApiStatus.Internal
public class NetworkRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final AttributeKey<NetworkPayloadSetup> ATTRIBUTE_PAYLOAD_SETUP = AttributeKey.valueOf("neoforge:payload_setup");
    private static final AttributeKey<Boolean> ATTRIBUTE_IS_MODDED_CONNECTION = AttributeKey.valueOf("neoforge:is_modded_connection");
    private static final AttributeKey<PacketFlow> ATTRIBUTE_FLOW = AttributeKey.valueOf("neoforge:flow");

    private static final NetworkRegistry INSTANCE = new NetworkRegistry();

    public static NetworkRegistry getInstance() {
        return INSTANCE;
    }

    private boolean setup = false;
    private final Map<ResourceLocation, ConfigurationRegistration<?>> knownConfigurationRegistrations = new HashMap<>();
    private final Map<ResourceLocation, PlayRegistration<?>> knownPlayRegistrations = new HashMap<>();

    private NetworkRegistry() {}

    /**
     * Invoked to initially set up the registry.
     * <p>
     * This fires an event on the mod bus to allow mods to register their custom packets. And then stores the registered packets in the registry.
     * </p>
     * <p>
     * This method can only be invoked once.
     * </p>
     */
    public void setup() {
        if (setup)
            throw new IllegalStateException("The network registry can only be setup once.");

        setup = true;

        final Map<String, ModdedPacketRegistrar> registrarsByNamespace = Collections.synchronizedMap(new HashMap<>());
        ModLoader.get().postEvent(new RegisterPayloadHandlerEvent(namespace -> registrarsByNamespace.computeIfAbsent(namespace, ModdedPacketRegistrar::new)));
        registrarsByNamespace.values().forEach(ModdedPacketRegistrar::invalidate);

        final ImmutableMap.Builder<ResourceLocation, ConfigurationRegistration<?>> configurationBuilder = ImmutableMap.builder();
        registrarsByNamespace.values().forEach(registrar -> registrar.getConfigurationRegistrations().forEach(configurationBuilder::put));

        final ImmutableMap.Builder<ResourceLocation, PlayRegistration<?>> playBuilder = ImmutableMap.builder();
        registrarsByNamespace.values().forEach(registrar -> registrar.getPlayRegistrations().forEach(playBuilder::put));

        knownConfigurationRegistrations.clear();
        knownPlayRegistrations.clear();

        knownConfigurationRegistrations.putAll(configurationBuilder.build());
        knownPlayRegistrations.putAll(playBuilder.build());
    }

    /**
     * Invoked by the network subsystem to get a reader for a custom packet payload.
     * <p>
     * This method special cases three situations:
     * <ul>
     * <li>Vanilla custom packets, they are defined as "known packets" and if the payload id matches the known vanilla reader is returned</li>
     * <li>{@link ModdedNetworkQueryPayload}, it has a hardcoded id check, since it can be used before a network setup exists.</li>
     * <li>{@link ModdedNetworkPayload}, it also has a hardcoded id check, since it can be used before a network setup exists, as well.</li>
     * <li>{@link ModdedNetworkSetupFailedPayload}, it also has a hardcoded id check, since it can be used before a network setup exists, as well.</li>
     * </ul>
     * </p>
     * <p>
     * This method will then check if the connection is properly configured to be used for modded packets.
     * If not a warning is logged, and null is returned. Which causes the packet to be discarded.
     * </p>
     * <p>
     * If the connection is properly configured, the method will check if the packet is known to the connection, and if it is not, null is returned.
     * Then the method will check if the packet is known to the registry, and if it is not, null is returned.
     * If the packet is known to the registry, the method will return a reader that will invoke the registered replyHandler.
     * </p>
     *
     * @param id         The id of the payload.
     * @param context    The context of the channel.
     * @param protocol   The protocol of the connection.
     * @param knownTypes The known types of the connection.
     * @return A reader for the payload, or null if the payload should be discarded.
     */
    public FriendlyByteBuf.Reader<? extends CustomPacketPayload> getReader(ResourceLocation id, ChannelHandlerContext context, ConnectionProtocol protocol, Map<ResourceLocation, FriendlyByteBuf.Reader<? extends CustomPacketPayload>> knownTypes) {
        //Vanilla custom packet, let it deal with it.
        if (knownTypes.containsKey(id)) {
            return knownTypes.get(id);
        }

        //These are our own custom modded packets which can be sent before a payload setup is negotiated.
        //Special case them
        if (id.equals(ModdedNetworkQueryPayload.ID)) {
            return ModdedNetworkQueryPayload.READER;
        }

        if (id.equals(ModdedNetworkPayload.ID)) {
            return ModdedNetworkPayload.READER;
        }

        if (id.equals(ModdedNetworkSetupFailedPayload.ID)) {
            return ModdedNetworkSetupFailedPayload.READER;
        }

        //Check the network setup.
        final NetworkPayloadSetup payloadSetup = context.channel().attr(ATTRIBUTE_PAYLOAD_SETUP).get();
        final PacketFlow flow = context.channel().attr(ATTRIBUTE_FLOW).get();
        if (payloadSetup == null || flow == null) {
            //Error: Bail.
            LOGGER.warn("Received a modded custom payload packet {} that has not been negotiated with the server. Not parsing packet.", id);
            return null;
        }

        //Now ask the protocol what kind of payload is being sent and get the channel for it.
        if (protocol.isPlay()) {
            final NetworkChannel channel = payloadSetup.play().get(id);

            //Validate that everything is okey and then return a reader.
            if (channel == null) {
                LOGGER.warn("Received a modded custom payload packet {} with an unknown or not accepted channel. Not parsing packet.", id);
                return null;
            }

            final PlayRegistration<?> registration = knownPlayRegistrations.get(channel.id());
            if (registration == null) {
                LOGGER.error("Received a modded custom payload packet {} with an unknown or not accepted channel. Not parsing packet.", channel.id());
                throw new IllegalStateException("A client sent a packet with an unknown or not accepted channel, while negotiation succeeded. Somebody changed the channels known to NeoForge!");
            }

            if (registration.flow().isPresent()) {
                if (registration.flow().get() != flow) {
                    LOGGER.warn("Received a modded custom payload packet {} that is not supposed to be sent to the server. Disconnecting client.", channel.id());
                    final Connection connection = ConnectionUtils.getConnection(context);
                    final PacketListener listener = connection.getPacketListener();
                    if (listener instanceof ServerGamePacketListener serverListener) {
                        serverListener.disconnect(Component.translatable("neoforge.network.invalid_flow", flow));
                    } else if (listener instanceof ClientGamePacketListener clientListener) {
                        clientListener.getConnection().disconnect(Component.translatable("neoforge.network.invalid_flow", flow));
                    } else {
                        LOGGER.error("Received a modded custom payload packet {} that is not supposed to be sent to the server. Disconnecting client, but the listener is not a game listener. This should not happen.", channel.id());
                        throw new IllegalStateException("A client sent a packet with an unknown or not accepted channel, while negotiation succeeded. Somebody changed the channels known to NeoForge!");
                    }
                }
            }

            return registration;
        } else if (protocol.isConfiguration()) {
            final NetworkChannel channel = payloadSetup.configuration().get(id);

            //Also validate that everything is key and then return a reader.
            if (channel == null) {
                LOGGER.warn("Received a modded custom payload packet {} with an unknown or not accepted channel. Not parsing packet.", id);
                return null;
            }

            final ConfigurationRegistration<?> registration = knownConfigurationRegistrations.get(channel.id());
            if (registration == null) {
                LOGGER.error("Received a modded custom payload packet {} with an unknown or not accepted channel. Not parsing packet.", channel.id());
                throw new IllegalStateException("A client sent a packet with an unknown or not accepted channel, while negotiation succeeded. Somebody changed the channels known to NeoForge!");
            }

            if (registration.flow().isPresent()) {
                if (registration.flow().get() != flow) {
                    LOGGER.warn("Received a modded custom payload packet {} that is not supposed to be sent to the server. Disconnecting client.", channel.id());
                    final Connection connection = ConnectionUtils.getConnection(context);
                    final PacketListener listener = connection.getPacketListener();
                    if (listener instanceof ServerGamePacketListener serverListener) {
                        serverListener.disconnect(Component.translatable("neoforge.network.invalid_flow", flow));
                    } else if (listener instanceof ClientGamePacketListener clientListener) {
                        clientListener.getConnection().disconnect(Component.translatable("neoforge.network.invalid_flow", flow));
                    } else {
                        LOGGER.error("Received a modded custom payload packet {} that is not supposed to be sent to the server. Disconnecting client, but the listener is not a game listener. This should not happen.", channel.id());
                        throw new IllegalStateException("A client sent a packet with an unknown or not accepted channel, while negotiation succeeded. Somebody changed the channels known to NeoForge!");
                    }
                }
            }

            return registration;
        } else {
            //Error case, somebody is trying to sent a custom packet during a phase that is not supported.
            LOGGER.error("Received a modded custom payload packet from a client that is not in the configuration or play phase. Not parsing packet.");
            throw new IllegalStateException("A client sent a packet while not in the configuration or play phase. Somebody changed the phases known to NeoForge!");
        }
    }

    /**
     * Invoked by a {@link ServerCommonPacketListener} when a modded packet is received on a modded connection that is not natively known to the vanilla code base.
     * <p>
     * The method will first validate that a proper modded connection is setup and that a {@link NetworkPayloadSetup} is present on the connection. If that is not the case a warning is logged, and the client is disconnected with a generic error message.
     * </p>
     * <p>
     * If the connection is setup properly, the method will check if the packet is known to the connection, and if it is not, the client is disconnected. Then checks are executed against the stored known packet handlers to see if the packet is known to the server. Technically, this list is considered fixed, based on the fact that the registration event is only fired once during bootstrap, so in practice this is just a safe-guard against people messing with the registration map. Once that completes the registered replyHandler is invoked.
     * </p>
     *
     * @param listener The listener which received the packet.
     * @param packet   The packet that was received.
     */
    public void onModdedPacketAtServer(ServerCommonPacketListener listener, ServerboundCustomPayloadPacket packet) {
        final NetworkPayloadSetup payloadSetup = listener.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).get();
        //Check if this client was even setup properly.
        if (payloadSetup == null) {
            LOGGER.warn("Received a modded custom payload packet from a client that has not negotiated with the server. Disconnecting client.");
            listener.disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s".formatted(NeoForgeVersion.getVersion())));
            return;
        }

        if (listener instanceof ServerConfigurationPacketListener configurationPacketListener) {
            //Get the configuration channel for the packet.
            final NetworkChannel channel = payloadSetup.configuration().get(packet.payload().id());

            //Check if the channel should even be processed.
            if (channel == null) {
                LOGGER.warn("Received a modded custom payload packet from a client with an unknown or not accepted channel. Disconnecting client.");
                listener.disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s".formatted(NeoForgeVersion.getVersion())));
                return;
            }

            //We are in the configuration phase, so lookup packet listeners for that
            final ConfigurationRegistration<?> registration = knownConfigurationRegistrations.get(channel.id());
            if (registration == null) {
                LOGGER.error("Received a modded custom payload packet from a client with an unknown or not accepted channel. Disconnecting client.");
                throw new IllegalStateException("A client sent a packet with an unknown or not accepted channel, while negotiation succeeded. Somebody changed the channels known to NeoForge!");
            }

            registration.handle(
                    packet.payload(),
                    new ConfigurationPayloadContext(
                            configurationPacketListener::send,
                            new ServerPacketHandler(configurationPacketListener),
                            configurationPacketListener::finishCurrentTask,
                            new EventLoopSynchronizedWorkHandler<>(configurationPacketListener.getMainThreadEventLoop(), packet.payload()),
                            PacketFlow.SERVERBOUND,
                            listener.getConnection().channel().pipeline().lastContext(),
                            Optional.empty()));
        } else if (listener instanceof ServerGamePacketListener playPacketListener) {
            //Get the configuration channel for the packet.
            final NetworkChannel channel = payloadSetup.play().get(packet.payload().id());

            //Check if the channel should even be processed.
            if (channel == null) {
                LOGGER.warn("Received a modded custom payload packet from a client with an unknown or not accepted channel. Disconnecting client.");
                listener.disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s".formatted(NeoForgeVersion.getVersion())));
                return;
            }

            //We are in the play phase, so lookup packet listeners for that
            final PlayRegistration<?> registration = knownPlayRegistrations.get(channel.id());
            if (registration == null) {
                LOGGER.error("Received a modded custom payload packet from a client with an unknown or not accepted channel. Disconnecting client.");
                throw new IllegalStateException("A client sent a packet with an unknown or not accepted channel, while negotiation succeeded. Somebody changed the channels known to NeoForge!");
            }

            registration.handle(
                    packet.payload(),
                    new PlayPayloadContext(
                            playPacketListener::send,
                            new ServerPacketHandler(playPacketListener),
                            new EventLoopSynchronizedWorkHandler<>(playPacketListener.getMainThreadEventLoop(), packet.payload()),
                            PacketFlow.SERVERBOUND,
                            listener.getConnection().channel().pipeline().lastContext(),
                            listener instanceof ServerPlayerConnection connection ? Optional.of(connection.getPlayer()) : Optional.empty()));
        } else {
            LOGGER.error("Received a modded custom payload packet from a client that is not in the configuration or play phase. Disconnecting client.");
            throw new IllegalStateException("A client sent a packet while not in the configuration or play phase. Somebody changed the phases known to NeoForge!");
        }
    }

    /**
     * Invoked by a {@link ClientCommonPacketListener} when a modded packet is received on a modded connection that is not natively known to the vanilla code base.
     * <p>
     * The method will first validate that a proper modded connection is setup and that a {@link NetworkPayloadSetup} is present on the connection. If that is not the case a warning is logged, and the client is disconnected with a generic error message.
     * </p>
     * <p>
     * If the connection is setup properly, the method will check if the packet is known to the connection, and if it is not, the client is disconnected. Then checks are executed against the stored known packet handlers to see if the packet is known to the client. Technically, this list is considered fixed, based on the fact that the registration event is only fired once during bootstrap, so in practice this is just a safe-guard against people messing with the registration map. Once that completes the registered replyHandler is invoked.
     * </p>
     *
     * @param listener The listener which received the packet.
     * @param packet   The packet that was received.
     */
    public boolean onModdedPacketAtClient(ClientCommonPacketListener listener, ClientboundCustomPayloadPacket packet) {
        if (packet.payload().id().getNamespace().equals("minecraft")) {
            return false;
        }

        final NetworkPayloadSetup payloadSetup = listener.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).get();
        //Check if this server was even setup properly.
        if (payloadSetup == null) {
            LOGGER.warn("Received a modded custom payload packet from a server that has not negotiated with the client. Disconnecting server.");
            listener.getConnection().disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s".formatted(NeoForgeVersion.getVersion())));
            return false;
        }

        if (listener instanceof ClientConfigurationPacketListener configurationPacketListener) {
            //Get the configuration channel for the packet.
            final NetworkChannel channel = payloadSetup.configuration().get(packet.payload().id());

            //Check if the channel should even be processed.
            if (channel == null) {
                LOGGER.warn("Received a modded custom payload packet from a server with an unknown or not accepted channel. Disconnecting server.");
                listener.getConnection().disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s".formatted(NeoForgeVersion.getVersion())));
                return false;
            }

            //We are in the configuration phase, so lookup packet listeners for that
            final ConfigurationRegistration<?> registration = knownConfigurationRegistrations.get(channel.id());
            if (registration == null) {
                LOGGER.error("Received a modded custom payload packet from a server with an unknown or not accepted channel. Disconnecting server.");
                throw new IllegalStateException("A server sent a packet with an unknown or not accepted channel, while negotiation succeeded. Somebody changed the channels known to NeoForge!");
            }

            registration.handle(
                    packet.payload(),
                    new ConfigurationPayloadContext(
                            configurationPacketListener::send,
                            new ClientPacketHandler(configurationPacketListener),
                            (task) -> LOGGER.warn("Tried to finish a task on the client. This should not happen. Ignoring. Task: {}", task),
                            new EventLoopSynchronizedWorkHandler<>(configurationPacketListener.getMainThreadEventLoop(), packet.payload()),
                            PacketFlow.CLIENTBOUND,
                            listener.getConnection().channel().pipeline().lastContext(),
                            Optional.ofNullable(configurationPacketListener.getMinecraft().player)));
        } else if (listener instanceof ClientGamePacketListener playPacketListener) {
            //Get the configuration channel for the packet.
            final NetworkChannel channel = payloadSetup.play().get(packet.payload().id());

            //Check if the channel should even be processed.
            if (channel == null) {
                LOGGER.warn("Received a modded custom payload packet from a server with an unknown or not accepted channel. Disconnecting server.");
                listener.getConnection().disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s".formatted(NeoForgeVersion.getVersion())));
                return false;
            }

            //We are in the play phase, so lookup packet listeners for that
            final PlayRegistration<?> registration = knownPlayRegistrations.get(channel.id());
            if (registration == null) {
                LOGGER.error("Received a modded custom payload packet from a server with an unknown or not accepted channel. Disconnecting server.");
                throw new IllegalStateException("A server sent a packet with an unknown or not accepted channel, while negotiation succeeded. Somebody changed the channels known to NeoForge!");
            }

            registration.handle(
                    packet.payload(),
                    new PlayPayloadContext(
                            playPacketListener::send,
                            new ClientPacketHandler(playPacketListener),
                            new EventLoopSynchronizedWorkHandler<>(playPacketListener.getMainThreadEventLoop(), packet.payload()),
                            PacketFlow.CLIENTBOUND,
                            listener.getConnection().channel().pipeline().lastContext(),
                            Optional.ofNullable(playPacketListener.getMinecraft().player)));
        } else {
            LOGGER.error("Received a modded custom payload packet from a server that is not in the configuration or play phase. Disconnecting server.");
            throw new IllegalStateException("A server sent a packet while not in the configuration or play phase. Somebody changed the phases known to NeoForge!");
        }
        return true;
    }

    /**
     * Invoked by the server when it completes the negotiation with the client during the configuration phase.
     * <p>
     * This method determines what the versions of each of the channels are, and checks if the client and server have a compatible set of network channels.
     * </p>
     * <p>
     * If the negotiation fails, a custom packet is send to the client to inform it of the failure, and which will allow the client to disconnect gracefully with an indicative error screen.
     * </p>
     * <p>
     * This method should only be invoked for modded connections. Use {@link #onVanillaConnectionDetectedAtServer(ServerConfigurationPacketListener)} to indicate that during the configuration phase of the network handshake between a client and the server, a vanilla connection was detected.
     * </p>
     *
     * @param sender        The listener which completed the negotiation.
     * @param configuration The configuration channels that the client has available.
     * @param play          The play channels that the client has available.
     */
    public void onModdedConnectionDetectedAtServer(ServerConfigurationPacketListener sender, Set<ModdedNetworkQueryComponent> configuration, Set<ModdedNetworkQueryComponent> play) {
        final NegotiationResult configurationNegotiationResult = NetworkComponentNegotiator.negotiate(
                knownConfigurationRegistrations.entrySet().stream()
                        .map(entry -> new NegotiableNetworkComponent(entry.getKey(), entry.getValue().version(), entry.getValue().flow(), entry.getValue().optional()))
                        .toList(),
                configuration.stream()
                        .map(entry -> new NegotiableNetworkComponent(entry.id(), entry.version(), entry.flow(), entry.optional()))
                        .toList());

        sender.getConnection().channel().attr(ATTRIBUTE_IS_MODDED_CONNECTION).set(true);
        sender.getConnection().channel().attr(ATTRIBUTE_FLOW).set(PacketFlow.SERVERBOUND);
        sender.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).set(NetworkPayloadSetup.emptyModded());

        //Negotiation failed. Disconnect the client.
        if (!configurationNegotiationResult.success()) {
            if (!configurationNegotiationResult.failureReasons().isEmpty()) {
                sender.send(new ModdedNetworkSetupFailedPayload(configurationNegotiationResult.failureReasons()));
            }

            sender.disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s".formatted(NeoForgeVersion.getVersion())));
            return;
        }

        final NegotiationResult playNegotiationResult = NetworkComponentNegotiator.negotiate(
                knownPlayRegistrations.entrySet().stream()
                        .map(entry -> new NegotiableNetworkComponent(entry.getKey(), entry.getValue().version(), entry.getValue().flow(), entry.getValue().optional()))
                        .toList(),
                play.stream()
                        .map(entry -> new NegotiableNetworkComponent(entry.id(), entry.version(), entry.flow(), entry.optional()))
                        .toList());

        //Negotiation failed. Disconnect the client.
        if (!playNegotiationResult.success()) {
            if (!playNegotiationResult.failureReasons().isEmpty()) {
                sender.send(new ModdedNetworkSetupFailedPayload(playNegotiationResult.failureReasons()));
            }

            sender.disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s".formatted(NeoForgeVersion.getVersion())));
        }

        final NetworkPayloadSetup setup = NetworkPayloadSetup.from(
                configurationNegotiationResult.components().stream()
                        .map(entry -> new NetworkChannel(entry.id(), entry.version()))
                        .collect(Collectors.toSet()),
                playNegotiationResult.components().stream()
                        .map(entry -> new NetworkChannel(entry.id(), entry.version()))
                        .collect(Collectors.toSet()));

        sender.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).set(setup);

        NetworkFilters.injectIfNecessary(sender.getConnection());

        sender.send(new ModdedNetworkPayload(
                setup.configuration().values().stream().map(channel -> new ModdedNetworkComponent(channel.id(), channel.chosenVersion())).collect(Collectors.toSet()),
                setup.play().values().stream().map(channel -> new ModdedNetworkComponent(channel.id(), channel.chosenVersion())).collect(Collectors.toSet())));
    }

    /**
     * Invoked by the {@link ServerConfigurationPacketListenerImpl} when a vanilla connection is detected.
     *
     * @param sender The listener which detected the vanilla connection during the configuration phase.
     * @return True if the vanilla connection should be handled by the server, false otherwise.
     */
    public boolean onVanillaConnectionDetectedAtServer(ServerConfigurationPacketListener sender) {
        NetworkFilters.cleanIfNecessary(sender.getConnection());

        final NegotiationResult configurationNegotiationResult = NetworkComponentNegotiator.negotiate(
                knownConfigurationRegistrations.entrySet().stream()
                        .map(entry -> new NegotiableNetworkComponent(entry.getKey(), entry.getValue().version(), entry.getValue().flow(), entry.getValue().optional()))
                        .toList(),
                List.of());

        //Because we are in vanilla land, no matter what we are not able to support any custom channels.
        sender.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).set(NetworkPayloadSetup.emptyVanilla());
        sender.getConnection().channel().attr(ATTRIBUTE_IS_MODDED_CONNECTION).set(false);
        sender.getConnection().channel().attr(ATTRIBUTE_FLOW).set(PacketFlow.SERVERBOUND);

        //Negotiation failed. Disconnect the client.
        if (!configurationNegotiationResult.success()) {
            sender.disconnect(Component.translatable("neoforge.network.negotiation.failure.vanilla.client.not_supported", NeoForgeVersion.getVersion()));
            return false;
        }

        final NegotiationResult playNegotiationResult = NetworkComponentNegotiator.negotiate(
                knownPlayRegistrations.entrySet().stream()
                        .map(entry -> new NegotiableNetworkComponent(entry.getKey(), entry.getValue().version(), entry.getValue().flow(), entry.getValue().optional()))
                        .toList(),
                List.of());

        //Negotiation failed. Disconnect the client.
        if (!playNegotiationResult.success()) {
            sender.disconnect(Component.translatable("neoforge.network.negotiation.failure.vanilla.client.not_supported", NeoForgeVersion.getVersion()));
            return false;
        }

        NetworkFilters.injectIfNecessary(sender.getConnection());

        return true;
    }

    /**
     * Indicates if the given packet can be sent via the given listener.
     * <p>
     * This method is invoked by the vanilla code base to check if any packet can be sent to a client. It will always return true for a packet that is not a {@link ClientboundCustomPayloadPacket}. For a custom payload packet, it will check if the packet is known to the client, and if it is not, it will return false.
     * </p>
     * <p>
     * If this method is invoked before the negotiation during the configuration phase has completed, and as such no {@link NetworkPayloadSetup} is present then it will only allow {@link ModdedNetworkQueryPayload} packets to be sent.
     * </p>
     *
     * @param packet   The packet that is about to be sent.
     * @param listener The listener that wants to send the packet.
     * @return True if the packet can be sent, false otherwise.
     */
    public boolean canSendPacket(Packet<?> packet, ServerCommonPacketListener listener) {
        if (!(packet instanceof ClientboundCustomPayloadPacket customPayloadPacket)) {
            return true;
        }

        if (shouldSendPacketRaw(packet)) {
            return true;
        }

        return isConnected(listener, customPayloadPacket.payload().id());
    }

    public boolean shouldSendPacketRaw(Packet<?> packet) {
        if (!(packet instanceof ClientboundCustomPayloadPacket customPayloadPacket)) {
            return true;
        }

        if (customPayloadPacket.payload() instanceof ModdedNetworkQueryPayload) {
            return true;
        }

        if (customPayloadPacket.payload() instanceof ModdedNetworkSetupFailedPayload) {
            return true;
        }

        if (customPayloadPacket.payload() instanceof ModdedNetworkPayload) {
            return true;
        }

        //Vanilla payloads.
        return ClientboundCustomPayloadPacket.KNOWN_TYPES.containsKey(customPayloadPacket.payload().id());
    }

    /**
     * Indicates if the given packet can be sent via the given listener.
     * <p>
     * This method is invoked by the vanilla code base to check if any packet can be sent to a client. It will always return true for a packet that is not a {@link ServerboundCustomPayloadPacket}. For a custom payload packet, it will check if the packet is known to the server, and if it is not, it will return false.
     * </p>
     * <p>
     * If this method is invoked before the negotiation during the configuration phase has completed, and as such no {@link NetworkPayloadSetup} is present then it will only allow {@link ModdedNetworkQueryPayload} packets to be sent.
     * </p>
     *
     * @param packet   The packet that is about to be sent.
     * @param listener The listener that wants to send the packet.
     * @return True if the packet can be sent, false otherwise.
     */
    public boolean canSendPacket(Packet<?> packet, ClientCommonPacketListener listener) {
        if (!(packet instanceof ServerboundCustomPayloadPacket customPayloadPacket)) {
            return true;
        }

        if (customPayloadPacket.payload() instanceof ModdedNetworkQueryPayload) {
            return true;
        }

        //Vanilla payloads.
        if (ServerboundCustomPayloadPacket.KNOWN_TYPES.containsKey(customPayloadPacket.payload().id())) {
            return true;
        }

        return isConnected(listener, customPayloadPacket.payload().id());
    }

    /**
     * Invoked by the client when a modded server queries it for its available channels. The negotiation happens solely on the server side, and the result is later transmitted to the client.
     *
     * @param listener The listener which received the query.
     */
    public void onNetworkQuery(ClientConfigurationPacketListener listener) {
        final ModdedNetworkQueryPayload payload = new ModdedNetworkQueryPayload(
                knownConfigurationRegistrations.entrySet().stream()
                        .map(entry -> new ModdedNetworkQueryComponent(entry.getKey(), entry.getValue().version(), entry.getValue().flow(), entry.getValue().optional()))
                        .collect(Collectors.toSet()),
                knownPlayRegistrations.entrySet().stream()
                        .map(entry -> new ModdedNetworkQueryComponent(entry.getKey(), entry.getValue().version(), entry.getValue().flow(), entry.getValue().optional()))
                        .collect(Collectors.toSet()));

        listener.send(payload);
    }

    /**
     * Invoked by the client to indicate that it detect a connection to a modded server, by receiving a {@link ModdedNetworkPayload}. This will configure the active connection to the server to use the channels that were negotiated.
     * <p>
     * Once this method completes a {@link NetworkPayloadSetup} will be present on the connection.
     * </p>
     *
     * @param listener      The listener which received the payload.
     * @param configuration The configuration channels that were negotiated.
     * @param play          The play channels that were negotiated.
     */
    public void onModdedNetworkConnectionEstablished(ClientConfigurationPacketListener listener, Set<ModdedNetworkComponent> configuration, Set<ModdedNetworkComponent> play) {
        final NetworkPayloadSetup setup = NetworkPayloadSetup.from(
                configuration.stream()
                        .map(entry -> new NetworkChannel(entry.id(), entry.version()))
                        .collect(Collectors.toSet()),
                play.stream()
                        .map(entry -> new NetworkChannel(entry.id(), entry.version()))
                        .collect(Collectors.toSet()));

        NetworkFilters.injectIfNecessary(listener.getConnection());

        listener.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).set(setup);
        listener.getConnection().channel().attr(ATTRIBUTE_IS_MODDED_CONNECTION).set(true);
        listener.getConnection().channel().attr(ATTRIBUTE_FLOW).set(PacketFlow.CLIENTBOUND);
    }

    /**
     * Invoked by the client when no {@link ModdedNetworkQueryPayload} has been received, but instead a {@link BrandPayload} has been received as the first packet during negotiation in the configuration phase.
     * <p>
     * If this happens then the client will do a negotiation of its own internal channel configuration, to check if mods are installed which <span class="strong">need</span> a modded connection to the server. If those are found then the connection is aborted and the client disconnects from the server.
     * </p>
     * <p>
     * This method should never be invoked on a connection where the serverside is a modded environment.
     * </p>
     *
     * @param sender The listener which received the brand payload.
     * @return True if the vanilla connection should be handled by the client, false otherwise.
     */
    public boolean onVanillaNetworkConnectionEstablished(ClientConfigurationPacketListener sender) {
        NetworkFilters.cleanIfNecessary(sender.getConnection());

        final NegotiationResult configurationNegotiationResult = NetworkComponentNegotiator.negotiate(
                List.of(),
                knownConfigurationRegistrations.entrySet().stream()
                        .map(entry -> new NegotiableNetworkComponent(entry.getKey(), entry.getValue().version(), entry.getValue().flow(), entry.getValue().optional()))
                        .toList());

        //Because we are in vanilla land, no matter what we are not able to support any custom channels.
        sender.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).set(NetworkPayloadSetup.emptyVanilla());
        sender.getConnection().channel().attr(ATTRIBUTE_IS_MODDED_CONNECTION).set(false);
        sender.getConnection().channel().attr(ATTRIBUTE_FLOW).set(PacketFlow.CLIENTBOUND);

        //Negotiation failed. Disconnect the client.
        if (!configurationNegotiationResult.success()) {
            sender.getConnection().disconnect(Component.translatable("neoforge.network.negotiation.failure.vanilla.client.not_supported", NeoForgeVersion.getVersion()));
            return false;
        }

        final NegotiationResult playNegotiationResult = NetworkComponentNegotiator.negotiate(
                List.of(),
                knownPlayRegistrations.entrySet().stream()
                        .map(entry -> new NegotiableNetworkComponent(entry.getKey(), entry.getValue().version(), entry.getValue().flow(), entry.getValue().optional()))
                        .toList());

        //Negotiation failed. Disconnect the client.
        if (!playNegotiationResult.success()) {
            sender.getConnection().disconnect(Component.translatable("neoforge.network.negotiation.failure.vanilla.client.not_supported", NeoForgeVersion.getVersion()));
            return false;
        }

        //We are on the client, connected to a vanilla server, We have to load the default configs.
        ConfigTracker.INSTANCE.loadDefaultServerConfigs();

        NetworkFilters.injectIfNecessary(sender.getConnection());

        return true;
    }

    /**
     * Indicates if the given connection is a vanilla connection.
     *
     * @param connection The connection to check.
     * @return True if the connection is a vanilla connection, false otherwise.
     */
    public boolean isVanillaConnection(Connection connection) {
        return isVanillaConnection(connection.channel());
    }
    /**
     * Indicates if the given connection is a vanilla connection.
     *
     * @param channel The channel to check.
     * @return True if the connection is a vanilla connection, false otherwise.
     */
    public boolean isVanillaConnection(Channel channel) {
        return channel.attr(ATTRIBUTE_IS_MODDED_CONNECTION).get() == Boolean.FALSE;
    }

    /**
     * Indicates whether the server listener has a connection setup that can transmit the given payload id.
     *
     * @param listener  The listener to check.
     * @param payloadId The payload id to check.
     * @return True if the listener has a connection setup that can transmit the given payload id, false otherwise.
     */
    public boolean isConnected(ServerCommonPacketListener listener, ResourceLocation payloadId) {
        final NetworkPayloadSetup payloadSetup = listener.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).get();
        if (payloadSetup == null) {
            LOGGER.warn("Somebody tried to send: {} to a client that has not negotiated with the client. Not sending packet.", payloadId);
            return false;
        }

        if (listener instanceof ServerConfigurationPacketListener) {
            final NetworkChannel channel = payloadSetup.configuration().get(payloadId);

            if (channel == null) {
                LOGGER.trace("Somebody tried to send: {} to a client which cannot accept it. Not sending packet.", payloadId);
                return false;
            }

            return true;
        } else if (listener instanceof ServerGamePacketListener) {
            final NetworkChannel channel = payloadSetup.play().get(payloadId);

            if (channel == null) {
                LOGGER.trace("Somebody tried to send: {} to a client which cannot accept it. Not sending packet.", payloadId);
                return false;
            }

            return true;
        } else {
            LOGGER.error("Somebody tried to send: {} to a client that is not in the configuration or play phase. Not sending packet.", payloadId);
            throw new IllegalStateException("Somebody tried to send a packet while not in the configuration or play phase. Somebody changed the phases known to NeoForge!");
        }
    }

    /**
     * Indicates whether the client listener has a connection setup that can transmit the given payload id.
     *
     * @param listener  The listener to check.
     * @param payloadId The payload id to check.
     * @return True if the listener has a connection setup that can transmit the given payload id, false otherwise.
     */
    public boolean isConnected(ClientCommonPacketListener listener, ResourceLocation payloadId) {
        final NetworkPayloadSetup payloadSetup = listener.getConnection().channel().attr(ATTRIBUTE_PAYLOAD_SETUP).get();
        if (payloadSetup == null) {
            LOGGER.warn("Somebody tried to send: {} to a server that has not negotiated with the client. Not sending packet.", payloadId);
            return false;
        }

        if (listener instanceof ClientConfigurationPacketListener) {
            final NetworkChannel channel = payloadSetup.configuration().get(payloadId);

            if (channel == null) {
                LOGGER.trace("Somebody tried to send: {} to a server which cannot accept it. Not sending packet.", payloadId);
                return false;
            }

            return true;
        } else if (listener instanceof ClientGamePacketListener) {
            final NetworkChannel channel = payloadSetup.play().get(payloadId);

            if (channel == null) {
                LOGGER.trace("Somebody tried to send: {} to a server which cannot accept it. Not sending packet.", payloadId);
                return false;
            }

            return true;
        } else {
            LOGGER.error("Somebody tried to send: {} to a server that is not in the configuration or play phase. Not sending packet.", payloadId);
            throw new IllegalStateException("Somebody tried to send a packet while not in the configuration or play phase. Somebody changed the phases known to NeoForge!");
        }
    }

    /**
     * Filters the given packets for a bundle packet in the game phase of the connection.
     *
     * @param context The context of the connection.
     * @param packets The packets to filter.
     * @param <T>     The type of the listener.
     * @return The filtered packets.
     */
    public <T extends PacketListener> List<Packet<?>> filterGameBundlePackets(ChannelHandlerContext context, Iterable<Packet<? super T>> packets) {
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

            final NetworkChannel channel = payloadSetup.play().get(customPayloadPacket.payload().id());

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
    public void configureMockConnection(final Connection connection) {
        connection.channel().attr(ATTRIBUTE_IS_MODDED_CONNECTION).set(true);
        connection.channel().attr(ATTRIBUTE_FLOW).set(PacketFlow.SERVERBOUND);
        connection.channel().attr(ATTRIBUTE_PAYLOAD_SETUP).set(NetworkPayloadSetup.emptyModded());

        final NetworkPayloadSetup setup = NetworkPayloadSetup.from(
                this.knownConfigurationRegistrations.entrySet().stream()
                        .map(entry -> new NetworkChannel(entry.getKey(), entry.getValue().version()))
                        .collect(Collectors.toSet()),
                this.knownPlayRegistrations.entrySet().stream()
                        .map(entry -> new NetworkChannel(entry.getKey(), entry.getValue().version()))
                        .collect(Collectors.toSet()));

        connection.channel().attr(ATTRIBUTE_PAYLOAD_SETUP).set(setup);

        NetworkFilters.injectIfNecessary(connection);
    }

    /**
     * An {@link ISynchronizedWorkHandler} that can be used to schedule tasks on the main thread of the server or client. This wrapper record is used to line up the APIs of the {@link ReentrantBlockableEventLoop} with the {@link ISynchronizedWorkHandler}.
     *
     * @param eventLoop The event loop to schedule tasks on.
     */
    @SuppressWarnings("resource")
    private record EventLoopSynchronizedWorkHandler<T>(
            ReentrantBlockableEventLoop<?> eventLoop, T payload) implements ISynchronizedWorkHandler {
        private static final Logger LOGGER = LogUtils.getLogger();

        /**
         * {@inheritDoc}
         */
        @Override
        public void execute(Runnable task) {
            submitAsync(task).exceptionally(
                    ex -> {
                        LOGGER.error("Failed to process a synchronized task of the payload: %s".formatted(payload()), ex);
                        return null;
                    });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CompletableFuture<Void> submitAsync(Runnable task) {
            return eventLoop().submit(task);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <R> CompletableFuture<R> submitAsync(Supplier<R> task) {
            return eventLoop().submit(task);
        }
    }

    @SuppressWarnings("unchecked")
    private record ServerPacketHandler(ServerCommonPacketListener listener) implements IPacketHandler {
        @Override
        public void handle(Packet<?> packet) {
            resolvePacketGenerics(packet, listener());
        }

        @Override
        public void handle(CustomPacketPayload payload) {
            handle(new ServerboundCustomPayloadPacket(payload));
        }

        @Override
        public void disconnect(Component reason) {
            listener().disconnect(reason);
        }

        private static <T extends PacketListener> void resolvePacketGenerics(Packet<T> packet, ServerCommonPacketListener listener) {
            try {
                packet.handle((T) listener);
            } catch (ClassCastException exception) {
                throw new IllegalStateException("Somebody tried to handle a packet in a listener that does not support it.", exception);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private record ClientPacketHandler(ClientCommonPacketListener listener) implements IPacketHandler {
        @Override
        public void handle(Packet<?> packet) {
            resolvePacketGenerics(packet, listener());
        }

        @Override
        public void handle(CustomPacketPayload payload) {
            handle(new ClientboundCustomPayloadPacket(payload));
        }

        @Override
        public void disconnect(Component reason) {
            listener().getConnection().disconnect(reason);
        }

        private static <T extends PacketListener> void resolvePacketGenerics(Packet<T> packet, ClientCommonPacketListener listener) {
            try {
                packet.handle((T) listener);
            } catch (ClassCastException exception) {
                throw new IllegalStateException("Somebody tried to handle a packet in a listener that does not support it.", exception);
            }
        }
    }
}
