/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import java.util.stream.IntStream;
import net.neoforged.neoforge.common.util.InsertableLinkedOpenCustomHashSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InsertableLinkedOpenCustomHashSetTest {
    @Test
    public void testAddAfter() {
        InsertableLinkedOpenCustomHashSet<Integer> test = new InsertableLinkedOpenCustomHashSet<>();
        IntStream.rangeClosed(0, 10).forEach(test::add);
        Assertions.assertArrayEquals(test.toArray(), new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
        test.addAfter(4, 100);
        Assertions.assertArrayEquals(test.toArray(), new Integer[] { 0, 1, 2, 3, 4, 100, 5, 6, 7, 8, 9, 10 });
        test.addAfter(100, 101);
        Assertions.assertArrayEquals(test.toArray(), new Integer[] { 0, 1, 2, 3, 4, 100, 101, 5, 6, 7, 8, 9, 10 });
        test.addAfter(10, 102);
        Assertions.assertArrayEquals(test.toArray(), new Integer[] { 0, 1, 2, 3, 4, 100, 101, 5, 6, 7, 8, 9, 10, 102 });
        test.addAfter(-1, 103);
        Assertions.assertArrayEquals(test.toArray(), new Integer[] { 0, 1, 2, 3, 4, 100, 101, 5, 6, 7, 8, 9, 10, 102, 103 });
    }

    @Test
    public void testAddBefore() {
        InsertableLinkedOpenCustomHashSet<Integer> test = new InsertableLinkedOpenCustomHashSet<>();
        IntStream.rangeClosed(0, 10).forEach(test::add);
        Assertions.assertArrayEquals(test.toArray(), new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
        test.addBefore(4, 100);
        Assertions.assertArrayEquals(test.toArray(), new Integer[] { 0, 1, 2, 3, 100, 4, 5, 6, 7, 8, 9, 10 });
        test.addBefore(100, 101);
        Assertions.assertArrayEquals(test.toArray(), new Integer[] { 0, 1, 2, 3, 101, 100, 4, 5, 6, 7, 8, 9, 10 });
        test.addBefore(0, 102);
        Assertions.assertArrayEquals(test.toArray(), new Integer[] { 102, 0, 1, 2, 3, 101, 100, 4, 5, 6, 7, 8, 9, 10 });
        test.addBefore(-1, 103);
        Assertions.assertArrayEquals(test.toArray(), new Integer[] { 102, 0, 1, 2, 3, 101, 100, 4, 5, 6, 7, 8, 9, 10, 103 });
    }
}
