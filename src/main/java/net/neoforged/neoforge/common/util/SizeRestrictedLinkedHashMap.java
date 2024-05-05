package net.neoforged.neoforge.common.util;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;

/**
 * A special kind of Long2ObjectLinkedOpenHashMap that will remove the oldest values
 * automatically if it is given new values that exceeds its given maxCapacity
 */
public class SizeRestrictedLinkedHashMap<V> extends Long2ObjectLinkedOpenHashMap<V> {

    private final long maxCapacity;

    /**
     * Creates a new size-restricted hash map with initial expected {@link Hash#DEFAULT_INITIAL_SIZE} entries and
     * {@link Hash#DEFAULT_LOAD_FACTOR} as load factor.
     *
     * @param maxCapacity Maximum size before this hash map begins removing the oldest entries
     */
    public SizeRestrictedLinkedHashMap(long maxCapacity) {
        super(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);

        this.maxCapacity = maxCapacity;
    }

    /**
     * Creates a new size-restricted hash map with given initial size, load factors, and max capacity.
     *
     * @param maxCapacity Maximum size before this hash map begins removing the oldest entries
     * @param defaultInitialSize The expected ideal number of elements in the hash map
     * @param defaultLoadFactor The load factor for determining when to rehash
     */
    public SizeRestrictedLinkedHashMap(long maxCapacity, int defaultInitialSize, int defaultLoadFactor) {
        super(defaultInitialSize, defaultLoadFactor);

        this.maxCapacity = maxCapacity;
    }

    @Override
    public V put(final long k, final V v) {
        // Removes oldest entry to keep under maxCapacity.
        if (size() + 1 > maxCapacity && find(k) <= 0) {
            removeFirst();
        }
        return super.put(k, v);
    }

    @Override
    public V putIfAbsent(final long k, final V v) {
        // Removes oldest entry to keep under maxCapacity.
        if (size() + 1 > maxCapacity) {
            removeFirst();
        }
        return super.putIfAbsent(k, v);
    }

    @Override
    public V compute(final long k, final java.util.function.BiFunction<? super Long, ? super V, ? extends V> remappingFunction) {
        // Removes oldest entry to keep under maxCapacity.
        if (size() + 1 > maxCapacity && find(k) <= 0) {
            removeFirst();
        }
        return super.compute(k, remappingFunction);
    }

    @Override
    public V computeIfAbsent(final long k, final java.util.function.LongFunction<? extends V> mappingFunction) {
        // Removes oldest entry to keep under maxCapacity.
        if (size() + 1 > maxCapacity) {
            removeFirst();
        }
        return super.computeIfAbsent(k, mappingFunction);
    }

    @Override
    public V merge(final long k, final V v, final java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        // Removes oldest entry to keep under maxCapacity.
        if (size() + 1 > maxCapacity) {
            removeFirst();
        }
        return super.merge(k, v, remappingFunction);
    }

    private int find(final long k) {
        if (((k) == (0))) return containsNullKey ? n : -(n + 1);
        long curr;
        final long[] key = this.key;
        int pos;
        // The starting point.
        if (((curr = key[pos = (int)it.unimi.dsi.fastutil.HashCommon.mix((k)) & mask]) == (0))) return -(pos + 1);
        if (((k) == (curr))) return pos;
        // There's always an unused entry.
        while (true) {
            if (((curr = key[pos = (pos + 1) & mask]) == (0))) return -(pos + 1);
            if (((k) == (curr))) return pos;
        }
    }
}
