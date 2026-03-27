/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.gametest;

import java.util.function.Supplier;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;

public class ExtendedSequence extends GameTestSequence {
    private final GameTestHelper gameTestHelper;

    public ExtendedSequence(GameTestHelper gameTestHelper) {
        super(gameTestHelper.testInfo);
        this.gameTestHelper = gameTestHelper;
    }

    @Override
    public ExtendedSequence thenWaitUntil(Runnable assertion) {
        return (ExtendedSequence) super.thenWaitUntil(assertion);
    }

    @Override
    public ExtendedSequence thenWaitUntil(long expectedDelay, Runnable assertion) {
        return (ExtendedSequence) super.thenWaitUntil(expectedDelay, assertion);
    }

    @Override
    public ExtendedSequence thenIdle(int delta) {
        return (ExtendedSequence) super.thenIdle(delta);
    }

    @Override
    public ExtendedSequence thenExecute(Runnable assertion) {
        return (ExtendedSequence) super.thenExecute(assertion);
    }

    @Override
    public ExtendedSequence thenExecuteAfter(int delta, Runnable after) {
        return (ExtendedSequence) super.thenExecuteAfter(delta, after);
    }

    @Override
    public ExtendedSequence thenExecuteFor(int delta, Runnable check) {
        return (ExtendedSequence) super.thenExecuteFor(delta, check);
    }

    public <T> ParametrizedGameTestSequence<T> thenMap(Supplier<T> value) {
        return new ParametrizedGameTestSequence<>(gameTestHelper, this, value);
    }

    public ExtendedSequence thenSequence(java.util.function.Consumer<ExtendedSequence> consumer) {
        final var sq = new ExtendedSequence(gameTestHelper);
        gameTestHelper.testInfo.sequences.add(sq);
        consumer.accept(sq);
        return sq;
    }
}
