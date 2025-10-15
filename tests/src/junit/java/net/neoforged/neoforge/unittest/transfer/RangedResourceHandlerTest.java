/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RangedResourceHandlerTest {
    private MockResourceHandler mockHandler;

    @BeforeEach
    public void setUp() {
        mockHandler = new MockResourceHandler(10);
    }

    @Test
    public void testRangeCreation() {
        var ranged = RangedResourceHandler.of(mockHandler, 2, 5);
        assertEquals(3, ranged.size(), "Range from 2 to 5 should have size 3");
    }

    @Test
    public void testSingleIndexCreation() {
        var ranged = RangedResourceHandler.ofSingleIndex(mockHandler, 3);
        assertEquals(1, ranged.size(), "Single index should have size 1");
    }

    @Test
    public void testInvalidRangeStartNegative() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            RangedResourceHandler.of(mockHandler, -1, 5);
        }, "Negative start index should throw exception");
    }

    @Test
    public void testInvalidRangeStartAfterEnd() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            RangedResourceHandler.of(mockHandler, 5, 2);
        }, "Start after end should throw exception");
    }

    @Test
    public void testInvalidRangeEndTooLarge() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            RangedResourceHandler.of(mockHandler, 0, 11);
        }, "End beyond handler size should throw exception");
    }

    @Test
    public void testGetResource() {
        mockHandler.set(3, TestResource.SOME, 1L);
        var ranged = RangedResourceHandler.of(mockHandler, 2, 5);

        assertEquals(TestResource.SOME, ranged.getResource(1), "Index 1 in range should map to index 3 in delegate");
        assertEquals(TestResource.EMPTY, ranged.getResource(0), "Index 0 in range should map to index 2 in delegate");
    }

    @Test
    public void testGetAmount() {
        mockHandler.set(3, TestResource.SOME, 100L);
        var ranged = RangedResourceHandler.of(mockHandler, 2, 5);

        assertEquals(100, ranged.getAmountAsLong(1), "Amount at index 1 should be 100");
        assertEquals(0, ranged.getAmountAsLong(0), "Amount at index 0 should be 0");
    }

    @Test
    public void testGetCapacity() {
        var ranged = RangedResourceHandler.of(mockHandler, 2, 5);
        mockHandler.setCapacity(2, 1000);

        assertEquals(1000, ranged.getCapacityAsLong(0, TestResource.SOME), "Capacity should be delegated correctly");
    }

    @Test
    public void testIsValid() {
        var ranged = RangedResourceHandler.of(mockHandler, 2, 5);

        assertTrue(ranged.isValid(0, TestResource.SOME), "Should delegate validity check");
    }

    @Test
    public void testInsertAtIndex() {
        var ranged = RangedResourceHandler.of(mockHandler, 2, 5);

        try (var transaction = Transaction.openRoot()) {
            var inserted = ranged.insert(1, TestResource.SOME, 50, transaction);
            assertEquals(50, inserted, "Should insert 50 units");
            assertEquals(50, mockHandler.getAmountAsLong(3), "Should be inserted at delegate index 3");
            assertEquals(TestResource.SOME, mockHandler.getResource(3), "Resource should be set at delegate index 3");
        }
    }

    @Test
    public void testPartialInsertWithoutIndex() {
        mockHandler.setCapacity(50);
        var ranged = RangedResourceHandler.of(mockHandler, 2, 4);

        try (var transaction = Transaction.openRoot()) {
            var inserted = ranged.insert(TestResource.SOME, 101, transaction);
            assertEquals(100, inserted, "Should only be able to fill two slots, which are limited to 50.");

            // Check distribution across the range
            assertEquals(50, mockHandler.getAmountAsLong(2), "First slot should be full");
            assertEquals(50, mockHandler.getAmountAsLong(3), "Second slot should be full");
        }
    }

    @Test
    public void testInsertWithoutIndex() {
        mockHandler.setCapacity(50);
        var ranged = RangedResourceHandler.of(mockHandler, 2, 4);

        try (var transaction = Transaction.openRoot()) {
            var inserted = ranged.insert(TestResource.SOME, 50, transaction);
            assertEquals(50, inserted);

            // Check distribution across the range
            assertEquals(50, mockHandler.getAmountAsLong(2), "First slot should be full");
            assertEquals(0, mockHandler.getAmountAsLong(3), "Second slot should be full");
        }
    }

    @Test
    public void testExtractAtIndex() {
        mockHandler.set(3, TestResource.SOME, 100L);
        var ranged = RangedResourceHandler.of(mockHandler, 2, 5);

        try (var transaction = Transaction.openRoot()) {
            var extracted = ranged.extract(1, TestResource.SOME, 50, transaction);
            assertEquals(50, extracted, "Should extract 50 units");
            assertEquals(50, mockHandler.getAmountAsLong(3), "Should have 50 units left at delegate index 3");
        }
    }

    @Test
    public void testExtractWithoutIndex() {
        mockHandler.set(0, TestResource.SOME, 100);
        mockHandler.set(1, TestResource.SOME, 1);
        mockHandler.set(2, TestResource.SOME, 100);
        var ranged = RangedResourceHandler.ofSingleIndex(mockHandler, 1);

        try (var transaction = Transaction.openRoot()) {
            var extracted = ranged.extract(TestResource.SOME, 180, transaction);
            assertEquals(1, extracted, "Should extract 1 unit total");
            assertEquals(0, mockHandler.getAmountAsLong(1));
        }
    }

    @Test
    public void testPartialExtractWithoutIndex() {
        mockHandler.set(1, TestResource.SOME, 2);
        var ranged = RangedResourceHandler.ofSingleIndex(mockHandler, 1);

        try (var transaction = Transaction.openRoot()) {
            var extracted = ranged.extract(TestResource.SOME, 1, transaction);
            assertEquals(1, extracted, "Should extract 1 unit total");
            assertEquals(1, mockHandler.getAmountAsLong(1));
        }
    }

    @Test
    public void testOutOfBoundsAccess() {
        var ranged = RangedResourceHandler.of(mockHandler, 2, 5);

        assertThrows(IndexOutOfBoundsException.class, () -> {
            ranged.getResource(3);
        }, "Index 3 should be out of bounds for size 3");

        assertThrows(IndexOutOfBoundsException.class, () -> {
            ranged.getResource(-1);
        }, "Negative index should be out of bounds");
    }
}
