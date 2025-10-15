/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import static net.neoforged.neoforge.unittest.transfer.HandlerTestUtil.describeSlots;
import static net.neoforged.neoforge.unittest.transfer.HandlerTestUtil.describeStacks;
import static net.neoforged.neoforge.unittest.transfer.HandlerTestUtil.stack;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CombinedResourceHandlerTest {
    static final List<HandlerTestUtil.SlotInfo<TestResource>> EXPECTED_CONTENT = List.of(
            new HandlerTestUtil.SlotInfo<>(TestResource.OTHER_1, 1, 101),
            new HandlerTestUtil.SlotInfo<>(TestResource.OTHER_2, 2, 102),
            new HandlerTestUtil.SlotInfo<>(TestResource.OTHER_3, 3, 201),
            new HandlerTestUtil.SlotInfo<>(TestResource.OTHER_4, 4, 202));

    private MockResourceHandler firstHandler;
    private MockResourceHandler secondHandler;
    private CombinedResourceHandler<TestResource> combinedHandler;
    private HandlerAndIndex[] underlyingSlots;

    @BeforeEach
    public void setUp() {
        // Give every index unique properties.
        firstHandler = new MockResourceHandler(2);
        firstHandler.set(0, TestResource.OTHER_1, 1);
        firstHandler.setCapacity(0, 101);
        firstHandler.setValid(0, TestResource.OTHER_1);

        firstHandler.set(1, TestResource.OTHER_2, 2);
        firstHandler.setCapacity(1, 102);
        firstHandler.setValid(1, TestResource.OTHER_2);

        secondHandler = new MockResourceHandler(2);
        secondHandler.set(0, TestResource.OTHER_3, 3);
        secondHandler.setCapacity(0, 201);
        secondHandler.setValid(0, TestResource.OTHER_3);

        secondHandler.set(1, TestResource.OTHER_4, 4);
        secondHandler.setCapacity(1, 202);
        secondHandler.setValid(1, TestResource.OTHER_4);

        combinedHandler = new CombinedResourceHandler<>(firstHandler, EmptyResourceHandler.instance(), secondHandler);

        underlyingSlots = new HandlerAndIndex[] {
                new HandlerAndIndex(firstHandler, 0),
                new HandlerAndIndex(firstHandler, 1),
                new HandlerAndIndex(secondHandler, 0),
                new HandlerAndIndex(secondHandler, 1),
        };
    }

    @Test
    public void testSize() {
        assertEquals(4, combinedHandler.size());
    }

    @Test
    public void testEmptyCombinedHandler() {
        var emptyCombined = new CombinedResourceHandler<TestResource>();
        assertEquals(0, emptyCombined.size());
    }

    @Test
    public void testResourceAmountAndCapacity() {
        assertThat(describeSlots(combinedHandler)).containsExactlyElementsOf(EXPECTED_CONTENT);
    }

    @Test
    public void testIsValid() {
        for (var i = 0; i < 4; i++) {
            var expectedValid = TestResource.otherFromIndex(i);
            assertTrue(combinedHandler.isValid(i, expectedValid), "Slot " + i + " should accept " + expectedValid);
            // Check for all others to be invalid to avoid any false positives when the method just returns true
            for (var j = 0; j < 4; j++) {
                var otherResource = TestResource.otherFromIndex(j);
                if (otherResource != expectedValid) {
                    assertFalse(combinedHandler.isValid(i, otherResource), "Slot " + i + " should not accept " + otherResource);
                }
            }
        }
    }

    @Test
    public void testInsertAtIndex() {
        for (int i = 0; i < combinedHandler.size(); i++) {
            var handlerAndIndex = underlyingSlots[i];
            var underlyingHandler = handlerAndIndex.handler;
            var underlyingIndex = handlerAndIndex.index;
            var resource = TestResource.otherFromIndex(i);
            var capacity = underlyingHandler.getCapacityAsLong(underlyingIndex, resource);
            var remainingCapacity = capacity - underlyingHandler.getAmountAsLong(underlyingIndex);

            // Build list of expected content after insertion. Only the underlying slot should be changed.
            var expectedContent = new ArrayList<>(EXPECTED_CONTENT);
            expectedContent.set(
                    i,
                    new HandlerTestUtil.SlotInfo<>(resource, capacity, capacity));

            try (var tx = Transaction.openRoot()) {
                int inserted = combinedHandler.insert(i, resource, (int) remainingCapacity, tx);
                assertEquals(remainingCapacity, inserted);
                assertThat(describeSlots(combinedHandler)).containsExactlyElementsOf(expectedContent);
            }
        }
    }

    @Test
    public void testInsertWithoutIndex() {
        // Fill all slots with the same resource to test the spillover behavior
        for (var underlyingSlot : underlyingSlots) {
            var handler = (MockResourceHandler) underlyingSlot.handler;
            handler.set(underlyingSlot.index, TestResource.EMPTY, 0);
            handler.setCapacity(underlyingSlot.index, 1);
            handler.setAllValid(underlyingSlot.index);
        }

        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = combinedHandler.insert(TestResource.SOME, Integer.MAX_VALUE, transaction);
            assertEquals(combinedHandler.size(), inserted);

            assertThat(describeStacks(combinedHandler)).containsExactly(
                    stack(TestResource.SOME, 1),
                    stack(TestResource.SOME, 1),
                    stack(TestResource.SOME, 1),
                    stack(TestResource.SOME, 1));
        }
    }

    @Test
    public void testExtractAtIndex() {
        for (int i = 0; i < underlyingSlots.length; i++) {
            var expectedResource = EXPECTED_CONTENT.get(i).resource();
            var expectedAmount = EXPECTED_CONTENT.get(i).amount();

            var expectedContent = new ArrayList<>(EXPECTED_CONTENT);
            expectedContent.set(i, null);

            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = combinedHandler.extract(i, expectedResource, Integer.MAX_VALUE, transaction);
                assertEquals(expectedAmount, extracted);
                assertThat(describeSlots(combinedHandler)).containsExactlyElementsOf(expectedContent);
            }
        }
    }

    @Test
    public void testExtractWithoutIndex() {
        // Fill all slots with the same resource to test extraction across slots
        for (var underlyingSlot : underlyingSlots) {
            var handler = (MockResourceHandler) underlyingSlot.handler;
            handler.set(underlyingSlot.index, TestResource.SOME, 1);
        }

        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = combinedHandler.extract(TestResource.SOME, Integer.MAX_VALUE, transaction);
            assertEquals(4, extracted);

            assertThat(describeSlots(combinedHandler)).containsOnlyNulls();
        }
    }

    @Test
    public void testPartialExtract() {
        firstHandler.set(1, TestResource.SOME, 30);
        secondHandler.set(0, TestResource.SOME, 20);

        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = combinedHandler.extract(TestResource.SOME, 100, transaction);
            assertEquals(50, extracted);
            assertEquals(0, firstHandler.getAmountAsLong(1));
            assertEquals(0, secondHandler.getAmountAsLong(0));

            transaction.commit();
        }
    }

    @Test
    public void testOutOfBoundsAccess() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            combinedHandler.getResource(-1);
        });

        assertThrows(IndexOutOfBoundsException.class, () -> {
            combinedHandler.getResource(combinedHandler.size());
        });

        assertThrows(IndexOutOfBoundsException.class, () -> {
            combinedHandler.getAmountAsLong(combinedHandler.size());
        });

        assertThrows(IndexOutOfBoundsException.class, () -> {
            combinedHandler.getCapacityAsLong(combinedHandler.size(), TestResource.SOME);
        });

        assertThrows(IndexOutOfBoundsException.class, () -> {
            combinedHandler.isValid(combinedHandler.size(), TestResource.SOME);
        });

        assertThrows(IndexOutOfBoundsException.class, () -> {
            try (Transaction tx = Transaction.openRoot()) {
                combinedHandler.insert(combinedHandler.size(), TestResource.SOME, 1, tx);
            }
        });

        assertThrows(IndexOutOfBoundsException.class, () -> {
            try (Transaction tx = Transaction.openRoot()) {
                combinedHandler.extract(combinedHandler.size(), TestResource.SOME, 1, tx);
            }
        });
    }

    record HandlerAndIndex(ResourceHandler<TestResource> handler, int index) {}
}
