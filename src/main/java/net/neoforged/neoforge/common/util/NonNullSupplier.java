/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import java.util.function.Supplier;

/**
 * Equivalent to {@link Supplier}, except with nonnull contract.
 *
 * @see Supplier
 * @deprecated Use {@link Supplier}
 */
@FunctionalInterface
@Deprecated
public interface NonNullSupplier<T> {
    T get();
}
