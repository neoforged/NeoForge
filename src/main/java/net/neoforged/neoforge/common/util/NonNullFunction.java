/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

/**
 * Equivalent to {@link Function}, except with nonnull contract.
 *
 * @see Function
 */
@FunctionalInterface
public interface NonNullFunction<T, R> {
    @NotNull
    R apply(@NotNull T t);
}
