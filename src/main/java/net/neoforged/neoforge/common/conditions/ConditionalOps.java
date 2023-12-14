/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.function.ObjIntConsumer;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

/**
 * Extension of {@link RegistryOps} that also encapsulates a {@link ICondition.IContext}.
 * This allows getting the {@link ICondition.IContext} while decoding an entry from within a codec.
 */
public class ConditionalOps<T> extends RegistryOps<T> {

    public static <T> ConditionalOps<T> create(RegistryOps<T> ops, ICondition.IContext context) {
        return new ConditionalOps<T>(ops, context);
    }

    private final ICondition.IContext context;

    private ConditionalOps(RegistryOps<T> ops, ICondition.IContext context) {
        super(ops);
        this.context = context;
    }

    /**
     * Returns a codec that can retrieve a {@link ICondition.IContext} from a registry ops,
     * for example with {@code retrieveContext().decode(ops, ops.emptyMap())}.
     */
    public static MapCodec<ICondition.IContext> retrieveContext() {
        return ExtraCodecs.retrieveContext(ops -> {
            if (!(ops instanceof ConditionalOps<?> conditionalOps))
                return DataResult.success(ICondition.IContext.EMPTY);

            return DataResult.success(conditionalOps.context);
        });
    }

    /**
     * Key used for the conditions inside an object.
     */
    public static final String DEFAULT_CONDITIONS_KEY = "neoforge:conditions";
    /**
     * Key used to store the value associated with conditions,
     * when the value is not represented as a map.
     * For example, if we wanted to store the value 2 with some conditions, we could do:
     * 
     * <pre>
     * {
     *     "neoforge:conditions": [ ... ],
     *     "neoforge:value": 2
     * }
     * </pre>
     */
    public static final String CONDITIONAL_VALUE_KEY = "neoforge:value";

    /**
     * @see #createConditionalCodec(Codec, String)
     */
    public static <T> Codec<Optional<T>> createConditionalCodec(final Codec<T> ownerCodec) {
        return createConditionalCodec(ownerCodec, DEFAULT_CONDITIONS_KEY);
    }

    /**
     * Creates a conditional codec.
     *
     * <p>The conditional codec is generally not suitable for use as a dispatch target because it is never a {@link MapCodec.MapCodecCodec}.
     */
    public static <T> Codec<Optional<T>> createConditionalCodec(final Codec<T> ownerCodec, String conditionalsKey) {
        return createConditionalCodecWithConditions(ownerCodec, conditionalsKey).xmap(r -> r.map(WithConditions::carrier), r -> r.map(i -> new WithConditions<>(List.of(), i)));
    }

    /**
     * Creates a codec that can decode a list of elements, and will check for conditions on each element.
     */
    public static <T> Codec<List<T>> decodeListWithElementConditions(final Codec<T> ownerCodec) {
        return Codec.of(
                ownerCodec.listOf(),
                NeoForgeExtraCodecs.listWithOptionalElements(createConditionalCodec(ownerCodec)));
    }

    /**
     * Creates a codec that can decode a list of elements, and will check for conditions on element.
     * Additionally, a callback will be invoked with each deserialized element and its index in the list.
     *
     * <p>The index is computed before filtering out the elements with non-matching conditions,
     * but the callback will only be invoked on the elements with matching conditions.
     */
    public static <T> Codec<List<T>> decodeListWithElementConditionsAndIndexedPeek(final Codec<T> ownerCodec, final ObjIntConsumer<T> consumer) {
        return Codec.of(
                ownerCodec.listOf(),
                NeoForgeExtraCodecs.listWithoutEmpty(
                        NeoForgeExtraCodecs.decodeOnly(
                                NeoForgeExtraCodecs.listDecoderWithIndexedPeek(
                                        createConditionalCodec(ownerCodec).listOf(),
                                        (op, i) -> op.ifPresent(o -> consumer.accept(o, i))))));
    }

    /**
     * @see #createConditionalCodecWithConditions(Codec, String)
     */
    public static <T> Codec<Optional<WithConditions<T>>> createConditionalCodecWithConditions(final Codec<T> ownerCodec) {
        return createConditionalCodecWithConditions(ownerCodec, DEFAULT_CONDITIONS_KEY);
    }

    /**
     * Creates a conditional codec.
     *
     * <p>The conditional codec is generally not suitable for use as a dispatch target because it is never a {@link MapCodec.MapCodecCodec}.
     */
    public static <T> Codec<Optional<WithConditions<T>>> createConditionalCodecWithConditions(final Codec<T> ownerCodec, String conditionalsKey) {
        return Codec.of(
                new ConditionalEncoder<>(conditionalsKey, ICondition.LIST_CODEC, ownerCodec),
                new ConditionalDecoder<>(conditionalsKey, ICondition.LIST_CODEC, retrieveContext().codec(), ownerCodec));
    }

    private static final class ConditionalEncoder<A> implements Encoder<Optional<WithConditions<A>>> {
        private final String conditionalsPropertyKey;
        public final Codec<List<ICondition>> conditionsCodec;
        private final Encoder<A> innerCodec;

        private ConditionalEncoder(String conditionalsPropertyKey, Codec<List<ICondition>> conditionsCodec, Encoder<A> innerCodec) {
            this.conditionalsPropertyKey = conditionalsPropertyKey;
            this.conditionsCodec = conditionsCodec;
            this.innerCodec = innerCodec;
        }

