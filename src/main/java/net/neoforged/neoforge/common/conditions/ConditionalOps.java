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

public class ConditionalOps<T> extends RegistryOps<T> {

    public static <T> ConditionalOps<T> create(RegistryOps<T> ops, ICondition.IContext context) {
        return new ConditionalOps<T>(ops, context);
    }

    private final ICondition.IContext context;

    private ConditionalOps(RegistryOps<T> ops, ICondition.IContext context) {
        super(ops);
        this.context = context;
    }

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

    public static <T> Decoder<Optional<T>> createConditionalDecoder(final Decoder<T> ownerCodec) {
        return createConditionalDecoder(ownerCodec, "conditions");
    }

    public static <T> Decoder<Optional<T>> createConditionalDecoder(final Decoder<T> ownerCodec, String conditionalsKey) {
        return new ConditionalDecoder<T>(
                conditionalsKey,
                ICondition.LIST_CODEC,
                retrieveContext().codec(),
                ownerCodec).map(o -> o.map(WithConditions::carrier));
    }

    public static <T> MapCodec<Optional<WithConditions<T>>> createConditionalCodecWithConditions(final Codec<T> ownerCodec) {
        return createConditionalCodecWithConditions(ownerCodec, "conditions");
    }

    public static <T> MapCodec<Optional<WithConditions<T>>> createConditionalCodecWithConditions(final Codec<T> ownerCodec, String conditionalsKey) {
        return new ConditionalCodec<T>(
                conditionalsKey,
                ICondition.LIST_CODEC,
                retrieveContext().codec(),
                ownerCodec);
    }

    private static final class ConditionalCodec<A> extends MapCodec<Optional<WithConditions<A>>> {

        private final String conditionalsPropertyKey;
        public final Codec<List<ICondition>> conditionsCodec;
        private final Codec<ICondition.IContext> contextCodec;
        private final Codec<A> innerCodec;
        private final String valuePropertyKey = "value";

        private ConditionalCodec(String conditionalsPropertyKey, Codec<List<ICondition>> conditionsCodec, Codec<ICondition.IContext> contextCodec, Codec<A> innerCodec) {
            this.conditionalsPropertyKey = conditionalsPropertyKey;
            this.conditionsCodec = conditionsCodec;
            this.contextCodec = contextCodec;
            this.innerCodec = innerCodec;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(conditionalsPropertyKey, valuePropertyKey).map(ops::createString);
        }

        @Override
        public <T> DataResult<Optional<WithConditions<A>>> decode(DynamicOps<T> ops, MapLike<T> input) {
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

        private <T> DataResult<Optional<A>> decodeInner(DynamicOps<T> ops, MapLike<T> input) {
            if (ops.compressMaps()) {
                final T value = input.get(ops.createString(valuePropertyKey));
                if (value == null) {
                    return DataResult.error(() -> "Input does not have a \"value\" entry: " + input);
                }
                return innerCodec.parse(ops, value).map(Optional::of);
            }
            if (innerCodec instanceof MapCodecCodec<?>) {
                return ((MapCodecCodec<? extends A>) innerCodec).codec().decode(ops, input).map(Optional::of);
            }
            return innerCodec.decode(ops, input.get(valuePropertyKey)).map(Pair::getFirst).map(Optional::of);
        }

        @Override
        public <T> RecordBuilder<T> encode(Optional<WithConditions<A>> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            if (input.isEmpty()) {
                return prefix;
            }

            final WithConditions<A> withConditions = input.get();

            if (ops.compressMaps()) {
                if (!withConditions.conditions().isEmpty()) {
                    prefix.add(conditionalsPropertyKey, conditionsCodec.encodeStart(ops, withConditions.conditions()));
                }

                return prefix.add(valuePropertyKey, innerCodec.encodeStart(ops, withConditions.carrier()));
            }
            if (innerCodec instanceof MapCodecCodec<?>) {
                final RecordBuilder<T> prefixWithInner = ((MapCodecCodec<A>) innerCodec).codec().encode(withConditions.carrier(), ops, prefix);

                if (!withConditions.conditions().isEmpty()) {
                    return prefixWithInner.add(conditionalsPropertyKey, conditionsCodec.encodeStart(ops, withConditions.conditions()));
                }

                return prefixWithInner;
            }

            final DataResult<T> result = innerCodec.encodeStart(ops, withConditions.carrier());
            if (!withConditions.conditions().isEmpty()) {
                prefix.add(conditionalsPropertyKey, conditionsCodec.encodeStart(ops, withConditions.conditions()));
            }
            prefix.add(valuePropertyKey, result);
            return prefix;
        }
    }

