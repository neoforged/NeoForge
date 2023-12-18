/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration.registrar;

import java.util.Optional;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlayPayloadHandler<T extends CustomPacketPayload> implements IPlayPayloadHandler<T> {

    @Nullable
    private final IPlayPayloadHandler<T> clientSide;
    @Nullable
    private final IPlayPayloadHandler<T> serverSide;

    private PlayPayloadHandler(@Nullable IPlayPayloadHandler<T> clientSide, @Nullable IPlayPayloadHandler<T> serverSide) {
        this.clientSide = clientSide;
        this.serverSide = serverSide;
    }

    @Override
    public void handle(PlayPayloadContext context, T payload) {
        if (context.flow().isClientbound()) {
            if (clientSide != null) {
                clientSide.handle(context, payload);
            }
        } else if (context.flow().isServerbound()) {
            if (serverSide != null) {
                serverSide.handle(context, payload);
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

    public static class Builder<T extends CustomPacketPayload> {
        private @Nullable IPlayPayloadHandler<T> clientSide;
        private @Nullable IPlayPayloadHandler<T> serverSide;

        public Builder<T> client(@NotNull IPlayPayloadHandler<T> clientSide) {
            this.clientSide = clientSide;
            return this;
        }

        public Builder<T> server(@NotNull IPlayPayloadHandler<T> serverSide) {
            this.serverSide = serverSide;
            return this;
        }

        PlayPayloadHandler<T> create() {
            return new PlayPayloadHandler<T>(clientSide, serverSide);
        }
    }
}
