/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ExtraCodecsTests {
    private final Map<BlockPos, String> values = Map.of(
            new BlockPos(8, 0, 0), "testA",
            new BlockPos(0, 8, 0), "testB",
            new BlockPos(0, 0, 8), "testC");

    private final Codec<Map<BlockPos, String>> codec = NeoForgeExtraCodecs.unboundedMapAsList("position", BlockPos.CODEC, "value", Codec.STRING);

    @DisplayName("test round-trip for unboundedMapAsList codec")
    @ParameterizedTest(name = "using ops for {1}")
    @MethodSource("provideDynamicOps")
    public <T> void roundTripTest_unboundedMapAsList(DynamicOps<T> ops, String name) {
        DataResult<T> resultJson = codec.encodeStart(ops, values);
        assertTrue(resultJson.isSuccess(), "Encode to " + name + " should succeed");

        DataResult<Map<BlockPos, String>> resultJsonBack = codec.parse(ops, resultJson.getOrThrow());
        assertTrue(resultJsonBack.isSuccess(), "Decode from " + name + " should succeed");

        Map<BlockPos, String> jsonDecoded = resultJsonBack.getOrThrow();
        assertEquals(values, jsonDecoded, name + "JSON round-tripped map should be equal to original map");
    }

    private static Stream<Arguments> provideDynamicOps() {
        return Stream.of(
                arguments(NbtOps.INSTANCE, "NBT"),
                arguments(JsonOps.INSTANCE, "JSON"));
    }
}
