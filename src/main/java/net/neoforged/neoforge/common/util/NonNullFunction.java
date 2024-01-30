/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import java.util.function.Function;

/**
 * Equivalent to {@link Function}, except with nonnull contract.
 *
 * @see Function
 * @deprecated Use {@link Function}
 */
@Deprecated
@FunctionalInterface
public interface NonNullFunction<T, R> {
    R apply(T t);
}
