/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExtraCodecsTests {
    @Test
    public void test_unboundedMapAsList() {
        Map<BlockPos, String> values = Map.of(
                new BlockPos(8, 0, 0), "testA",
                new BlockPos(0, 8, 0), "testB",
                new BlockPos(0, 0, 8), "testC");
        Codec<Map<BlockPos, String>> codec = NeoForgeExtraCodecs.unboundedMapAsList("position", BlockPos.CODEC, "value", Codec.STRING);

        DataResult<JsonElement> resultJson = codec.encodeStart(JsonOps.INSTANCE, values);
        assertTrue(resultJson.isSuccess(), "Encode to JSON should succeed");

        DataResult<Tag> resultTag = codec.encodeStart(NbtOps.INSTANCE, values);
        assertTrue(resultTag.isSuccess(), "Encode to NBT should succeed");

        DataResult<Map<BlockPos, String>> resultJsonBack = codec.parse(JsonOps.INSTANCE, resultJson.getOrThrow());
        assertTrue(resultJsonBack.isSuccess(), "Decode from JSON should succeed");

        DataResult<Map<BlockPos, String>> resultTagBack = codec.parse(NbtOps.INSTANCE, resultTag.getOrThrow());
        assertTrue(resultTagBack.isSuccess(), "Decode from NBT should succeed");

        Map<BlockPos, String> jsonDecoded = resultJsonBack.getOrThrow();
        assertEquals(values, jsonDecoded, "JSON round-tripped map should be equal to original map");

        Map<BlockPos, String> tagDecoded = resultTagBack.getOrThrow();
        assertEquals(values, tagDecoded, "NBT round-tripped map should be equal to original map");
    }
}
