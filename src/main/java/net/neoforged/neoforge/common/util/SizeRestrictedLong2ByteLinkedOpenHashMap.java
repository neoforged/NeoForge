/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.longs.Long2ByteFunction;
import it.unimi.dsi.fastutil.longs.Long2ByteLinkedOpenHashMap;

/**
 * A special kind of Long2ByteLinkedOpenHashMap that will remove the oldest values
 * automatically if it is given new values that exceeds its given maxCapacity
 */
public class SizeRestrictedLong2ByteLinkedOpenHashMap extends Long2ByteLinkedOpenHashMap {
    private final long maxCapacity;

    /**
     * Creates a new size-restricted hash map with initial expected {@link Hash#DEFAULT_INITIAL_SIZE} entries and
     * {@link Hash#DEFAULT_LOAD_FACTOR} as load factor.
     *
     * @param maxCapacity Maximum size before this hash map begins removing the oldest entries
     */
    public SizeRestrictedLong2ByteLinkedOpenHashMap(long maxCapacity) {
        super(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);

        this.maxCapacity = maxCapacity;
    }

    /**
     * Creates a new size-restricted hash map with given initial size, load factors, and max capacity.
     *
     * @param maxCapacity        Maximum size before this hash map begins removing the oldest entries
     * @param defaultInitialSize The expected ideal number of elements in the hash map
     * @param defaultLoadFactor  The load factor for determining when to rehash
     */
    public SizeRestrictedLong2ByteLinkedOpenHashMap(long maxCapacity, int defaultInitialSize, int defaultLoadFactor) {
        super(defaultInitialSize, defaultLoadFactor);

        this.maxCapacity = maxCapacity;
    }

    @Override
    public byte put(final long k, final byte v) {
        // Removes oldest entry to keep under maxCapacity.
        if (size() + 1 > maxCapacity && find(k) <= 0) {
            removeFirstByte();
        }
        return super.put(k, v);
    }

    @Override
    public byte putIfAbsent(final long k, final byte v) {
        // Removes oldest entry to keep under maxCapacity.
        if (size() + 1 > maxCapacity) {
            removeFirstByte();
        }
        return super.putIfAbsent(k, v);
    }

    @Override
    public byte compute(final long k, final java.util.function.BiFunction<? super Long, ? super Byte, ? extends Byte> remappingFunction) {
        // Removes oldest entry to keep under maxCapacity.
        if (size() + 1 > maxCapacity && find(k) <= 0) {
            removeFirstByte();
        }
        return super.compute(k, remappingFunction);
    }

    @Override
    public byte computeIfAbsent(final long k, final java.util.function.LongToIntFunction mappingFunction) {
        // Removes oldest entry to keep under maxCapacity.
        if (size() + 1 > maxCapacity) {
            removeFirstByte();
        }
        return super.computeIfAbsent(k, mappingFunction);
    }

    @Override
    public byte computeIfAbsent(final long key, final Long2ByteFunction mappingFunction) {
        // Removes oldest entry to keep under maxCapacity.
        if (size() + 1 > maxCapacity) {
            removeFirstByte();
        }
        return super.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public byte computeIfAbsentNullable(final long k, final java.util.function.LongFunction<? extends Byte> mappingFunction) {
        // Removes oldest entry to keep under maxCapacity.
        if (size() + 1 > maxCapacity) {
            removeFirstByte();
        }
        return super.computeIfAbsentNullable(k, mappingFunction);
    }

    @Override
    public byte merge(final long k, final byte v, final java.util.function.BiFunction<? super Byte, ? super Byte, ? extends Byte> remappingFunction) {
        // Removes oldest entry to keep under maxCapacity.
        if (size() + 1 > maxCapacity) {
            removeFirstByte();
        }
        return super.merge(k, v, remappingFunction);
    }

    private int find(final long k) {
        if (((k) == (0))) return containsNullKey ? n : -(n + 1);
        long curr;
        final long[] key = this.key;
        int pos;
        // The starting point.
        if (((curr = key[pos = (int) it.unimi.dsi.fastutil.HashCommon.mix((k)) & mask]) == (0))) return -(pos + 1);
        if (((k) == (curr))) return pos;
        // There's always an unused entry.
        while (true) {
            if (((curr = key[pos = (pos + 1) & mask]) == (0))) return -(pos + 1);
            if (((k) == (curr))) return pos;
        }
    }
}
