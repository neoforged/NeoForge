/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class DummyPayloadHandler implements IPayloadHandler<CustomPacketPayload> {
    private static final DummyPayloadHandler INSTANCE = new DummyPayloadHandler();

    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> IPayloadHandler<T> instance() {
        return (IPayloadHandler<T>) INSTANCE;
    }

    @Override
    public void handle(CustomPacketPayload payload, IPayloadContext context) {
        throw new AssertionError("Dummy");
    }
}
