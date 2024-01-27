/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import java.util.Optional;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.handling.IConfigurationPayloadHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * An internal implementation of {@link IDirectionAwarePayloadHandlerBuilder} for {@link IConfigurationPayloadHandler}.
 *
 * @param <T> The payload type.
 */
@ApiStatus.Internal
public final class ConfigurationPayloadHandler<T extends CustomPacketPayload> implements IConfigurationPayloadHandler<T> {
    @Nullable
    private final IConfigurationPayloadHandler<T> clientSide;
    @Nullable
    private final IConfigurationPayloadHandler<T> serverSide;

    private ConfigurationPayloadHandler(@Nullable IConfigurationPayloadHandler<T> clientSide, @Nullable IConfigurationPayloadHandler<T> serverSide) {
        this.clientSide = clientSide;
        this.serverSide = serverSide;
    }

    @Override
    public void handle(T payload, ConfigurationPayloadContext context) {
        if (context.flow().isClientbound()) {
            if (clientSide != null) {
                clientSide.handle(payload, context);
            }
        } else if (context.flow().isServerbound()) {
            if (serverSide != null) {
                serverSide.handle(payload, context);
            }
        }
    }

    Optional<PacketFlow> flow() {
        if (clientSide == null && serverSide == null) {
            return Optional.empty();
        }

        if (clientSide == null) {
            return Optional.of(PacketFlow.SERVERBOUND);
        }

        if (serverSide == null) {
            return Optional.of(PacketFlow.CLIENTBOUND);
        }

        return Optional.empty();
    }

    /**
     * Internal builder for a configuration payload handler.
     *
     * @param <T> The type of payload.
     */
    static class Builder<T extends CustomPacketPayload> implements IDirectionAwarePayloadHandlerBuilder<T, IConfigurationPayloadHandler<T>> {
        private @Nullable IConfigurationPayloadHandler<T> clientSide;
        private @Nullable IConfigurationPayloadHandler<T> serverSide;

        public Builder<T> client(IConfigurationPayloadHandler<T> clientSide) {
            this.clientSide = clientSide;
            return this;
        }

        public Builder<T> server(IConfigurationPayloadHandler<T> serverSide) {
            this.serverSide = serverSide;
            return this;
        }

        ConfigurationPayloadHandler<T> create() {
            return new ConfigurationPayloadHandler<T>(clientSide, serverSide);
        }
    }
}
