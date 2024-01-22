/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import java.util.function.Predicate;

/**
 * Equivalent to {@link Predicate}, except with nonnull contract.
 *
 * @see Predicate
 * @deprecated Use {@link Predicate}
 */
@FunctionalInterface
@Deprecated
public interface NonNullPredicate<T> {
    boolean test(T t);
}
