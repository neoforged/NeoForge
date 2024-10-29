/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.assertj.core.api.Assertions.assertThat;

import com.mojang.serialization.JsonOps;
import net.minecraft.client.animation.definitions.BreezeAnimation;
import net.neoforged.neoforge.client.entity.animation.json.AnimationParser;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests that animations can be serialized into json and deserialized to be identical to the original.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class EntityModelSerializationTest {
    @Test
    void breezeRoundTripTest() {
        final var beforeSerialize = BreezeAnimation.JUMP;
        final var serialized = AnimationParser.CODEC.encodeStart(JsonOps.INSTANCE, beforeSerialize).getOrThrow();
        final var afterDeserialize = AnimationParser.CODEC.parse(JsonOps.INSTANCE, serialized).getOrThrow();
        assertThat(afterDeserialize).usingRecursiveComparison().isEqualTo(beforeSerialize);
    }
}
