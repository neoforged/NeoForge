/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.network.registration;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.client.network.handling.ClientPayloadContext;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.configuration.CheckExtensibleEnums;
import net.neoforged.neoforge.network.configuration.CheckFeatureFlags;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.filters.NetworkFilters;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.handling.MainThreadPayloadHandler;
import net.neoforged.neoforge.network.negotiation.NegotiableNetworkComponent;
import net.neoforged.neoforge.network.negotiation.NegotiationResult;
import net.neoforged.neoforge.network.negotiation.NetworkComponentNegotiator;
import net.neoforged.neoforge.network.payload.MinecraftRegisterPayload;
import net.neoforged.neoforge.network.payload.ModdedNetworkPayload;
import net.neoforged.neoforge.network.payload.ModdedNetworkQueryPayload;
import net.neoforged.neoforge.network.registration.ChannelAttributes;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.NetworkChannel;
import net.neoforged.neoforge.network.registration.NetworkPayloadSetup;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.network.registration.PayloadRegistration;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

@ApiStatus.Internal
public final class ClientNetworkRegistry extends NetworkRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean setupClient = false;

    private ClientNetworkRegistry() {}

    /**
     * Sets up the client network registry by firing {@link RegisterClientPayloadHandlersEvent}, updating the payload
     * registrations for clientbound payloads in {@link #PAYLOAD_REGISTRATIONS}.
     */
    public static void setup() {
        if (!NetworkRegistry.setup) {
            throw new IllegalStateException("ClientNetworkRegistry cannot be set up before main NetworkRegistry");
        }
        if (setupClient) {
            throw new IllegalStateException("The client network registry can only be set up once.");
        }

        ModLoader.postEvent(new RegisterClientPayloadHandlersEvent());

        List<ResourceLocation> missingHandlers = PAYLOAD_REGISTRATIONS.values()
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .filter(reg -> {
                    if (!reg.matchesFlow(PacketFlow.CLIENTBOUND)) {
                        return false;
                    }

                    for (ConnectionProtocol protocol : reg.protocols()) {
                        if (!CLIENTBOUND_HANDLERS.get(protocol).containsKey(reg.id())) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(PayloadRegistration::id)
                .distinct()
                .toList();
        if (!missingHandlers.isEmpty()) {
            throw new IllegalStateException("Some clientbound payloads are missing client-side handlers: " + missingHandlers);
        }

        setupClient = true;
    }

    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, HandlerThread thread, IPayloadHandler<T> handler) {
        if (setupClient) {
            throw new UnsupportedOperationException("Cannot register client-side handler for payload " + type.id() + " after registration phase.");
        }

        if (thread == HandlerThread.MAIN) {
            handler = new MainThreadPayloadHandler<>(handler);
        }

        boolean found = false;
        for (var entry : PAYLOAD_REGISTRATIONS.entrySet()) {
            ConnectionProtocol protocol = entry.getKey();
            Map<ResourceLocation, PayloadRegistration<?>> registrations = entry.getValue();

            PayloadRegistration<T> registration = (PayloadRegistration<T>) registrations.get(type.id());
            if (registration == null) {
                continue;
            }

            if (!registration.matchesFlow(PacketFlow.CLIENTBOUND)) {
                throw new IllegalArgumentException("Cannot register client handler for serverbound payload " + type);
            }

            found = true;

            registerHandler(CLIENTBOUND_HANDLERS, protocol, PacketFlow.CLIENTBOUND, type, handler);
        }
        if (!found) {
            throw new IllegalArgumentException("Cannot register client handler for unknown payload type " + type);
        }
    }

    /**
     * Handles modded payloads on the client. Invoked after built-in handling.
     * <p>
     * Called on the network thread.
     *
     * @param listener The listener which received the packet.
     * @param packet   The packet that was received.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void handleModdedPayload(ClientCommonPacketListener listener, ClientboundCustomPayloadPacket packet) {
        NetworkPayloadSetup payloadSetup = ChannelAttributes.getPayloadSetup(listener.getConnection());
        // Check if channels were negotiated.
        if (payloadSetup == null) {
            LOGGER.warn("Received a modded payload before channel negotiation; disconnecting.");
            listener.getConnection().disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s (No Payload Setup)".formatted(NeoForgeVersion.getVersion())));
            return;
        }

        ResourceLocation payloadId = packet.payload().type().id();
        ClientPayloadContext context = new ClientPayloadContext(listener, payloadId);

        if (CLIENTBOUND_HANDLERS.containsKey(listener.protocol())) {
            // Get the configuration channel for the packet.
            NetworkChannel channel = payloadSetup.getChannel(listener.protocol(), payloadId);

            // Check if the channel should even be processed.
            if (channel == null && !hasAdhocChannel(listener.protocol(), packet.payload().type().id(), PacketFlow.CLIENTBOUND)) {
                LOGGER.warn("Received a modded payload with an unknown or unaccepted channel; disconnecting.");
                listener.getConnection().disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s (No Channel for %s)".formatted(NeoForgeVersion.getVersion(), payloadId.toString())));
                return;
            }

            IPayloadHandler handler = CLIENTBOUND_HANDLERS.get(listener.protocol()).get(payloadId);
            if (handler == null) {
                LOGGER.error("Received a modded payload with no registration; disconnecting.");
                listener.getConnection().disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s (No Handler for %s)".formatted(NeoForgeVersion.getVersion(), payloadId.toString())));
                dumpStackToLog(); // This case is only likely when handling packets without serialization, i.e. from a compound listener, so this can help debug why.
                return;
            }

            handler.handle(packet.payload(), context);
        } else {
            LOGGER.error("Received a modded payload while not in the configuration or play phase. Disconnecting.");
            listener.getConnection().disconnect(Component.translatable("multiplayer.disconnect.incompatible", "NeoForge %s (Invalid Protocol %s)".formatted(NeoForgeVersion.getVersion(), listener.protocol().name())));
        }
    }

    /**
     * Invoked by the client when a modded server queries it for its available channels. The negotiation happens solely on the server side, and the result is later transmitted to the client.
     * <p>
     * Invoked on the network thread.
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
     * <p>
     * Invoked on the network thread.
     *
     * @param listener The listener which received the payload.
     * @param setup    The network channels that were negotiated.
     */
    public static void initializeNeoForgeConnection(ClientConfigurationPacketListener listener, NetworkPayloadSetup setup) {
        ChannelAttributes.setPayloadSetup(listener.getConnection(), setup);
        ChannelAttributes.setConnectionType(listener.getConnection(), listener.getConnectionType());

        // Only inject filters once the payload setup is stored, as the filters might check for available channels.
        NetworkFilters.injectIfNecessary(listener.getConnection());

        final ImmutableSet.Builder<ResourceLocation> nowListeningOn = ImmutableSet.builder();
        nowListeningOn.addAll(getInitialListeningChannels(listener.flow()));
        nowListeningOn.addAll(setup.getChannels(ConnectionProtocol.CONFIGURATION).keySet());
        listener.send(new MinecraftRegisterPayload(nowListeningOn.build()));
    }

    /**
     * Invoked by the client when no {@link ModdedNetworkQueryPayload} has been received, but instead a {@link BrandPayload} has been received as the first packet during negotiation in the configuration phase.
     * <p>
     * If this happens then the client will do a negotiation of its own internal channel configuration, to check if any mods are installed that require a modded connection to the server.
     * If those are found then the connection is aborted and the client disconnects from the server.
     * <p>
     * This method should never be invoked on a connection where the server is {@link ConnectionType#NEOFORGE}.
     * <p>
     * Invoked on the network thread.
     *
     * @param listener The listener which received the brand payload.
     */
    public static void initializeOtherConnection(ClientConfigurationPacketListener listener) {
        // Because we are in vanilla land, no matter what we are not able to support any custom channels.
        ChannelAttributes.setPayloadSetup(listener.getConnection(), NetworkPayloadSetup.empty());
        ChannelAttributes.setConnectionType(listener.getConnection(), listener.getConnectionType());

        for (ConnectionProtocol protocol : PAYLOAD_REGISTRATIONS.keySet()) {
            NegotiationResult negotiationResult = NetworkComponentNegotiator.negotiate(
                    List.of(),
                    PAYLOAD_REGISTRATIONS.get(protocol).entrySet().stream()
                            .map(entry -> new NegotiableNetworkComponent(entry.getKey(), entry.getValue().version(), entry.getValue().flow(), entry.getValue().optional()))
                            .toList());

            // Negotiation failed. Disconnect the client.
            if (!negotiationResult.success()) {
                listener.getConnection().disconnect(Component.translatableWithFallback("neoforge.network.negotiation.failure.vanilla.server.not_supported",
                        "You are trying to connect to a server that is not running NeoForge, but you have mods that require it. A connection could not be established.", NeoForgeVersion.getVersion()));
                return;
            }
        }

        // We are on the client, connected to a vanilla server, make sure we don't have any extended enums that may be sent to the server
        if (!CheckExtensibleEnums.handleVanillaServerConnection(listener)) {
            return;
        }
        // We are on the client, connected to a vanilla server, make sure we don't have any modded feature flags
        if (!CheckFeatureFlags.handleVanillaServerConnection(listener)) {
            return;
        }

        // We are on the client, connected to a vanilla server, We have to load the default configs.
        ConfigTracker.INSTANCE.loadDefaultServerConfigs();

        NetworkFilters.injectIfNecessary(listener.getConnection());

        ImmutableSet.Builder<ResourceLocation> nowListeningOn = ImmutableSet.builder();
        nowListeningOn.addAll(getInitialListeningChannels(listener.flow()));
        PAYLOAD_REGISTRATIONS.get(ConnectionProtocol.CONFIGURATION).entrySet().stream()
                .filter(registration -> registration.getValue().matchesFlow(listener.flow()))
                .filter(registration -> registration.getValue().optional())
                .forEach(registration -> nowListeningOn.add(registration.getKey()));
        listener.send(new MinecraftRegisterPayload(nowListeningOn.build()));
    }

    /**
     * Used in place of {@link Thread#dumpStack()} as that logs to {@link System#err}.
     */
    private static void dumpStackToLog() {
        LOGGER.error("", new Exception("Stack Trace"));
    }
}
