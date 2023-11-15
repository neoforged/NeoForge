/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.conditions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.TrueCondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import org.slf4j.Logger;

/**
 * Tests for {@link ConditionalOps} and related methods.
 * We do not have JUnit support at the moment,
 * so these unit tests run in the {@link FMLCommonSetupEvent}.
 * If you can get to the main menu, it means that the tests passed.
 */
@Mod("conditional_codec_test")
public class ConditionalCodecTest {
    public static final boolean ENABLED = true;
    private static final Logger LOGGER = LogUtils.getLogger();

    public ConditionalCodecTest(IEventBus modBus) {
        if (!ENABLED) return;

        modBus.addListener(FMLCommonSetupEvent.class, event -> {
            try {
                Record.testReadNoConditions();
                Record.testReadMatchingConditions();
                Record.testReadFailingConditions();
                Record.testReadNested();
                Record.testWriteNoConditions();
                Record.testWriteWithConditions();

                Primitive.testReadNoConditions();
                Primitive.testReadMatchingConditions();
                Primitive.testReadFailingConditions();
                Primitive.testReadNestedNoConditions();
                Primitive.testWriteNoConditions();
                Primitive.testWriteWithConditions();
            } catch (Throwable t) {
                throw new RuntimeException("ConditionalCodecTest failed", t);
            }

            LOGGER.info("ConditionalCodecTest passed");
        });
    }

    private static class Record {
        public static void testReadNoConditions() {
            JsonElement json = read("""
                    {
                        "i": 1,
                        "s": "test"
                    }
                    """);
            SimpleRecord decoded = SimpleRecord.CONDITIONAL_CODEC.decode(JsonOps.INSTANCE, json).result().get().getFirst().get();
            assertEquals(new SimpleRecord(1, "test"), decoded);
        }

        public static void testReadMatchingConditions() {
            JsonElement json = read("""
                    {
                        "neoforge:conditions": [
                            { "type": "neoforge:true" }
                        ],
                        "i": 1,
                        "s": "test"
                    }
                    """);
            SimpleRecord decoded = SimpleRecord.CONDITIONAL_CODEC.decode(JsonOps.INSTANCE, json).result().get().getFirst().get();
            assertEquals(new SimpleRecord(1, "test"), decoded);
        }

        public static void testReadFailingConditions() {
            JsonElement json = read("""
                    {
                        "neoforge:conditions": [
                            { "type": "neoforge:false" }
                        ],
                        "i": 1,
                        "s": "test"
                    }
                    """);
            // We expect a successful result with Optional.empty()
            var decoded = SimpleRecord.CONDITIONAL_CODEC.decode(JsonOps.INSTANCE, json).result().get().getFirst();
            assertEquals(Optional.empty(), decoded);
        }

        /**
         * Nesting a map field in a value field is also allowed.
         */
        public static void testReadNested() {
            JsonElement json = read("""
                    {
                        "neoforge:conditions": [
                            { "type": "neoforge:true" }
                        ],
                        "neoforge:value": {
                            "i": 1,
                            "s": "test"
                        }
                    }
                    """);
            var decoded = SimpleRecord.CONDITIONAL_CODEC.decode(JsonOps.INSTANCE, json).result().get().getFirst().get();
            assertEquals(new SimpleRecord(1, "test"), decoded);
        }

        public static void testWriteNoConditions() {
            SimpleRecord record = new SimpleRecord(1, "test");
            assertEquals("""
                    {
                      "i": 1,
                      "s": "test"
                    }""", write(SimpleRecord.CONDITIONS_CODEC, Optional.of(new WithConditions<>(record))));
        }

        public static void testWriteWithConditions() {
            SimpleRecord record = new SimpleRecord(1, "test");
            assertEquals("""
                    {
                      "neoforge:conditions": [
                        {
                          "type": "neoforge:true"
                        }
                      ],
                      "i": 1,
                      "s": "test"
                    }""", write(SimpleRecord.CONDITIONS_CODEC, Optional.of(WithConditions.builder(record).addCondition(TrueCondition.INSTANCE).build())));
        }

        private record SimpleRecord(int i, String s) {
            public static Codec<SimpleRecord> CODEC = RecordCodecBuilder.create(
                    builder -> builder
                            .group(
                                    Codec.INT.fieldOf("i").forGetter(SimpleRecord::i),
                                    Codec.STRING.fieldOf("s").forGetter(SimpleRecord::s))
                            .apply(builder, SimpleRecord::new));
            public static Codec<Optional<SimpleRecord>> CONDITIONAL_CODEC = ConditionalOps.createConditionalCodec(CODEC);
            public static Codec<Optional<WithConditions<SimpleRecord>>> CONDITIONS_CODEC = ConditionalOps.createConditionalCodecWithConditions(CODEC);
        }
    }

    private static class Primitive {
        private static final Codec<Optional<Integer>> CONDITIONAL_INT = ConditionalOps.createConditionalCodec(Codec.INT);
        private static final Codec<Optional<WithConditions<Integer>>> CONDITIONS_INT = ConditionalOps.createConditionalCodecWithConditions(Codec.INT);

        public static void testReadNoConditions() {
            JsonElement json = read("""
                    1
                    """);
            int decoded = CONDITIONAL_INT.decode(JsonOps.INSTANCE, json).result().get().getFirst().get();
            assertEquals(1, decoded);
        }

        /**
         * Nesting a value without conditions is not allowed (why would anyone do this?).
         */
        public static void testReadNestedNoConditions() {
            JsonElement json = read("""
                    {
                        "neoforge:value": 1
                    }
                    """);
            assertErrored(CONDITIONAL_INT.decode(JsonOps.INSTANCE, json));
        }

        public static void testReadMatchingConditions() {
            JsonElement json = read("""
                    {
                        "neoforge:conditions": [
                            { "type": "neoforge:true" }
                        ],
                        "neoforge:value": 1
                    }
                    """);
            int decoded = CONDITIONAL_INT.decode(JsonOps.INSTANCE, json).result().get().getFirst().get();
            assertEquals(1, decoded);
        }

        public static void testReadFailingConditions() {
            JsonElement json = read("""
                    {
                        "neoforge:conditions": [
                            { "type": "neoforge:false" }
                        ],
                        "i": 1,
                        "s": "test"
                    }
                    """);
            // We expect a successful result with Optional.empty()
            var decoded = CONDITIONAL_INT.decode(JsonOps.INSTANCE, json).result().get().getFirst();
            assertEquals(Optional.empty(), decoded);
        }

        public static void testWriteNoConditions() {
            assertEquals("""
                    1""", write(CONDITIONS_INT, Optional.of(new WithConditions<>(1))));
        }

        public static void testWriteWithConditions() {
            Record.SimpleRecord record = new Record.SimpleRecord(1, "test");
            assertEquals("""
                    {
                      "neoforge:conditions": [
                        {
                          "type": "neoforge:true"
                        }
                      ],
                      "neoforge:value": 1
                    }""", write(CONDITIONS_INT, Optional.of(WithConditions.builder(1).addCondition(TrueCondition.INSTANCE).build())));
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static JsonElement read(String s) {
        return JsonParser.parseString(s);
    }

    private static <T> String write(Codec<T> codec, T input) {
        return GSON.toJson(codec.encodeStart(JsonOps.INSTANCE, input).get().left().get());
    }

    private static <T> void assertEquals(T expected, T actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    private static void assertErrored(DataResult<?> result) {
        assertEquals(true, result.error().isPresent());
    }
}
