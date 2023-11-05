/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.stream.Stream;
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

    public static <T> MapCodec<Optional<T>> createConditionalCodec(final Codec<T> ownerCodec) {
        return createConditionalCodec(ownerCodec, "conditions");
    }

    public static <T> MapCodec<Optional<T>> createConditionalCodec(final Codec<T> ownerCodec, String conditionalsKey) {
        return createConditionalCodecWithConditions(ownerCodec, conditionalsKey).xmap(r -> r.map(WithConditions::carrier), r -> r.map(i -> new WithConditions<>(List.of(), i)));
    }

    public static <T> Codec<List<T>> decodeListWithElementConditions(final Codec<T> ownerCodec, String conditionalsKey) {
        final Codec<List<T>> delegate = ownerCodec.listOf();
        final Decoder<List<T>> decoder = NeoForgeExtraCodecs.listDecoderWithOptionalElements(createConditionalDecoder(ownerCodec, conditionalsKey));
        return Codec.of(delegate, decoder);
    }

    public static <T> Codec<List<T>> decodeListWithElementConditionsAndConsumeIndex(final Codec<T> ownerCodec, final String conditionalsKey, final ObjIntConsumer<T> consumer) {
        final Codec<List<T>> list = ownerCodec.listOf();
        return Codec.of(list, NeoForgeExtraCodecs.listOptionalUnwrapDecoder(
                NeoForgeExtraCodecs.listDecoderWithIndexConsumer(
                        NeoForgeExtraCodecs.listDecoder(createConditionalDecoder(ownerCodec, conditionalsKey)),
                        (op, i) -> op.ifPresent(o -> consumer.accept(o, i))),
                Function.identity()));
    }

    /**
     * @see #createConditionalDecoder(Decoder, String)
     */
    public static <T> Decoder<Optional<T>> createConditionalDecoder(final Decoder<T> ownerDecoder) {
        return createConditionalDecoder(ownerDecoder, "conditions");
    }

    /**
     * Creates a conditional decoder.
     * If the inner object (of type {@code T}) serializes as a map, then conditions are checked.
     * Otherwise, conditions are not looked for.
     * In other words, the deserializer does not assume that the inner object is a map.
     */
    public static <T> Decoder<Optional<T>> createConditionalDecoder(final Decoder<T> ownerDecoder, String conditionalsKey) {
        var conditionalMapDecoder = new ConditionalDecoder<>(conditionalsKey, ICondition.LIST_CODEC, retrieveContext().codec(), ownerDecoder)
                .map(r -> r.map(WithConditions::carrier));
        return new Decoder<>() {
            @Override
            public <I> DataResult<Pair<Optional<T>, I>> decode(DynamicOps<I> ops, I input) {
                var map = ops.getMap(input).get();
                return map.map(
                        mapLike -> {
                            // Converted to map, so decode with the conditional MapDecoder.
                            return conditionalMapDecoder.decode(ops, mapLike).map(obj -> Pair.of(obj, input));
                        },
                        partialResult -> {
                            // Failed to convert to map, so fall back to the inner decoder.
                            return ownerDecoder.decode(ops, input).map(r -> r.mapFirst(Optional::of));
                        });
            }

            @Override
            public String toString() {
                return "ConditionalDecoder[" + ownerDecoder + "]";
            }
        };
    }

    /**
     * @see #createConditionalCodecWithConditions(Codec, String)
     */
    public static <T> MapCodec<Optional<WithConditions<T>>> createConditionalCodecWithConditions(final Codec<T> ownerCodec) {
        return createConditionalCodecWithConditions(ownerCodec, "conditions");
    }

    /**
     * Creates a conditional codec. The codec assumes that the inner object (of type {@code T}) serializes as a map.
     */
    public static <T> MapCodec<Optional<WithConditions<T>>> createConditionalCodecWithConditions(final Codec<T> ownerCodec, String conditionalsKey) {
        return MapCodec.of(
                new ConditionalEncoder<>(conditionalsKey, ICondition.LIST_CODEC, ownerCodec),
                new ConditionalDecoder<>(conditionalsKey, ICondition.LIST_CODEC, retrieveContext().codec(), ownerCodec));
    }

    private static final class ConditionalEncoder<A> extends MapEncoder.Implementation<Optional<WithConditions<A>>> {
        private final String conditionalsPropertyKey;
        public final Codec<List<ICondition>> conditionsCodec;
        private final Encoder<A> innerCodec;

        private ConditionalEncoder(String conditionalsPropertyKey, Codec<List<ICondition>> conditionsCodec, Encoder<A> innerCodec) {
            this.conditionalsPropertyKey = conditionalsPropertyKey;
            this.conditionsCodec = conditionsCodec;
            this.innerCodec = innerCodec;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(conditionalsPropertyKey).map(ops::createString);
        }

        @Override
        public <T> RecordBuilder<T> encode(Optional<WithConditions<A>> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            if (ops.compressMaps()) {
                // Cannot enumerate the keys from inner in keys(), so we cannot support compressing ops at the moment.
                return prefix.withErrorsFrom(DataResult.error(() -> "Cannot use ConditionalCodec with compressing DynamicOps"));
            }

            if (input.isEmpty()) {
                return prefix;
            }

            final WithConditions<A> withConditions = input.get();

            // Add conditions to the map
            if (!withConditions.conditions().isEmpty()) {
                prefix.add(conditionalsPropertyKey, conditionsCodec.encodeStart(ops, withConditions.conditions()));
            }

            // Add fields from the inner object
            if (innerCodec instanceof MapCodec.MapCodecCodec<A> innerMap) {
                // Also a MapCodec -> encode to the prefix directly
                return innerMap.codec().encode(withConditions.carrier(), ops, prefix);
            } else {
                // Else we don't know what the object serializes to, so let's serialize it first.
                var encodedInner = innerCodec.encodeStart(ops, withConditions.carrier());
                // Then attempt to convert to a map.
                var asMap = encodedInner.flatMap(ops::getMap);

                return asMap.map(innerMap -> {
                    // Add all values from the inner map into the prefix.
                    innerMap.entries().forEach(pair -> {
                        if (!pair.getFirst().equals(conditionalsPropertyKey)) {
                            prefix.add(pair.getFirst(), pair.getSecond());
                        }
                    });
                    return prefix;
                }).result().orElseGet(() -> {
                    // Add errors encountered when trying to convert encodedInner to a map.
                    return prefix.withErrorsFrom(asMap);
                });
            }
        }
    }

    private static final class ConditionalDecoder<A> extends MapDecoder.Implementation<Optional<WithConditions<A>>> {
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

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(conditionalsPropertyKey).map(ops::createString);
        }

        @Override
        public <T> DataResult<Optional<WithConditions<A>>> decode(DynamicOps<T> ops, MapLike<T> input) {
            if (ops.compressMaps()) {
                // Cannot enumerate the keys from inner in keys(), so we cannot support compressing ops at the moment.
                return DataResult.error(() -> "Cannot use ConditionalCodec with compressing DynamicOps");
            }

            final T conditionsDataCarrier = input.get(conditionalsPropertyKey);
            if (conditionsDataCarrier == null) {
                return decodeInner(ops, input).map(result -> result.map(carrier -> new WithConditions<>(List.of(), carrier)));
            }

            return conditionsCodec.decode(ops, conditionsDataCarrier).flatMap(conditionsCarrier -> {
                final List<ICondition> conditions = conditionsCarrier.getFirst();
                final DataResult<Pair<ICondition.IContext, T>> contextDataResult = contextCodec.decode(ops, ops.emptyMap());

                return contextDataResult.flatMap(contextCarrier -> {
                    final ICondition.IContext context = contextCarrier.getFirst();

                    final boolean conditionsMatch = conditions.stream().allMatch(c -> c.test(context));
                    if (!conditionsMatch)
                        return DataResult.error(() -> "Conditions did not match", Optional.empty());

                    return decodeInner(ops, input).map(result -> result.map(carrier -> new WithConditions<>(conditions, carrier)));
                });
            });
        }

        /**
         * Decode the inner object (ignoring the conditions).
         */
        private <T> DataResult<Optional<A>> decodeInner(DynamicOps<T> ops, MapLike<T> input) {
            if (innerCodec instanceof MapCodec.MapCodecCodec<A> innerMap) {
                // Also a MapCodec -> decode from the input directly.
                return innerMap.codec().decode(ops, input).map(Optional::of);
            } else {
                // Else copy the input into a new map and decode from that.
                return innerCodec.parse(ops, ops.createMap(input.entries())).map(Optional::of);
            }
        }
    }
}
