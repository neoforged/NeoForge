/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class EmptyResourceHandlerTest {
    ResourceHandler<TestResource> handler = EmptyResourceHandler.instance();

    @Test
    void testSizeShouldBeZero() {
        assertEquals(0, handler.size());
    }

    @Test
    void testThrowsOnExtract() {
        emptyHandlerThrow(() -> {
            try (var transaction = Transaction.openRoot()) {
                handler.extract(0, TestResource.SOME, 1, transaction);
            }
        });
    }

    @Test
    void testThrowsOnInsert() {
        emptyHandlerThrow(() -> {
            try (var transaction = Transaction.openRoot()) {
                handler.insert(0, TestResource.SOME, 1, transaction);
            }
        });
    }

    @Test
    void testThrowsOnIsValid() {
        emptyHandlerThrow(() -> handler.isValid(0, TestResource.SOME));
    }

    @Test
    void testThrowsOnGetResource() {
        emptyHandlerThrow(() -> handler.getResource(0));
    }

    @Test
    void testThrowsOnGetAmountAsLong() {
        emptyHandlerThrow(() -> handler.getAmountAsLong(0));
    }

    @Test
    void testThrowsOnGetCapacityAsLong() {
        emptyHandlerThrow(() -> handler.getCapacityAsLong(0, TestResource.EMPTY));
    }

    @Test
    void testThrowsOnGetAmountAsInt() {
        emptyHandlerThrow(() -> handler.getAmountAsInt(0));
    }

    @Test
    void testThrowsOnGetCapacityAsInt() {
        emptyHandlerThrow(() -> handler.getCapacityAsInt(0, TestResource.EMPTY));
    }

    @Test
    void testExtractWithoutIndexIsANoop() {
        try (var transaction = Transaction.openRoot()) {
            int inserted = handler.insert(TestResource.SOME, 100, transaction);
            Assertions.assertThat(0).isEqualTo(inserted);
        }
    }

    @Test
    void testInsertWithoutIndexIsANoop() {
        try (var transaction = Transaction.openRoot()) {
            int extracted = handler.extract(TestResource.SOME, 100, transaction);
            Assertions.assertThat(0).isEqualTo(extracted);
        }
    }

    private static void emptyHandlerThrow(Executable callable) {
        assertThrows(IndexOutOfBoundsException.class, callable, "Empty handlers should throw when using index methods");
    }
}