    private static final class ConditionalDecoder<A> implements Decoder<Optional<WithConditions<A>>> {

        private final String conditionalsPropertyKey;
        public final Codec<List<ICondition>> conditionsCodec;
        private final Codec<ICondition.IContext> contextCodec;
        private final Decoder<A> innerCodec;
        private final String valuePropertyKey = "value";

        private ConditionalDecoder(String conditionalsPropertyKey, Codec<List<ICondition>> conditionsCodec, Codec<ICondition.IContext> contextCodec, Decoder<A> innerCodec) {
            this.conditionalsPropertyKey = conditionalsPropertyKey;
            this.conditionsCodec = conditionsCodec;
            this.contextCodec = contextCodec;
            this.innerCodec = innerCodec;
        }

        @Override
        public <T> DataResult<Pair<Optional<WithConditions<A>>, T>> decode(DynamicOps<T> ops, T input) {
            final DataResult<T> conditionsDataCarrierResult = ops.get(input, conditionalsPropertyKey);

            if (conditionsDataCarrierResult.error().isPresent()) {
                return decodeInner(ops, input).map(result -> result.map(carrier -> new WithConditions<>(List.of(), carrier))).map(v -> Pair.of(v, input));
            }

            return conditionsDataCarrierResult.flatMap(conditionsDataCarrier -> conditionsCodec.decode(ops, conditionsDataCarrier).flatMap(conditionsCarrier -> {
                final List<ICondition> conditions = conditionsCarrier.getFirst();
                final DataResult<Pair<ICondition.IContext, T>> contextDataResult = contextCodec.decode(ops, ops.emptyMap());

                return contextDataResult.flatMap(contextCarrier -> {
                    final ICondition.IContext context = contextCarrier.getFirst();

                    final boolean conditionsMatch = conditions.stream().allMatch(c -> c.test(context));
                    if (!conditionsMatch)
                        return DataResult.error(() -> "Conditions did not match", Optional.<WithConditions<A>>empty());

                    return decodeInner(ops, input).map(result -> result.map(carrier -> new WithConditions<>(conditions, carrier)));
                });
            })).map(v -> Pair.of(v, input));
        }

        private <T> DataResult<Optional<A>> decodeInner(DynamicOps<T> ops, T input) {
            if (ops.compressMaps()) {
                final DataResult<T> valueResult = ops.get(input, valuePropertyKey);
                if (valueResult.error().map(DataResult.PartialResult::message).isPresent()) {
                    return DataResult.error(() -> "Input does not have a \"value\" entry: " + input);
                }
                return valueResult.flatMap(value -> innerCodec.parse(ops, value).map(Optional::of));
            }
            if (innerCodec instanceof MapCodec.MapCodecCodec<A> mapCodec) {
                return mapCodec.decode(ops, input).map(Pair::getFirst).map(Optional::of);
            }

            final DataResult<T> valueDataCarrierResult = ops.get(input, valuePropertyKey);

            if (valueDataCarrierResult.error().isPresent()) {
                return innerCodec.decode(ops, input).map(Pair::getFirst).map(Optional::of);
            }

            return valueDataCarrierResult.flatMap(valueDataCarrier -> innerCodec.decode(ops, valueDataCarrier).map(Pair::getFirst).map(Optional::of));
        }

        @Override
        public String toString() {
            return "Conditional[inner=" + innerCodec + ", conditionalsKey=" + conditionalsPropertyKey + "]";
        }
    }
}
