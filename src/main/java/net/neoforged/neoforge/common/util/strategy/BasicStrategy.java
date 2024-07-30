/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util.strategy;

import it.unimi.dsi.fastutil.Hash;
import java.util.Objects;

/**
 * A strategy that uses {@link Objects#hashCode(Object)} and {@link Object#equals(Object)}.
 */
public class BasicStrategy implements Hash.Strategy<Object> {
    public static final Hash.Strategy<? super Object> BASIC = new BasicStrategy();

    private BasicStrategy() {}

    @Override
    public int hashCode(Object o) {
        return Objects.hashCode(o);
    }

    @Override
    public boolean equals(Object a, Object b) {
        return Objects.equals(a, b);
    }
}
