/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Helper class that merges two unidirectional handlers into a single bidirectional handler.
 */
public record DirectionalPayloadHandler<T extends CustomPacketPayload>(IPayloadHandler<T> clientSide, IPayloadHandler<T> serverSide) implements IPayloadHandler<T> {
    @Override
    public void handle(T payload, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            clientSide.handle(payload, context);
        } else if (context.flow().isServerbound()) {
            serverSide.handle(payload, context);
        }
    }
}
