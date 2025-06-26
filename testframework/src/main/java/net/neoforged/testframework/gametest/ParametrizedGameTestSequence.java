/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.gametest;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.gametest.framework.GameTestException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;

public class ParametrizedGameTestSequence<T> {
    private final GameTestHelper helper;
    private final ExtendedSequence sequence;
    private final Supplier<T> value;

    public ParametrizedGameTestSequence(GameTestHelper helper, ExtendedSequence sequence, Supplier<T> value) {
        this.helper = helper;
        this.sequence = sequence;

        final AtomicReference<Throwable> capturedException = new AtomicReference<>();
        final AtomicReference<T> val = new AtomicReference<>();
        sequence.thenExecute(() -> {
            try {
                val.set(value.get());
            } catch (Throwable ex) {
                // Capture the exception to rethrow later, to avoid overwriting it with our own
                capturedException.set(ex);
                throw ex;
            }
        });
        this.value = () -> {
            final var v = val.get();
            if (v == null) {
                // Rethrow the captured exception if any before throwing our own exception
                final var ex = capturedException.get();
                if (ex != null) {
                    sneakyThrow(ex);
                }
                throw helper.assertionException("Expected value to be non-null!");
            }
            return v;
        };
    }

    public ParametrizedGameTestSequence<T> thenWaitUntil(Runnable condition) {
        sequence.thenWaitUntil(condition);
        return this;
    }

    public ParametrizedGameTestSequence<T> thenWaitUntil(Consumer<T> condition) {
        return thenWaitUntil(() -> condition.accept(value.get()));
    }

    public ParametrizedGameTestSequence<T> thenWaitUntil(long ticks, Runnable condition) {
        sequence.thenWaitUntil(ticks, condition);
        return this;
    }

    public ParametrizedGameTestSequence<T> thenWaitUntil(long ticks, Consumer<T> condition) {
        return thenWaitUntil(ticks, () -> condition.accept(value.get()));
    }

    public ParametrizedGameTestSequence<T> thenIdle(int amount) {
        return this.thenExecuteAfter(amount, () -> {});
    }

    public ParametrizedGameTestSequence<T> thenExecute(Runnable runnable) {
        sequence.thenExecute(runnable);
        return this;
    }

    public ParametrizedGameTestSequence<T> thenExecute(Consumer<T> runnable) {
        return thenExecute(() -> runnable.accept(value.get()));
    }

    public ParametrizedGameTestSequence<T> thenExecuteAfter(int ticks, Runnable runnable) {
        sequence.thenExecuteAfter(ticks, runnable);
        return this;
    }

    public ParametrizedGameTestSequence<T> thenExecuteAfter(int ticks, Consumer<T> runnable) {
        return thenExecuteAfter(ticks, () -> runnable.accept(value.get()));
    }

    public ParametrizedGameTestSequence<T> thenExecuteFor(int ticks, Runnable runnable) {
        sequence.thenExecuteFor(ticks, runnable);
        return this;
    }

    public ParametrizedGameTestSequence<T> thenExecuteFor(int ticks, Consumer<T> runnable) {
        return thenExecuteFor(ticks, () -> runnable.accept(value.get()));
    }

    public <Z> ParametrizedGameTestSequence<Z> thenMap(Function<T, Z> mapper) {
        return new ParametrizedGameTestSequence<>(helper, sequence, () -> mapper.apply(value.get()));
    }

    public <Z> ParametrizedGameTestSequence<Z> thenMapAfter(int ticks, Function<T, Z> mapper) {
        thenIdle(ticks);
        return thenMap(mapper);
    }

    public <Z> ParametrizedGameTestSequence<Z> thenMap(Supplier<Z> value) {
        return new ParametrizedGameTestSequence<>(helper, sequence, value);
    }

    public <Z> ParametrizedGameTestSequence<Z> thenMapAfter(int ticks, Supplier<Z> value) {
        thenIdle(ticks);
        return thenMap(value);
    }

    public <Z> ParametrizedGameTestSequence<Z> thenMapToSequence(BiFunction<ParametrizedGameTestSequence<T>, Supplier<T>, ParametrizedGameTestSequence<Z>> sequence) {
        final AtomicReference<Z> value = new AtomicReference<>();
        this.sequence.thenSequence(sq -> sequence.apply(new ParametrizedGameTestSequence<>(helper, sq, this.value), this.value)
                .thenExecute(value::set));
        return new ParametrizedGameTestSequence<>(helper, this.sequence, value::get);
    }

    public <Z> ParametrizedGameTestSequence<Z> thenMapToSequence(Function<Supplier<T>, ParametrizedGameTestSequence<Z>> sequence) {
        return thenMapToSequence((seq, val) -> sequence.apply(val));
    }

    public ParametrizedGameTestSequence<T> thenSequence(BiConsumer<ParametrizedGameTestSequence<T>, Supplier<T>> sequence) {
        return thenSequence((sq) -> sequence.accept(sq, value));
    }

    public ParametrizedGameTestSequence<T> thenSequence(Consumer<ParametrizedGameTestSequence<T>> sequence) {
        this.sequence.thenSequence(sq -> sequence.accept(sq.thenMap(value)));
        return this;
    }

    public void thenSucceed() {
        sequence.thenSucceed();
    }

    public void thenFail(Supplier<GameTestException> exception) {
        sequence.thenFail(exception);
    }

    public void thenFail(Function<T, GameTestException> exception) {
        thenFail(() -> exception.apply(value.get()));
    }

    public GameTestSequence.Condition thenTrigger() {
        return sequence.thenTrigger();
    }

    // Never returns normally.
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable exception) throws E {
        throw (E) exception;
    }
}
