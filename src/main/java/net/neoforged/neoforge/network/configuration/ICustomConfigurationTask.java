/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.configuration;

import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a custom configuration task that should be run when a client connects.
 * <p>
 * This interface is a wrapper functional interface around {@link ConfigurationTask}.
 * Allowing for easily sending custom payloads to the client, without having to perform the wrapping
 * in {@link ClientboundCustomPayloadPacket} yourself.
 * <br>
 * It is recommended to use this interface over {@link ConfigurationTask} when you need to send custom payloads.
 * It's functionality is otherwise identical.
 * </p>
 */
public interface ICustomConfigurationTask extends ConfigurationTask {

    /**
     * Invoked when it is time for this configuration to run.
     *
     * @param sender A consumer that accepts a {@link CustomPacketPayload} to send to the client.
     */
    void run(Consumer<CustomPacketPayload> sender);

    /**
     * Invoked when it is time for this configuration to run.
     * 
     * @param sender A consumer that accepts a {@link Packet} to send to the client.
     * @implNote Please do not override this method, it is implemented to wrap the {@link CustomPacketPayload} in a {@link ClientboundCustomPayloadPacket}.
     */
    @Override
    @ApiStatus.Internal
    @ApiStatus.NonExtendable
    default void start(@NotNull Consumer<Packet<?>> sender) {
        run((payload) -> sender.accept(new ClientboundCustomPayloadPacket(payload)));
    }
}
