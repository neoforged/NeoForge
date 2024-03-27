/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import java.util.function.Consumer;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

/**
 * Defines a registrar for custom payloads that can be sent over the network.
 * <p>
 * A custom payload is a class which extends {@link CustomPacketPayload}, it is recommended to use a record for this.
 * </p>
 * <p>
 * The payload is written to the networks outgoing buffer using the {@link CustomPacketPayload#write(FriendlyByteBuf)} method.
 * However, to read the payload from the incoming buffer, your registered {@link FriendlyByteBuf.Reader} is used.
 * <br>
 * When you implement your {@link CustomPacketPayload#write(FriendlyByteBuf)} method you do not need to write the id of the payload,
 * neither do you need to read it in your {@link FriendlyByteBuf.Reader} implementation. However, you do need to make sure that the
 * id you pass into {@link #play(ResourceLocation, FriendlyByteBuf.Reader, IPlayPayloadHandler)} and
 * {@link #configuration(ResourceLocation, FriendlyByteBuf.Reader, IConfigurationPayloadHandler)} is the same as the id you
 * return from your {@link CustomPacketPayload#id()}. We suggest using a <code>public static final ResourceLocation</code> field
 * to store it and then reference it in both places.
 * <br>
 * Ids can be reused between play and configuration payloads, but it is needed to use different ids for different payloads.
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
 * You can register a custom payload for either of these types of payloads using the {@link #play(ResourceLocation, FriendlyByteBuf.Reader, IPlayPayloadHandler)}
 * and {@link #configuration(ResourceLocation, FriendlyByteBuf.Reader, IConfigurationPayloadHandler)} methods respectively.
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
 * {@link #play(ResourceLocation, FriendlyByteBuf.Reader, IPlayPayloadHandler)} or {@link #configuration(ResourceLocation, FriendlyByteBuf.Reader, IConfigurationPayloadHandler)}
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
    <T extends CustomPacketPayload> IPayloadRegistrar play(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, IPayloadHandler<T> handler);

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
    <T extends CustomPacketPayload> IPayloadRegistrar play(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, Consumer<DirectionalPayloadHandlerBuilder<T>> handler);

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
    <T extends CustomPacketPayload> IPayloadRegistrar configuration(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, IPayloadHandler<T> handler);

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
    <T extends CustomPacketPayload> IPayloadRegistrar configuration(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, Consumer<DirectionalPayloadHandlerBuilder<T>> handler);

    /**
     * Registers a new payload type for all supported phases.
     *
     * @param <T>     The type of the payload.
     * @param id      The id of the payload.
     * @param reader  The reader for the payload.
     * @param handler The handler for the payload.
     * @return The registrar.
     */
    default <T extends CustomPacketPayload> IPayloadRegistrar common(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, IPayloadHandler<T> handler) {
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
    <T extends CustomPacketPayload> IPayloadRegistrar common(ResourceLocation id, FriendlyByteBuf.Reader<T> reader, Consumer<DirectionalPayloadHandlerBuilder<T>> handler);

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
