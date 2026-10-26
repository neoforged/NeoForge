/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.obj;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceArray;

// Array pool based on the implementation used in .NET
public final class ArrayPool {
    private static final int NUMBER_OF_BUCKETS = 27;
    private static final char[] EMPTY = new char[] {};

    private static final ThreadLocal<char[][]> _threadLocalArrays = new ThreadLocal<>();
    private static final AtomicReferenceArray<Partitions> _buckets = new AtomicReferenceArray<>(NUMBER_OF_BUCKETS);

    public static final ArrayPool SHARED = new ArrayPool();

    private ArrayPool() {}

    private static int selectBucketIndex(int minimumLength) {
        var log2 = 31 ^ Integer.numberOfLeadingZeros(minimumLength - 1 | 15);
        return log2 - 3;
    }

    public char[] rentArray(int minimumLength) {
        var bucketIndex = selectBucketIndex(minimumLength);
        var threadLocalBuckets = _threadLocalArrays.get();
        if (threadLocalBuckets != null && bucketIndex < threadLocalBuckets.length) {
            var array = threadLocalBuckets[bucketIndex];
            if (array != null) {
                threadLocalBuckets[bucketIndex] = null;
                return array;
            }
        }

        var buckets = _buckets;
        if (bucketIndex < buckets.length()) {
            var bucket = buckets.get(bucketIndex);
            if (bucket != null) {
                var array = bucket.tryPop();
                if (array != null) return array;
            }

            minimumLength = 16 << bucketIndex;
        } else if (minimumLength == 0) {
            return EMPTY;
        } else {
            throw new IllegalArgumentException("minimumLength must be greater than 0");
        }

        return new char[minimumLength];
    }

    public void returnArray(char[] array) {
        returnArray(array, false);
    }

    public void returnArray(char[] array, boolean clear) {
        if (array == null)
            throw new IllegalArgumentException("array must not be null");

        var bucketIndex = selectBucketIndex(array.length);
        var threadLocalArrays = _threadLocalArrays.get();
        if (threadLocalArrays == null) {
            threadLocalArrays = initializeThreadLocalBuckets();
        }

        if (bucketIndex < threadLocalArrays.length) {
            if (clear) {
                Arrays.fill(array, (char) 0);
            }
            if (array.length != (16 << bucketIndex)) {
                throw new IllegalArgumentException("array must be from the pool");
            }

            // Keep the most recent array in the thread-local cache for better locality.
            var previous = threadLocalArrays[bucketIndex];
            threadLocalArrays[bucketIndex] = array;
            if (previous != null) {
                var bucket = _buckets.get(bucketIndex);
                if (bucket == null) {
                    bucket = createPerCorePartitions(bucketIndex);
                }

                bucket.tryPush(previous);
            }
        }
    }

    private char[][] initializeThreadLocalBuckets() {
        var buckets = new char[NUMBER_OF_BUCKETS][];
        _threadLocalArrays.set(buckets);

        return buckets;
    }

    private Partitions createPerCorePartitions(int bucketIndex) {
        var it = new Partitions();
        var result = _buckets.compareAndExchange(bucketIndex, null, it);
        if (result == null)
            result = it;

        return result;
    }

    private static class Partitions {
        private static final int PARTITION_COUNT = Runtime.getRuntime().availableProcessors();

        private final Partition[] _partitions;

        public Partitions() {
            var partitions = new Partition[PARTITION_COUNT];
            for (int i = 0; i < partitions.length; i++) {
                partitions[i] = new Partition();
            }

            _partitions = partitions;
        }

        public boolean tryPush(char[] array) {
            var partitions = _partitions;
            // N.B. this was ProcessorId in .NET
            var index = ((int) Thread.currentThread().threadId() % PARTITION_COUNT);
            for (int i = 0; i < partitions.length; i++) {
                if (partitions[index].tryPush(array)) return true;
                if (++index == partitions.length) index = 0;
            }

            return false;
        }

        public char[] tryPop() {
            char[] array;
            var partitions = _partitions;
            // N.B. this was ProcessorId in .NET
            var index = ((int) Thread.currentThread().threadId() % PARTITION_COUNT);

            for (int i = 0; i < partitions.length; i++) {
                if ((array = partitions[index].tryPop()) != null) return array;
                if (++index == partitions.length) index = 0;
            }

            return null;
        }
    }

    private static class Partition {
        private static final int MAX_ARRAYS_PER_PARTITION = 32;

        private final char[][] _arrays = new char[MAX_ARRAYS_PER_PARTITION][];
        private int _count;

        public boolean tryPush(char[] array) {
            var enqueued = false;

            synchronized (this) {
                var arrays = _arrays;
                var count = _count;
                if (count < arrays.length) {
                    arrays[count] = array;
                    _count = count + 1;
                    enqueued = true;
                }
            }

            return enqueued;
        }

        public char[] tryPop() {
            char[] array = null;

            synchronized (this) {
                var arrays = _arrays;
                var count = _count - 1;
                if (count < arrays.length) {
                    array = arrays[count];
                    arrays[count] = null;
                    _count = count;
                }
            }

            return array;
        }
    }
}
