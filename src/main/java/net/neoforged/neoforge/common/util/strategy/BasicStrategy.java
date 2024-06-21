/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util.strategy;

import it.unimi.dsi.fastutil.Hash;
import java.util.Objects;

public class BasicStrategy implements Hash.Strategy<Object> {
    @Override
    public int hashCode(Object o) {
        return Objects.hashCode(o);
    }

    @Override
    public boolean equals(Object a, Object b) {
        return Objects.equals(a, b);
    }
}
