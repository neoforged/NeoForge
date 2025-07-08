/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.handling.MainThreadPayloadHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Builder-style helper for registering {@link CustomPacketPayload}s, used for modded networking.
 * 
 * @see Class-level documentation on {@link RegisterPayloadHandlersEvent} for baseline rules.
 * @see Method-level documentation on {@link RegisterPayloadHandlersEvent#register} for parameter information.
 */
public class PayloadRegistrar {
    private String version;
    private boolean optional = false;
    private HandlerThread thread = HandlerThread.MAIN;

    public PayloadRegistrar(String version) {
        this.version = version;
    }

    private PayloadRegistrar(PayloadRegistrar source) {
        this.version = source.version;
        this.optional = source.optional;
        this.thread = source.thread;
    }

    /**
     * Registers a client-bound payload for the play phase.
     */
    public <T extends CustomPacketPayload> PayloadRegistrar playToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        register(type, codec, null, handler, List.of(ConnectionProtocol.PLAY), Optional.of(PacketFlow.CLIENTBOUND), version, optional);
        return this;
    }

    /**
     * Registers a client-bound payload for the play phase without a handler. The missing handler must be registered via {@code RegisterClientPayloadHandlersEvent}.
     */
    public <T extends CustomPacketPayload> PayloadRegistrar playToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        register(type, codec, null, null, List.of(ConnectionProtocol.PLAY), Optional.of(PacketFlow.CLIENTBOUND), version, optional);
        return this;
    }

    /**
     * Registers a server-bound payload for the play phase.
     */
    public <T extends CustomPacketPayload> PayloadRegistrar playToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        register(type, codec, handler, null, List.of(ConnectionProtocol.PLAY), Optional.of(PacketFlow.SERVERBOUND), version, optional);
        return this;
    }

    /**
     * Registers a bidirectional payload for the play phase.
     * <p>
     * If the provided {@linkplain IPayloadHandler client handler} is null, it must be registered via {@code RegisterClientPayloadHandlersEvent}
     */
    public <T extends CustomPacketPayload> PayloadRegistrar playBidirectional(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> serverHandler, @Nullable IPayloadHandler<T> clientHandler) {
        register(type, codec, serverHandler, clientHandler, List.of(ConnectionProtocol.PLAY), Optional.empty(), version, optional);
        return this;
    }

    /**
     * Registers a bidirectional payload for the play phase.
     * <p>
     * The provided handler is registered for serverbound payloads and the client-side handler must be registered via {@code RegisterClientPayloadHandlersEvent}
     */
    public <T extends CustomPacketPayload> PayloadRegistrar playBidirectional(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> serverHandler) {
        return this.playBidirectional(type, codec, serverHandler, null);
    }

    /**
     * Registers a client-bound payload for the configuration phase.
     */
    public <T extends CustomPacketPayload> PayloadRegistrar configurationToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        register(type, codec, null, handler, List.of(ConnectionProtocol.CONFIGURATION), Optional.of(PacketFlow.CLIENTBOUND), version, optional);
        return this;
    }

    /**
     * Registers a client-bound payload for the configuration phase without a handler. The missing handler must be registered via {@code RegisterClientPayloadHandlersEvent}.
     */
    public <T extends CustomPacketPayload> PayloadRegistrar configurationToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec) {
        register(type, codec, null, null, List.of(ConnectionProtocol.CONFIGURATION), Optional.of(PacketFlow.CLIENTBOUND), version, optional);
        return this;
    }

    /**
     * Registers a server-bound payload for the configuration phase.
     */
    public <T extends CustomPacketPayload> PayloadRegistrar configurationToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        register(type, codec, handler, null, List.of(ConnectionProtocol.CONFIGURATION), Optional.of(PacketFlow.SERVERBOUND), version, optional);
        return this;
    }

    /**
     * Registers a bidirectional payload for the configuration phase.
     * <p>
     * If the provided {@linkplain IPayloadHandler client handler} is null, it must be registered via {@code RegisterClientPayloadHandlersEvent}
     */
    public <T extends CustomPacketPayload> PayloadRegistrar configurationBidirectional(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, IPayloadHandler<T> serverHandler, @Nullable IPayloadHandler<T> clientHandler) {
        register(type, codec, serverHandler, clientHandler, List.of(ConnectionProtocol.CONFIGURATION), Optional.empty(), version, optional);
        return this;
    }

    /**
     * Registers a bidirectional payload for the configuration phase.
     * <p>
     * The provided handler is registered for serverbound payloads and the client-side handler must be registered via {@code RegisterClientPayloadHandlersEvent}
     */
    public <T extends CustomPacketPayload> PayloadRegistrar configurationBidirectional(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, IPayloadHandler<T> serverHandler) {
        return this.configurationBidirectional(type, codec, serverHandler, null);
    }

    /**
     * Registers a client-bound payload for all phases.
     */
    public <T extends CustomPacketPayload> PayloadRegistrar commonToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        register(type, codec, null, handler, List.of(ConnectionProtocol.PLAY, ConnectionProtocol.CONFIGURATION), Optional.of(PacketFlow.CLIENTBOUND), version, optional);
        return this;
    }

    /**
     * Registers a client-bound payload for all phases without a handler. The missing handler must be registered via {@code RegisterClientPayloadHandlersEvent}.
     */
    public <T extends CustomPacketPayload> PayloadRegistrar commonToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec) {
        register(type, codec, null, null, List.of(ConnectionProtocol.PLAY, ConnectionProtocol.CONFIGURATION), Optional.of(PacketFlow.CLIENTBOUND), version, optional);
        return this;
    }

    /**
     * Registers a server-bound payload for all phases.
     */
    public <T extends CustomPacketPayload> PayloadRegistrar commonToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        register(type, codec, handler, null, List.of(ConnectionProtocol.PLAY, ConnectionProtocol.CONFIGURATION), Optional.of(PacketFlow.SERVERBOUND), version, optional);
        return this;
    }

    /**
     * Registers a bidirectional payload for all phases.
     * <p>
     * If the provided {@linkplain IPayloadHandler client handler} is null, it must be registered via {@code RegisterClientPayloadHandlersEvent}
     */
    public <T extends CustomPacketPayload> PayloadRegistrar commonBidirectional(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, IPayloadHandler<T> serverHandler, @Nullable IPayloadHandler<T> clientHandler) {
        register(type, codec, serverHandler, clientHandler, List.of(ConnectionProtocol.PLAY, ConnectionProtocol.CONFIGURATION), Optional.empty(), version, optional);
        return this;
    }

    /**
     * Registers a bidirectional payload for all phases.
     * <p>
     * The provided handler is registered for serverbound payloads and the client-side handler must be registered via {@code RegisterClientPayloadHandlersEvent}
     */
    public <T extends CustomPacketPayload> PayloadRegistrar commonBidirectional(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, IPayloadHandler<T> serverHandler) {
        return this.commonBidirectional(type, codec, serverHandler, null);
    }

    /**
     * Creates a copy of this registrar with a different default handling thread.
     * <p>
     * When the handling thread is set to {@link HandlerThread#MAIN}, all registered handlers will be wrapped in {@link MainThreadPayloadHandler}.
     * <p>
     * The initial handling thread is {@link HandlerThread#MAIN}.
     * 
     * @param thread The default handling thread.
     * @return A new registrar, which will map payload handlers to the desired thread.
     */
    public PayloadRegistrar executesOn(HandlerThread thread) {
        PayloadRegistrar clone = new PayloadRegistrar(this);
        clone.thread = thread;
        return clone;
    }

    /**
     * Creates a copy of this registrar with a different version. Payloads registered with the returned copy will use the passed version, instead of the version from the constructor.
     * <p>
     * On Neo-Neo connections, the connection will only succeed if all registered payloads have the same version.
     * <p>
     * On other connections, the payload version is ignored, since only Neo knows how to communicate Neo payload versions.
     *
     * @param version The version to use.
     * @return A new registrar, ready to configure payloads with that version.
     */
    public PayloadRegistrar versioned(String version) {
        PayloadRegistrar clone = new PayloadRegistrar(this);
        clone.version = version;
        return clone;
    }

    /**
     * Creates a copy of this registrar with optional mode enabled. Payloads registered with the returned copy will be marked as optional.
     * <p>
     * If any non-optional payloads are missing during a connection attempt, the connection will fail.
     * 
     * @return A new registrar, ready to configure payloads as optional.
     */
    public PayloadRegistrar optional() {
        PayloadRegistrar clone = new PayloadRegistrar(this);
        clone.optional = true;
        return clone;
    }

    private <T extends CustomPacketPayload, B extends FriendlyByteBuf> void register(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super B, T> codec,
            @Nullable IPayloadHandler<T> serverHandler,
            @Nullable IPayloadHandler<T> clientHandler,
            List<ConnectionProtocol> protocols,
            Optional<PacketFlow> flow,
            String version,
            boolean optional) {
        if (this.thread == HandlerThread.MAIN) {
            if (serverHandler != null) {
                serverHandler = new MainThreadPayloadHandler<>(serverHandler);
            }
            if (clientHandler != null) {
                clientHandler = new MainThreadPayloadHandler<>(clientHandler);
            }
        }
        NetworkRegistry.register(type, codec, serverHandler, clientHandler, protocols, flow, version, optional);
    }
}
