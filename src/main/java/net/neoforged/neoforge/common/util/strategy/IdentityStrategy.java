/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util.strategy;

import it.unimi.dsi.fastutil.Hash;

/**
 * A strategy that uses {@link System#identityHashCode(Object)} and {@code a == b} comparisons.
 */
public class IdentityStrategy implements Hash.Strategy<Object> {
    public static final Hash.Strategy<? super Object> IDENTITY = new IdentityStrategy();

    private IdentityStrategy() {}

    @Override
    public int hashCode(Object o) {
        return System.identityHashCode(o);
    }

    @Override
    public boolean equals(Object a, Object b) {
        return a == b;
    }
}
