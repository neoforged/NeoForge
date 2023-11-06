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
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

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

                ValueKeyConflict.testReadOnlyValueNoConditions();
                ValueKeyConflict.testReadOnlyValueMatchingConditions();
                ValueKeyConflict.testReadNestedOnlyValueMatchingConditions();
                ValueKeyConflict.testWriteOnlyValueNoConditions();
                ValueKeyConflict.testWriteOnlyValueWithConditions();

                ValueKeyConflict.testReadValueAndOtherNoConditions();
                ValueKeyConflict.testReadValueAndOtherMatchingConditions();
                ValueKeyConflict.testReadNestedValueAndOtherMatchingConditions();
                ValueKeyConflict.testWriteValueAndOtherNoConditions();
                ValueKeyConflict.testWriteValueAndOtherWithConditions();
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
                        "conditions": [
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
                        "conditions": [
                            { "type": "neoforge:false" }
                        ],
                        "i": 1,
                        "s": "test"
                    }
                    """);
            // We expect a partial result with Optional.empty()
            var decoded = getPartialResult(SimpleRecord.CONDITIONAL_CODEC.decode(JsonOps.INSTANCE, json)).get().getFirst();
            assertEquals(Optional.empty(), decoded);
        }

        /**
         * Nesting a map field in a value field is not allowed.
         * This prevents ambiguities with for example an inner dispatch codec.
         */
        public static void testReadNested() {
            JsonElement json = read("""
                    {
                        "conditions": [
                            { "type": "neoforge:false" }
                        ],
                        "value": {
                            "i": 1,
                            "s": "test"
                        }
                    }
                    """);
            // We expect an errored result
            assertErrored(SimpleRecord.CONDITIONAL_CODEC.decode(JsonOps.INSTANCE, json));
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
                      "conditions": [
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
         * Nesting a value without conditions is not allowed as it could lead to ambiguities.
         */
        public static void testReadNestedNoConditions() {
            JsonElement json = read("""
                    {
                        "value": 1
                    }
                    """);
            assertErrored(CONDITIONAL_INT.decode(JsonOps.INSTANCE, json));
        }

        public static void testReadMatchingConditions() {
            JsonElement json = read("""
                    {
                        "conditions": [
                            { "type": "neoforge:true" }
                        ],
                        "value": 1
                    }
                    """);
            int decoded = CONDITIONAL_INT.decode(JsonOps.INSTANCE, json).result().get().getFirst().get();
            assertEquals(1, decoded);
        }

        public static void testReadFailingConditions() {
            JsonElement json = read("""
                    {
                        "conditions": [
                            { "type": "neoforge:false" }
                        ],
                        "i": 1,
                        "s": "test"
                    }
                    """);
            // We expect a partial result with Optional.empty()
            var decoded = getPartialResult(CONDITIONAL_INT.decode(JsonOps.INSTANCE, json)).get().getFirst();
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
                      "conditions": [
                        {
                          "type": "neoforge:true"
                        }
                      ],
                      "value": 1
                    }""", write(CONDITIONS_INT, Optional.of(WithConditions.builder(1).addCondition(TrueCondition.INSTANCE).build())));
        }
    }

    private static class ValueKeyConflict {
        private record OnlyValue(String value) {
            public static Codec<OnlyValue> CODEC = RecordCodecBuilder.create(
                    builder -> builder
                            .group(
                                    Codec.STRING.fieldOf("value").forGetter(OnlyValue::value))
                            .apply(builder, OnlyValue::new));
            public static Codec<Optional<OnlyValue>> CONDITIONAL_CODEC = ConditionalOps.createConditionalCodec(CODEC);
            public static Codec<Optional<WithConditions<OnlyValue>>> CONDITIONS_CODEC = ConditionalOps.createConditionalCodecWithConditions(CODEC);
        }

        private record ValueAndOther(String value, String other) {
            public static Codec<ValueAndOther> CODEC = RecordCodecBuilder.create(
                    builder -> builder
                            .group(
                                    Codec.STRING.fieldOf("value").forGetter(ValueAndOther::value),
                                    Codec.STRING.fieldOf("other").forGetter(ValueAndOther::other))
                            .apply(builder, ValueAndOther::new));
            public static Codec<Optional<ValueAndOther>> CONDITIONAL_CODEC = ConditionalOps.createConditionalCodec(CODEC);
            public static Codec<Optional<WithConditions<ValueAndOther>>> CONDITIONS_CODEC = ConditionalOps.createConditionalCodecWithConditions(CODEC);
        }

        public static void testReadOnlyValueNoConditions() {
            JsonElement json = read("""
                    {
                        "value": "test"
                    }
                    """);
            OnlyValue decoded = OnlyValue.CONDITIONAL_CODEC.decode(JsonOps.INSTANCE, json).result().get().getFirst().get();
            assertEquals(new OnlyValue("test"), decoded);
        }

        /**
         * This will fail because "test" will be used to deserialize the OnlyValue.
         */
        public static void testReadOnlyValueMatchingConditions() {
            JsonElement json = read("""
                    {
                        "conditions": [
                            { "type": "neoforge:true" }
                        ],
                        "value": "test"
                    }
                    """);
            assertErrored(OnlyValue.CONDITIONAL_CODEC.decode(JsonOps.INSTANCE, json));
        }

        /**
         * A nested value should work here.
         */
        public static void testReadNestedOnlyValueMatchingConditions() {
            JsonElement json = read("""
                    {
                        "conditions": [
                            { "type": "neoforge:true" }
                        ],
                        "value": {
                            "value": "test"
                        }
                    }
                    """);
            OnlyValue decoded = OnlyValue.CONDITIONAL_CODEC.decode(JsonOps.INSTANCE, json).result().get().getFirst().get();
            assertEquals(new OnlyValue("test"), decoded);
        }

        public static void testWriteOnlyValueNoConditions() {
            assertEquals("""
                    {
                      "value": "test"
                    }""", write(OnlyValue.CONDITIONS_CODEC, Optional.of(new WithConditions<>(new OnlyValue("test")))));
        }

        public static void testWriteOnlyValueWithConditions() {
            assertEquals("""
                    {
                      "conditions": [
                        {
                          "type": "neoforge:true"
                        }
                      ],
                      "value": {
                        "value": "test"
                      }
                    }""", write(OnlyValue.CONDITIONS_CODEC, Optional.of(WithConditions.builder(new OnlyValue("test")).addCondition(TrueCondition.INSTANCE).build())));
        }

        public static void testReadValueAndOtherNoConditions() {
            JsonElement json = read("""
                    {
                        "value": "test",
                        "other": "test"
                    }
                    """);
            ValueAndOther decoded = ValueAndOther.CONDITIONAL_CODEC.decode(JsonOps.INSTANCE, json).result().get().getFirst().get();
            assertEquals(new ValueAndOther("test", "test"), decoded);
        }

        /**
         * This will succeed because there is a 3rd key in the object, so we know the value is top-level.
         */
        public static void testReadValueAndOtherMatchingConditions() {
            JsonElement json = read("""
                    {
                        "conditions": [
                            { "type": "neoforge:true" }
                        ],
                        "value": "test",
                        "other": "test"
                    }
                    """);
            ValueAndOther decoded = ValueAndOther.CONDITIONAL_CODEC.decode(JsonOps.INSTANCE, json).result().get().getFirst().get();
            assertEquals(new ValueAndOther("test", "test"), decoded);
        }

        /**
         * The nested value will work because there will only be 2 keys in the map, so we know the value is not top-level.
         */
        public static void testReadNestedValueAndOtherMatchingConditions() {
            JsonElement json = read("""
                    {
                        "conditions": [
                            { "type": "neoforge:true" }
                        ],
                        "value": {
                            "value": "test",
                            "other": "test"
                        }
                    }
                    """);
            ValueAndOther decoded = ValueAndOther.CONDITIONAL_CODEC.decode(JsonOps.INSTANCE, json).result().get().getFirst().get();
            assertEquals(new ValueAndOther("test", "test"), decoded);
        }

        public static void testWriteValueAndOtherNoConditions() {
            assertEquals("""
                    {
                      "value": "test",
                      "other": "test"
                    }""", write(ValueAndOther.CONDITIONS_CODEC, Optional.of(new WithConditions<>(new ValueAndOther("test", "test")))));
        }

        public static void testWriteValueAndOtherWithConditions() {
            assertEquals("""
                    {
                      "conditions": [
                        {
                          "type": "neoforge:true"
                        }
                      ],
                      "value": "test",
                      "other": "test"
                    }""", write(ValueAndOther.CONDITIONS_CODEC, Optional.of(WithConditions.builder(new ValueAndOther("test", "test")).addCondition(TrueCondition.INSTANCE).build())));
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static JsonElement read(String s) {
        return JsonParser.parseString(s);
    }

    private static <T> String write(Codec<T> codec, T input) {
        return GSON.toJson(codec.encodeStart(JsonOps.INSTANCE, input).get().left().get());
    }

    private static <R> Optional<R> getPartialResult(DataResult<R> result) {
        assertEquals(true, result.error().isPresent());
        return result.promotePartial(e -> {}).result();
    }

    private static <T> void assertEquals(T expected, T actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    private static void assertErrored(DataResult<?> result) {
        MutableBoolean hasError = new MutableBoolean();
        result.promotePartial(error -> hasError.setTrue());
        assertEquals(true, hasError.booleanValue());
    }
}
