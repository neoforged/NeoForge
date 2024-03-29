/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import io.netty.buffer.Unpooled;
import java.util.function.Consumer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * Utility class for working with {@link FriendlyByteBuf}s.
 */
public class FriendlyByteBufUtil {
    private FriendlyByteBufUtil() {
        throw new IllegalStateException("Tried to create utility class!");
    }

    /**
     * Writes custom data to a {@link RegistryFriendlyByteBuf}, then returns the written data as a byte array.
     *
     * @param dataWriter     The data writer.
     * @param registryAccess The registry access used by registry dependent writers on the buffer
     * @return The written data.
     */
    public static byte[] writeCustomData(Consumer<RegistryFriendlyByteBuf> dataWriter, RegistryAccess registryAccess) {
        final RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
        try {
            dataWriter.accept(buf);
            return buf.array();
        } finally {
            buf.release();
        }
    }
}
