/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.codec;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
public class EnergyStorageCodecTests {
    static final int ENERGY_STORED = 100;
    static final int CAPACITY = 5000;

    private static EnergyStorage makeStorage() {
        return new EnergyStorage(CAPACITY, 250, 250, ENERGY_STORED);
    }

    @Test
    public void canSerializeWithCodec(MinecraftServer server) {
        var storage = makeStorage();

        var wrappedResult = storage.CODEC.encodeStart(server.registryAccess().createSerializationContext(NbtOps.INSTANCE), storage);

        var naturalResult = storage.serializeNBT(server.registryAccess());

        Assertions.assertTrue(wrappedResult.isSuccess());
        Assertions.assertEquals(naturalResult, wrappedResult.getOrThrow());
    }

    private static void assertDeserializedHandlerEquals(EnergyStorage expected, EnergyStorage actual) {
        Assertions.assertNotSame(expected, actual);
        Assertions.assertEquals(expected.getEnergyStored(), actual.getEnergyStored());
        Assertions.assertEquals(expected.getMaxEnergyStored(), actual.getMaxEnergyStored());
    }

    @Test
    public void canDeserializeWithCodec(MinecraftServer server) {
        var storage = makeStorage();

        var naturalResult = storage.serializeNBT(server.registryAccess());

        var wrappedResult = storage.CODEC
                .parse(server.registryAccess().createSerializationContext(NbtOps.INSTANCE), naturalResult);

        Assertions.assertTrue(wrappedResult.isSuccess());

        assertDeserializedHandlerEquals(storage, wrappedResult.getOrThrow());
    }

    @Test
    public void canRoundTripWithStreamCodec(MinecraftServer server) {
        var original = makeStorage();

        final RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), server.registryAccess(), ConnectionType.NEOFORGE);

        original.STREAM_CODEC.encode(buf, original);

        final var result = original.STREAM_CODEC.decode(buf);

        assertDeserializedHandlerEquals(original, result);
    }
}
