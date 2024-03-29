/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import java.util.function.Consumer;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.handling.IConfigurationPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.handling.ISynchronizedWorkHandler;
import net.neoforged.neoforge.network.handling.ITaskCompletedHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Defines a registrar for custom payloads that can be sent over the network.
 * <p>
 * A custom payload is a class which extends {@link CustomPacketPayload}, it is recommended to use a record for this.
 * </p>
 * <p>
 * The payload is written to the networks outgoing buffer and read from the incoming buffer using your registered {@link StreamCodec}
 * <br>
 * When you implement your {@link StreamCodec}, you do not need to read or write the id of the payload. However, you do
 * need to make sure that the id you pass into {@link #play(CustomPacketPayload.Type, StreamCodec, IPlayPayloadHandler)} and
 * {@link #configuration(CustomPacketPayload.Type, StreamCodec, IConfigurationPayloadHandler)} is the same as the id held by the
 * {@link CustomPacketPayload.Type} you return from your {@link CustomPacketPayload#type()}. We suggest using a
 * <code>public static final CustomPacketPayload.Type</code> field to store it and then reference it in both places.
 * <br>
 * Types can be reused between play and configuration payloads, but it is needed to use different types with different
 * ids for different payloads.
 * <br>
 * Under certain situations you are not able to register a payload:
 * <ul>
 * <li>If the id you are trying to register is already in use, meaning you used the same id twice for different packets of the same kind.</li>
 * <li>If you are trying to register a payload to a namespace that is not your own.</li>
 * <li>If the registrar has become invalid.</li>
 * </ul>
 * This means that the registration will fail if any of these cases occur.
 * The exception thrown in these cases is a {@link RegistrationFailedException}.
 * </p>
 * <p>
 * There are two kinds of payloads:
 * <ul>
 * <li>Play payloads: These are payloads that are sent from the client to the server, or from the server to the client, during normal gameplay.</li>
 * <li>Configuration payloads: These are payloads that are sent from the server to the client, or from the client to the server, during the login process, before the player is spawned.</li>
 * </ul>
 * You can register a custom payload for either of these types of payloads using the {@link #play(CustomPacketPayload.Type, StreamCodec, IPlayPayloadHandler)}
 * and {@link #configuration(CustomPacketPayload.Type, StreamCodec, IConfigurationPayloadHandler)} methods respectively.
 * <br>
 * The difference between the play and configuration phases, if you like to call them that, is that the configuration phase generally requires
 * a confirmation payload to be returned to the server to trigger the next phase. In the {@link ConfigurationPayloadContext context} passed into
 * your {@link IConfigurationPayloadHandler} you will find a {@link ITaskCompletedHandler} which you can use, <span class="strong">on the server side</span>,
 * to notify the connection management system that a given {@link ConfigurationTask.Type} has been completed. This will trigger the next phase of the
 * login process. Invoking the {@link ITaskCompletedHandler#onTaskCompleted(ConfigurationTask.Type)} method on the client, will throw an exception.
 * </p>
 * <p>
 * Note: the processing of payloads happens solely on the network thread. You are responsible for ensuring that any data you access
 * in your handlers is either thread safe, or that you queue up your work to be done on the main thread, of the relevant side.
 * This is particularly important for the {@link IPlayPayloadHandler} or {@link IConfigurationPayloadHandler} implementations that you pass to
 * {@link #play(CustomPacketPayload.Type, StreamCodec, IPlayPayloadHandler)} or {@link #configuration(CustomPacketPayload.Type, StreamCodec, IConfigurationPayloadHandler)}
 * respectively, since those are also invoked on the network thread.
 * <br>
 * The {@link PlayPayloadContext} and {@link ConfigurationPayloadContext} given to each of these handlers contains a {@link ISynchronizedWorkHandler}
 * which you can use to submit work to be run on the main thread of the game. This is the recommended way to handle any work that needs to be done
 * on the main thread.
 * <br>
 * Note the reader passed to any of the registration methods in this interface is invoked only if the payload is actually transferred over a connection which
 * is not marked as {@link Connection#isMemoryConnection()}. This is important for single-player of lan-opened worlds since there the writer and reader are
 * not invoked. That is because the payload is not actually transferred over the network, but only passed around in memory.
 * </p>
 */
public interface IPayloadRegistrar {
    /**
     * Registers a new payload type for the play phase.
     *
     * @param <T>     The type of the payload.
     * @param id      The id of the payload.
     * @param reader  The reader for the payload.
     * @param handler The handler for the payload.
     * @return The registrar.
     * @implNote This method will capture all internal errors and wrap them in a {@link RegistrationFailedException}.
     */
    <T extends CustomPacketPayload> IPayloadRegistrar play(CustomPacketPayload.Type<T> id, StreamCodec<? super RegistryFriendlyByteBuf, T> reader, IPlayPayloadHandler<T> handler);

    /**
     * Registers a new payload type for the play phase.
     * <p>
     * This method allows different handlers to be registered for different packet-flows.
     * <br>
     * In practice this means that you can register a different handler for clientbound and serverbound packets,
     * which allows you to handle them differently on the client and server side.
     * </p>
     *
     * @param <T>     The type of the payload.
     * @param id      The id of the payload.
     * @param reader  The reader for the payload.
     * @param handler The handler for the payload.
     * @return The registrar.
     * @implNote This method will capture all internal errors and wrap them in a {@link RegistrationFailedException}.
     */
    <T extends CustomPacketPayload> IPayloadRegistrar play(CustomPacketPayload.Type<T> id, StreamCodec<? super RegistryFriendlyByteBuf, T> reader, Consumer<IDirectionAwarePayloadHandlerBuilder<T, IPlayPayloadHandler<T>>> handler);

    /**
     * Registers a new payload type for the configuration phase.
     *
     * @param <T>     The type of the payload.
     * @param id      The id of the payload.
     * @param reader  The reader for the payload.
     * @param handler The handler for the payload.
     * @return The registrar.
     * @implNote This method will capture all internal errors and wrap them in a {@link RegistrationFailedException}.
     */
    <T extends CustomPacketPayload> IPayloadRegistrar configuration(CustomPacketPayload.Type<T> id, StreamCodec<? super FriendlyByteBuf, T> reader, IConfigurationPayloadHandler<T> handler);

    /**
     * Registers a new payload type for the configuration phase.
     * <p>
     * This method allows different handlers to be registered for different packet-flows.
     * <br>
     * In practice this means that you can register a different handler for clientbound and serverbound packets,
     * which allows you to handle them differently on the client and server side.
     * </p>
     *
     * @param <T>     The type of the payload.
     * @param id      The id of the payload.
     * @param reader  The reader for the payload.
     * @param handler The handler for the payload.
     * @return The registrar.
     * @implNote This method will capture all internal errors and wrap them in a {@link RegistrationFailedException}.
     */
    <T extends CustomPacketPayload> IPayloadRegistrar configuration(CustomPacketPayload.Type<T> id, StreamCodec<? super FriendlyByteBuf, T> reader, Consumer<IDirectionAwarePayloadHandlerBuilder<T, IConfigurationPayloadHandler<T>>> handler);

    /**
     * Registers a new payload type for all supported phases.
     *
     * @param <T>     The type of the payload.
     * @param id      The id of the payload.
     * @param reader  The reader for the payload.
     * @param handler The handler for the payload.
     * @return The registrar.
     */
    default <T extends CustomPacketPayload> IPayloadRegistrar common(CustomPacketPayload.Type<T> id, StreamCodec<? super FriendlyByteBuf, T> reader, IPayloadHandler<T> handler) {
        return play(id, reader, handler::handle).configuration(id, reader, handler::handle);
    }

    /**
     * Registers a new payload type for all supported phases.
     * <p>
     * This method allows different handlers to be registered for different packet-flows.
     * <br>
     * In practice this means that you can register a different handler for clientbound and serverbound packets,
     * which allows you to handle them differently on the client and server side.
     * </p>
     *
     * @param <T>     The type of the payload.
     * @param id      The id of the payload.
     * @param reader  The reader for the payload.
     * @param handler The handler for the payload.
     * @return The registrar.
     */
    <T extends CustomPacketPayload> IPayloadRegistrar common(CustomPacketPayload.Type<T> id, StreamCodec<? super FriendlyByteBuf, T> reader, Consumer<IDirectionAwarePayloadHandlerBuilder<T, IPayloadHandler<T>>> handler);

    /**
     * Defines that the payloads registered by this registrar have a specific version associated with them.
     * Clients connecting to a server with these payloads, will only be able to connect if they have the same version.
     *
     * @param version The version to use.
     * @return A new registrar, ready to configure payloads with that version.
     * @implNote The registrar implementation is immutable, so this method will return a new registrar.
     */
    IPayloadRegistrar versioned(String version);

    /**
     * Defines that the payloads registered by this registrar are optional.
     * Clients connecting to a server which do not have the payloads registered, will still be able to connect.
     * <p>
     * If clients have also a version set, and a version mismatch occurs (so both client and server have the payloads registered,
     * yet have different versions), the connection attempt will fail.
     * In other words, marking a payload as optional does not exempt it from versioning, if it has that configured.
     * </p>
     * 
     * @return A new registrar, ready to configure payloads as optional.
     * @implNote The registrar implementation is immutable, so this method will return a new registrar.
     */
    IPayloadRegistrar optional();
}
