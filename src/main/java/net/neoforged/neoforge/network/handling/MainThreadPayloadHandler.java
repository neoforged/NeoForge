/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Helper class that always executes the wrapped handler on the main thread.
 */
public record MainThreadPayloadHandler<T extends CustomPacketPayload>(IPayloadHandler<T> handler) implements IPayloadHandler<T> {
    @Override
    public void handle(T payload, IPayloadContext context) {
        context.enqueueWork(() -> this.handler().handle(payload, context));
    }
}
