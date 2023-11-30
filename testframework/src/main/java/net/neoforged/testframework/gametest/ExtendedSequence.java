/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.gametest;

import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestSequence;

import java.util.function.Supplier;

public class ExtendedSequence extends GameTestSequence {
    private final GameTestInfo gameTestInfo;
    public ExtendedSequence(GameTestInfo p_177542_) {
        super(p_177542_);
        this.gameTestInfo = p_177542_;
    }

    @Override
    public ExtendedSequence thenWaitUntil(Runnable p_177553_) {
        return (ExtendedSequence) super.thenWaitUntil(p_177553_);
    }

    @Override
    public ExtendedSequence thenWaitUntil(long p_177550_, Runnable p_177551_) {
        return (ExtendedSequence) super.thenWaitUntil(p_177550_, p_177551_);
    }

    @Override
    public ExtendedSequence thenIdle(int p_177545_) {
        return (ExtendedSequence) super.thenIdle(p_177545_);
    }

    @Override
    public ExtendedSequence thenExecute(Runnable p_177563_) {
        return (ExtendedSequence) super.thenExecute(p_177563_);
    }

    @Override
    public ExtendedSequence thenExecuteAfter(int p_177547_, Runnable p_177548_) {
        return (ExtendedSequence) super.thenExecuteAfter(p_177547_, p_177548_);
    }

    @Override
    public ExtendedSequence thenExecuteFor(int p_177560_, Runnable p_177561_) {
        return (ExtendedSequence) super.thenExecuteFor(p_177560_, p_177561_);
    }

    public <T> ParametrizedGameTestSequence<T> thenMap(Supplier<T> value) {
        return new ParametrizedGameTestSequence<>(gameTestInfo, this, value);
    }

    public ExtendedSequence thenSequence(java.util.function.Consumer<ExtendedSequence> consumer) {
        final var sq = new ExtendedSequence(gameTestInfo);
        gameTestInfo.sequences.add(sq);
        consumer.accept(sq);
        return sq;
    }
}
