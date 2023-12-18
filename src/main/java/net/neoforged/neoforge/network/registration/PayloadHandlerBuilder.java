/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IConfigurationPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * The internal implementation of {@link IDirectionAwarePayloadHandlerBuilder}, for use by {@link IPayloadRegistrar#common(ResourceLocation, FriendlyByteBuf.Reader, Consumer)}
 *
 * @param <T> The type of {@link CustomPacketPayload} that this handler handles
 */
@ApiStatus.Internal
class PayloadHandlerBuilder<T extends CustomPacketPayload> implements IDirectionAwarePayloadHandlerBuilder<T, IPayloadHandler<T>> {
    private @Nullable IPayloadHandler<T> clientSide;
    private @Nullable IPayloadHandler<T> serverSide;

    public PayloadHandlerBuilder<T> client(@NotNull IPayloadHandler<T> clientSide) {
        this.clientSide = clientSide;
        return this;
    }

    public PayloadHandlerBuilder<T> server(@NotNull IPayloadHandler<T> serverSide) {
        this.serverSide = serverSide;
        return this;
    }
    
    /**
     * Internal callback method used to configure the play builder with the handlers
     *
     * @param play The play builder
     */
    void handlePlay(IDirectionAwarePayloadHandlerBuilder<T, IPlayPayloadHandler<T>> play) {
        if (clientSide != null) {
            play.client(clientSide::handle);
        }
        if (serverSide != null) {
            play.server(serverSide::handle);
        }
    }

    /**
     * Internal callback method used to configure the configuration builder with the handlers
     *
     * @param configuration The configuration builder
     */
    void handleConfiguration(IDirectionAwarePayloadHandlerBuilder<T, IConfigurationPayloadHandler<T>> configuration) {
        if (clientSide != null) {
            configuration.client(clientSide::handle);
        }
        if (serverSide != null) {
            configuration.server(serverSide::handle);
        }
    }
}
