package net.neoforged.neoforge.gametest;

import com.google.common.base.Suppliers;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestSequence;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ParametrizedGameTestSequence<T> {
    private final GameTestInfo info;
    private final GameTestSequence sequence;
    private final Supplier<T> value;
    public ParametrizedGameTestSequence(GameTestInfo info, GameTestSequence sequence, Supplier<T> value) {
        this.info = info;
        this.sequence = sequence;

        final AtomicReference<T> val = new AtomicReference<>();
        sequence.thenExecute(() -> val.set(value.get()));
        this.value = val::get;
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
        return new ParametrizedGameTestSequence<>(info, sequence, () -> mapper.apply(value.get()));
    }

    public <Z> ParametrizedGameTestSequence<Z> thenMapAfter(int ticks, Function<T, Z> mapper) {
        thenIdle(ticks);
        return thenMap(mapper);
    }

    public <Z> ParametrizedGameTestSequence<Z> thenMap(Supplier<Z> value) {
        return new ParametrizedGameTestSequence<>(info, sequence, value);
    }

    public <Z> ParametrizedGameTestSequence<Z> thenMapAfter(int ticks, Supplier<Z> value) {
        thenIdle(ticks);
        return thenMap(value);
    }

    public <Z> ParametrizedGameTestSequence<Z> thenMapToSequence(BiFunction<ParametrizedGameTestSequence<T>, Supplier<T>, ParametrizedGameTestSequence<Z>> sequence) {
        final AtomicReference<Z> value = new AtomicReference<>();
        this.sequence.thenSequence(sq -> sequence.apply(new ParametrizedGameTestSequence<>(info, sq, this.value), this.value)
                .thenExecute(value::set));
        return new ParametrizedGameTestSequence<>(info, this.sequence, value::get);
    }

    public ParametrizedGameTestSequence<T> thenSequence(BiConsumer<ParametrizedGameTestSequence<T>, T> sequence) {
        return thenSequence((sq) -> sequence.accept(sq, value.get()));
    }

    public ParametrizedGameTestSequence<T> thenSequence(Consumer<ParametrizedGameTestSequence<T>> sequence) {
        this.sequence.thenSequence(sq -> sequence.accept(sq.thenMap(value)));
        return this;
    }

    public void thenSucceed() {
        sequence.thenSucceed();
    }

    public void thenFail(Supplier<Exception> exception) {
        sequence.thenFail(exception);
    }

    public void thenFail(Function<T, Exception> exception) {
        thenFail(() -> exception.apply(value.get()));
    }

    public GameTestSequence.Condition thenTrigger() {
        return sequence.thenTrigger();
    }
}
