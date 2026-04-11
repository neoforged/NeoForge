/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/// A memoized proxy wrapping a function, with a clearable cache of values.
///
/// Once the function is called with a given key, the returned value is cached and reused until the cache is cleared.
///
/// @see net.minecraft.util.Util#memoize(Function)
public class MemoizedFunction<T, R> implements Function<T, R> {
    /// Wraps and memoizes the given function.
    ///
    /// @param function the function to be wrapped
    /// @return the memoized function
    public static <T, R> MemoizedFunction<T, R> of(Function<T, R> function) {
        return new MemoizedFunction<>(function, new ConcurrentHashMap<>());
    }

    protected final Function<T, R> function;
    protected final Map<T, R> cache;

    protected MemoizedFunction(Function<T, R> function, Map<T, R> cache) {
        this.function = function;
        this.cache = cache;
    }

    /// {@inheritDoc}
    @Override
    public R apply(T t) {
        return cache.computeIfAbsent(t, function);
    }

    /// {@inheritDoc}
    @Override
    public String toString() {
        return "memoize/1[function=" + function + ", size=" + this.cache.size() + "]";
    }

    /// Clears the cache of this memoized function.
    public void clear() {
        this.cache.clear();
    }
}
