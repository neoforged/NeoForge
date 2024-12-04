/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.stream.IntStream;
import net.neoforged.neoforge.common.util.InsertableLinkedOpenCustomHashSet;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InsertableLinkedOpenCustomHashSetTest {
    private static final Hash.Strategy<? super String> CUSTOM_STRATEGY = new Hash.Strategy<>() {
        @Override
        public int hashCode(String o) {
            return Character.hashCode(o.charAt(0));
        }

        @Override
        public boolean equals(@Nullable String a, @Nullable String b) {
            return a == null ? b == null : b != null && a.charAt(0) == b.charAt(0);
        }
    };

    @Test
    public void testAddAfter() {
        var test = new InsertableLinkedOpenCustomHashSet<Integer>();
        IntStream.rangeClosed(0, 10).forEach(test::add);
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), test);
        test.addAfter(4, 100);
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of(0, 1, 2, 3, 4, 100, 5, 6, 7, 8, 9, 10), test);
        test.addAfter(100, 101);
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of(0, 1, 2, 3, 4, 100, 101, 5, 6, 7, 8, 9, 10), test);
        test.addAfter(10, 102);
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of(0, 1, 2, 3, 4, 100, 101, 5, 6, 7, 8, 9, 10, 102), test);
        test.addAfter(-1, 103);
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of(0, 1, 2, 3, 4, 100, 101, 5, 6, 7, 8, 9, 10, 102, 103), test);
        Assertions.assertFalse(test.addAfter(3, 103), "Set was mutated, despite the element being present");
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of(102, 0, 1, 2, 3, 101, 100, 4, 5, 6, 7, 8, 9, 10, 103), test);
    }

    @Test
    public void testAddBefore() {
        var test = new InsertableLinkedOpenCustomHashSet<Integer>();
        IntStream.rangeClosed(0, 10).forEach(test::add);
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), test);
        test.addBefore(4, 100);
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of(0, 1, 2, 3, 100, 4, 5, 6, 7, 8, 9, 10), test);
        test.addBefore(100, 101);
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of(0, 1, 2, 3, 101, 100, 4, 5, 6, 7, 8, 9, 10), test);
        test.addBefore(0, 102);
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of(102, 0, 1, 2, 3, 101, 100, 4, 5, 6, 7, 8, 9, 10), test);
        test.addBefore(-1, 103);
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of(102, 0, 1, 2, 3, 101, 100, 4, 5, 6, 7, 8, 9, 10, 103), test);
        Assertions.assertFalse(test.addBefore(3, 103), "Set was mutated, despite the element being present");
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of(102, 0, 1, 2, 3, 101, 100, 4, 5, 6, 7, 8, 9, 10, 103), test);
    }

    @Test
    public void testAddAfterCustomStrategy() {
        var test = new InsertableLinkedOpenCustomHashSet<String>(CUSTOM_STRATEGY);
        test.add("here");
        test.add("is");
        test.add("a");
        test.add("test");
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of("here", "is", "a", "test"), test);
        test.addAfter("a", "b");
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of("here", "is", "a", "b", "test"), test);
        test.addAfter("b", "c");
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of("here", "is", "a", "b", "c", "test"), test);
        test.addAfter("test", "102");
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of("here", "is", "a", "b", "c", "test", "102"), test);
        test.addAfter("doesn't exist", "203");
        Assertions.assertEquals(ObjectLinkedOpenHashSet.of("here", "is", "a", "b", "c", "test", "102", "203"), test);
    }
}