        @Override
        public <T> DataResult<T> encode(Optional<WithConditions<A>> input, DynamicOps<T> ops, T prefix) {
            if (ops.compressMaps()) {
                // Compressing ops are not supported at the moment because they require special handling.
                return DataResult.error(() -> "Cannot use ConditionalCodec with compressing DynamicOps");
            }

            if (input.isEmpty()) {
                // Optional must be present for encoding.
                return DataResult.error(() -> "Cannot encode empty Optional with a ConditionalEncoder. We don't know what to encode to!");
            }

            final WithConditions<A> withConditions = input.get();

            if (withConditions.conditions().isEmpty()) {
                // If there are no conditions, forward to the inner codec directly.
                return innerCodec.encode(withConditions.carrier(), ops, prefix);
            }

            // By now we know we will produce a map-like object, so let's start building one.
            var recordBuilder = ops.mapBuilder();
            // Add conditions
            recordBuilder.add(conditionalsPropertyKey, conditionsCodec.encodeStart(ops, withConditions.conditions()));

            // Serialize the object
            var encodedInner = innerCodec.encodeStart(ops, withConditions.carrier());

            return encodedInner.flatMap(inner -> {
                return ops.getMap(inner).map(innerMap -> {
                    // If the inner is a map...
                    if (innerMap.get(conditionalsPropertyKey) != null || innerMap.get(CONDITIONAL_VALUE_KEY) != null) {
                        // Conditional or value key cannot be used in the inner codec!
                        return DataResult.<T>error(() -> "Cannot wrap a value that already uses the condition or value key with a ConditionalCodec.");
                    }
                    // Copy all fields to the record builder
                    innerMap.entries().forEach(pair -> {
                        recordBuilder.add(pair.getFirst(), pair.getSecond());
                    });
                    return recordBuilder.build(prefix);
                }).result().orElseGet(() -> {
                    // If the inner is not a map, write it to a value field
                    recordBuilder.add(CONDITIONAL_VALUE_KEY, inner);
                    return recordBuilder.build(prefix);
                });
            });
        }

        @Override
        public String toString() {
            return "Conditional[" + innerCodec + "]";
        }
    }

    private static final class ConditionalDecoder<A> implements Decoder<Optional<WithConditions<A>>> {
        private final String conditionalsPropertyKey;
        public final Codec<List<ICondition>> conditionsCodec;
        private final Codec<ICondition.IContext> contextCodec;
        private final Decoder<A> innerCodec;

        private ConditionalDecoder(String conditionalsPropertyKey, Codec<List<ICondition>> conditionsCodec, Codec<ICondition.IContext> contextCodec, Decoder<A> innerCodec) {
            this.conditionalsPropertyKey = conditionalsPropertyKey;
            this.conditionsCodec = conditionsCodec;
            this.contextCodec = contextCodec;
            this.innerCodec = innerCodec;
        }

        // Note: I am not too sure of what to return in the second element of the pair.
        // If this turns out to be a problem, please change it but also document it and write some test cases.
        @Override
        public <T> DataResult<Pair<Optional<WithConditions<A>>, T>> decode(DynamicOps<T> ops, T input) {
            if (ops.compressMaps()) {
                // Compressing ops are not supported at the moment because they require special handling.
                return DataResult.error(() -> "Cannot use ConditionalCodec with compressing DynamicOps");
            }

            return ops.getMap(input).map(inputMap -> {
                final T conditionsDataCarrier = inputMap.get(conditionalsPropertyKey);
                if (conditionsDataCarrier == null) {
                    // No conditions, forward to inner codec
                    return innerCodec.decode(ops, input).map(result -> result.mapFirst(carrier -> Optional.of(new WithConditions<>(carrier))));
                }

                return conditionsCodec.decode(ops, conditionsDataCarrier).flatMap(conditionsCarrier -> {
                    final List<ICondition> conditions = conditionsCarrier.getFirst();
                    final DataResult<Pair<ICondition.IContext, T>> contextDataResult = contextCodec.decode(ops, ops.emptyMap());

                    return contextDataResult.flatMap(contextCarrier -> {
                        final ICondition.IContext context = contextCarrier.getFirst();

                        final boolean conditionsMatch = conditions.stream().allMatch(c -> c.test(context));
                        if (!conditionsMatch)
                            return DataResult.success(Pair.of(Optional.empty(), input));

                        DataResult<Pair<A, T>> innerDecodeResult;

                        T valueDataCarrier = inputMap.get(CONDITIONAL_VALUE_KEY);
                        if (valueDataCarrier != null) {
                            // If there is a value field use its contents to deserialize.
                            innerDecodeResult = innerCodec.decode(ops, valueDataCarrier);
                        } else {
                            // Else copy the input into a new map without our custom key and decode from that.
                            T conditionalsKey = ops.createString(conditionalsPropertyKey);
                            var mapForDecoding = ops.createMap(inputMap
                                    .entries()
                                    .filter(pair -> !pair.getFirst().equals(conditionalsKey)));
                            innerDecodeResult = innerCodec.decode(ops, mapForDecoding);
                        }

                        // Variable is required because type inference can't handle this
                        DataResult<Pair<Optional<WithConditions<A>>, T>> ret = innerDecodeResult.map(
                                result -> result.mapFirst(
                                        carrier -> Optional.of(new WithConditions<>(conditions, carrier))));
                        return ret;
                    });
                });
            }).result().orElseGet(() -> {
                // Not a map, forward to inner codec
                return innerCodec.decode(ops, input).map(result -> result.mapFirst(carrier -> Optional.of(new WithConditions<>(carrier))));
            });
        }
    }
}
