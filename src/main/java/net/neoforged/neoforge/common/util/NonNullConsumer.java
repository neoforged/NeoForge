/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import java.util.function.Consumer;

/**
 * Equivalent to {@link Consumer}, except with nonnull contract.
 *
 * @see Consumer
 * @deprecated Use {@link Consumer}
 */
@FunctionalInterface
public interface NonNullConsumer<T> {
    void accept(T t);
}
