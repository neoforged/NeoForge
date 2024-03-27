/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import java.util.Optional;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class DirectionalPayloadHandler<T extends CustomPacketPayload> implements IPayloadHandler<T> {
    @Nullable
    private final IPayloadHandler<T> clientSide;
    @Nullable
    private final IPayloadHandler<T> serverSide;

    DirectionalPayloadHandler(@Nullable IPayloadHandler<T> clientSide, @Nullable IPayloadHandler<T> serverSide) {
        this.clientSide = clientSide;
        this.serverSide = serverSide;

        if (clientSide == null && serverSide == null) {
            throw new UnsupportedOperationException("Attempted to register a directional payload handler with no delegate handlers. At least one must be provided.");
        }
    }

    @Override
    public void handle(T payload, IPayloadContext context) {
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
        if (clientSide == null) {
            return Optional.of(PacketFlow.SERVERBOUND);
        }

        if (serverSide == null) {
            return Optional.of(PacketFlow.CLIENTBOUND);
        }

        return Optional.empty();
    }
}
