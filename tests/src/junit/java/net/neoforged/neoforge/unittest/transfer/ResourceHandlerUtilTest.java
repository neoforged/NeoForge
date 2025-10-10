/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import static net.neoforged.neoforge.unittest.transfer.HandlerTestUtil.EMPTY_SLOT;
import static net.neoforged.neoforge.unittest.transfer.HandlerTestUtil.EMPTY_STACK;
import static net.neoforged.neoforge.unittest.transfer.HandlerTestUtil.describeStacks;
import static net.neoforged.neoforge.unittest.transfer.HandlerTestUtil.handler;
import static net.neoforged.neoforge.unittest.transfer.HandlerTestUtil.handlerForStacks;
import static net.neoforged.neoforge.unittest.transfer.HandlerTestUtil.slotInfo;
import static net.neoforged.neoforge.unittest.transfer.HandlerTestUtil.stack;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import net.minecraft.util.Mth;
import net.minecraft.world.level.redstone.Redstone;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ResourceHandlerUtilTest {
    @Nested
    class IsEmpty {
        @Test
        void emptyResourceButNonZeroAmount() {
            assertTrue(ResourceHandlerUtil.isEmpty(TestResource.EMPTY, 1));
        }

        @Test
        void nonEmptyResourceButZeroAmount() {
            assertTrue(ResourceHandlerUtil.isEmpty(TestResource.SOME, 0));
        }

        @Test
        void nonEmptyResourceAndNonZeroAmount() {
            assertFalse(ResourceHandlerUtil.isEmpty(TestResource.SOME, 1));
        }
    }

    @Nested
    class IsEmptyForHandler {
        @Test
        void handlerWithSize0() {
            assertTrue(ResourceHandlerUtil.isEmpty(EmptyResourceHandler.instance()));
        }

        @Test
        void handlerWithEmptySlots() {
            var handler = new MockResourceHandler(1);
            assertTrue(ResourceHandlerUtil.isEmpty(handler));
        }

        @Test
        void handlerWithMixedSlots() {
            var handler = handlerForStacks(EMPTY_STACK, stack(TestResource.SOME, 1), EMPTY_STACK);
            assertFalse(ResourceHandlerUtil.isEmpty(handler));
        }

        @Test
        void handlerWithNonEmptyResourceButZeroAmount() {
            var handler = handlerForStacks(stack(TestResource.SOME, 0));
            assertTrue(ResourceHandlerUtil.isEmpty(handler));
        }
    }

    @Nested
    class IsFullForHandler {
        @Test
        void handlerWithSize0() {
            assertTrue(ResourceHandlerUtil.isFull(EmptyResourceHandler.instance()));
        }

        @Test
        void handlerWithEmptySlots() {
            var handler = new MockResourceHandler(1);
            assertFalse(ResourceHandlerUtil.isFull(handler));
        }

        @Test
        void handlerWithMixedSlots() {
            var handler = handler(
                    slotInfo(TestResource.SOME, Long.MAX_VALUE, Long.MAX_VALUE),
                    slotInfo(TestResource.SOME, Long.MAX_VALUE - 1, Long.MAX_VALUE));
            assertFalse(ResourceHandlerUtil.isFull(handler));
        }

        @Test
        void handlerWithFullSlots() {
            var handler = handler(
                    slotInfo(TestResource.SOME, Long.MAX_VALUE, Long.MAX_VALUE),
                    slotInfo(TestResource.SOME, Long.MAX_VALUE, Long.MAX_VALUE));
            assertTrue(ResourceHandlerUtil.isFull(handler));
        }

        @Test
        void handlerWithOverfilledAndEmptySlot() {
            var handler = handler(
                    slotInfo(TestResource.SOME, 2, 1),
                    EMPTY_SLOT);
            assertFalse(ResourceHandlerUtil.isFull(handler));
        }
    }

    @Nested
    class IsValidForHandler {
        @Test
        void handlerWithSize0() {
            assertFalse(ResourceHandlerUtil.isValid(EmptyResourceHandler.instance(), TestResource.SOME));
        }

        @Test
        void handlerWithNoSlotsAcceptingTheResource() {
            var handler = new MockResourceHandler(1);
            handler.setValid(0);
            assertFalse(ResourceHandlerUtil.isValid(handler, TestResource.SOME));
        }

        @Test
        void handlerWithSomeSlotsAcceptingTheResource() {
            var handler = handler(EMPTY_SLOT, EMPTY_SLOT, EMPTY_SLOT);
            handler.setValid(0);
            handler.setValid(1, TestResource.SOME);
            handler.setValid(2);
            assertTrue(ResourceHandlerUtil.isValid(handler, TestResource.SOME));
        }
    }

    @Nested
    class InsertStacking {
        @Test
        void nullHandlerReturnsZero() {
            assertEquals(0, ResourceHandlerUtil.insertStacking(null, TestResource.SOME, 10, null));
        }

        @Test
        void zeroAmountReturnsZero() {
            var handler = new MockResourceHandler(1);
            assertEquals(0, ResourceHandlerUtil.insertStacking(handler, TestResource.SOME, 0, null));
        }

        @Test
        void insertIntoEmptyHandler() {
            var handler = handler(EMPTY_SLOT, EMPTY_SLOT, EMPTY_SLOT);
            int inserted = ResourceHandlerUtil.insertStacking(handler, TestResource.SOME, 10, null);

            assertEquals(10, inserted);
            assertThat(describeStacks(handler))
                    .containsExactly(
                            stack(TestResource.SOME, 10),
                            EMPTY_STACK,
                            EMPTY_STACK);
        }

        @Test
        void prioritizesNonEmptySlots() {
            var handler = handlerForStacks(EMPTY_STACK, stack(TestResource.SOME, 5), EMPTY_STACK);

            int inserted = ResourceHandlerUtil.insertStacking(handler, TestResource.SOME, 10, null);

            assertEquals(10, inserted);
            assertThat(describeStacks(handler))
                    .containsExactly(
                            EMPTY_STACK,
                            stack(TestResource.SOME, 15),
                            EMPTY_STACK);
        }

        @Test
        void fillsNonEmptySlotsThenEmptySlots() {
            var handler = handler(
                    slotInfo(TestResource.EMPTY, 0, 10),
                    slotInfo(TestResource.SOME, 5, 10),
                    slotInfo(TestResource.EMPTY, 0, 10));

            int inserted = ResourceHandlerUtil.insertStacking(handler, TestResource.SOME, 20, null);

            assertEquals(20, inserted);
            assertThat(describeStacks(handler))
                    .containsExactly(
                            stack(TestResource.SOME, 10),
                            stack(TestResource.SOME, 10),
                            stack(TestResource.SOME, 5));
        }

        @Test
        void transactionIsRespected() {
            var handler = handler(EMPTY_SLOT, EMPTY_SLOT);

            try (Transaction tx = Transaction.openRoot()) {
                int inserted = ResourceHandlerUtil.insertStacking(handler, TestResource.SOME, 10, tx);
                assertEquals(10, inserted);
                assertThat(describeStacks(handler))
                        .containsExactly(
                                stack(TestResource.SOME, 10),
                                EMPTY_STACK);

                // Do not commit the transaction
            }

            // Changes should be rolled back
            assertThat(describeStacks(handler)).containsOnly(EMPTY_STACK, EMPTY_STACK);
        }
    }

    @Nested
    class RedstoneSignalFromResourceHandler {
        @Test
        void handlerWithSize0() {
            assertEquals(Redstone.SIGNAL_NONE, ResourceHandlerUtil.getRedstoneSignalFromResourceHandler(EmptyResourceHandler.instance()));
        }

        @Test
        void handlerWithMixedFillLevels() {
            var handler = handler(
                    slotInfo(TestResource.EMPTY, 0, Long.MAX_VALUE),
                    slotInfo(TestResource.EMPTY, 0, 2));
            assertRedstoneForFillLevels(handler, 100, 50);
        }

        @Test
        void handlerWithOverfilledSlots() {
            var handler = handler(
                    slotInfo(TestResource.EMPTY, 0, 4),
                    slotInfo(TestResource.EMPTY, 0, 2));
            assertRedstoneForFillLevels(handler, 200, 50);
        }

        @Test
        void handlerWithZeroCapacitySlots() {
            var handler = handler(
                    slotInfo(TestResource.EMPTY, 0, 4),
                    slotInfo(TestResource.EMPTY, 0, 0),
                    slotInfo(TestResource.EMPTY, 0, 2));
            assertRedstoneForFillLevels(handler, 100, 100, 100);
        }

        private void assertRedstoneForFillLevels(MockResourceHandler handler, int... fillPercentage) {
            if (fillPercentage.length != handler.size()) {
                throw new IllegalArgumentException("give a fill percentage for every handler slot");
            }
            for (int i = 0; i < fillPercentage.length; i++) {
                handler.set(i, TestResource.SOME, (long) (fillPercentage[i] / 100.0 * handler.getCapacityAsLong(i, TestResource.SOME)));
            }
            var averageFillLevel = (float) (Arrays.stream(fillPercentage)
                    // We expect that overfilled slots do not contribute to the signal more than a full slot
                    .map(p -> Math.min(100, p))
                    .average().getAsDouble() / 100.0);
            var expected = Mth.lerpDiscrete(averageFillLevel, Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
            assertEquals(expected, ResourceHandlerUtil.getRedstoneSignalFromResourceHandler(handler));
        }
    }

    @Nested
    class ExtractFirst {
        @Test
        void nullHandlerReturnsNull() {
            var result = ResourceHandlerUtil.extractFirst(null, r -> true, 10, null);
            assertNull(result);
        }

        @Test
        void emptyHandlerReturnsNull() {
            var handler = handler(EMPTY_SLOT, EMPTY_SLOT);
            var result = ResourceHandlerUtil.extractFirst(handler, r -> true, 10, null);
            assertNull(result);
        }

        @Test
        void extractFromHandlerWithNoMatchingResource() {
            var handler = handlerForStacks(stack(TestResource.SOME, 5), EMPTY_STACK);
            var result = ResourceHandlerUtil.extractFirst(handler, r -> false, 10, null);
            assertNull(result);
        }

        @Test
        void extractLessThanAvailable() {
            var handler = handlerForStacks(stack(TestResource.SOME, 10), EMPTY_STACK);

            var result = ResourceHandlerUtil.extractFirst(handler, r -> true, 5, null);

            assertEquals(new ResourceStack<>(TestResource.SOME, 5), result);

            // Verify handler state
            assertThat(describeStacks(handler))
                    .containsExactly(
                            stack(TestResource.SOME, 5),
                            EMPTY_STACK);
        }

        @Test
        void extractExactlyAvailable() {
            var handler = handlerForStacks(stack(TestResource.SOME, 10), EMPTY_STACK);

            var result = ResourceHandlerUtil.extractFirst(handler, r -> true, 10, null);

            assertEquals(new ResourceStack<>(TestResource.SOME, 10), result);

            // Verify handler state
            assertThat(describeStacks(handler)).containsOnly(EMPTY_STACK, EMPTY_STACK);
        }

        @Test
        void extractMoreThanAvailable() {
            var handler = handlerForStacks(stack(TestResource.SOME, 10), EMPTY_STACK);

            var result = ResourceHandlerUtil.extractFirst(handler, r -> true, 20, null);

            assertEquals(new ResourceStack<>(TestResource.SOME, 10), result);

            // Verify handler state
            assertThat(describeStacks(handler)).containsOnly(EMPTY_STACK, EMPTY_STACK);
        }

        @Test
        void transactionIsRespected() {
            var handler = handlerForStacks(stack(TestResource.SOME, 10), EMPTY_STACK);

            try (Transaction tx = Transaction.openRoot()) {
                var result = ResourceHandlerUtil.extractFirst(handler, r -> true, 5, tx);

                assertEquals(new ResourceStack<>(TestResource.SOME, 5), result);

                assertThat(describeStacks(handler))
                        .containsExactly(
                                stack(TestResource.SOME, 5),
                                EMPTY_STACK);

                // Do not commit the transaction
            }

            // Changes should be rolled back
            assertThat(describeStacks(handler))
                    .containsExactly(
                            stack(TestResource.SOME, 10),
                            EMPTY_STACK);
        }

        @Test
        void resourceFoundButCannotBeExtractedReturnsNull() {
            var handler = new MockResourceHandler(2) {
                @Override
                public int extract(int index, TestResource resource, int amount, TransactionContext transaction) {
                    // Override to make extraction always fail
                    return 0;
                }
            };
            handler.set(0, TestResource.SOME, 10);
            handler.set(1, TestResource.OTHER_1, 5);

            var result = ResourceHandlerUtil.extractFirst(handler, r -> true, 10, null);

            assertNull(result);

            // Verify handler state is unchanged
            assertThat(describeStacks(handler))
                    .containsExactly(
                            stack(TestResource.SOME, 10),
                            stack(TestResource.OTHER_1, 5));
        }
    }

    @Nested
    class Move {
        @Test
        void nullSourceReturnsZero() {
            var target = handler(EMPTY_SLOT);
            int moved = ResourceHandlerUtil.move(null, target, r -> true, 10, null);
            assertEquals(0, moved);
        }

        @Test
        void nullTargetReturnsZero() {
            var source = handlerForStacks(stack(TestResource.SOME, 5));
            int moved = ResourceHandlerUtil.move(source, null, r -> true, 10, null);
            assertEquals(0, moved);
        }

        @Test
        void zeroAmountReturnsZero() {
            var source = handlerForStacks(stack(TestResource.SOME, 5));
            var target = handler(EMPTY_SLOT);
            int moved = ResourceHandlerUtil.move(source, target, r -> true, 0, null);
            assertEquals(0, moved);
        }

        @Test
        void emptySourceReturnsZero() {
            var source = handler(EMPTY_SLOT);
            var target = handler(EMPTY_SLOT);
            int moved = ResourceHandlerUtil.move(source, target, r -> true, 10, null);
            assertEquals(0, moved);
        }

        @Test
        void moveLessThanAvailable() {
            var source = handlerForStacks(stack(TestResource.SOME, 10));
            var target = handler(EMPTY_SLOT);

            int moved = ResourceHandlerUtil.move(source, target, r -> true, 5, null);

            assertEquals(5, moved);

            // Verify source and target state
            assertThat(describeStacks(source))
                    .containsExactly(stack(TestResource.SOME, 5));
            assertThat(describeStacks(target))
                    .containsExactly(stack(TestResource.SOME, 5));
        }

        @Test
        void moveExactlyAvailable() {
            var source = handlerForStacks(stack(TestResource.SOME, 10));
            var target = handler(EMPTY_SLOT);

            int moved = ResourceHandlerUtil.move(source, target, r -> true, 10, null);

            assertEquals(10, moved);

            // Verify source and target state
            assertThat(describeStacks(source))
                    .containsOnly(EMPTY_STACK);
            assertThat(describeStacks(target))
                    .containsExactly(stack(TestResource.SOME, 10));
        }

        @Test
        void moveMoreThanAvailable() {
            var source = handlerForStacks(stack(TestResource.SOME, 10));
            var target = handler(EMPTY_SLOT);

            int moved = ResourceHandlerUtil.move(source, target, r -> true, 20, null);

            assertEquals(10, moved);

            // Verify source and target state
            assertThat(describeStacks(source))
                    .containsOnly(EMPTY_STACK);
            assertThat(describeStacks(target))
                    .containsExactly(stack(TestResource.SOME, 10));
        }

        @Test
        void moveRespectingResourceFilter() {
            var source = handlerForStacks(
                    stack(TestResource.SOME, 10),
                    stack(TestResource.OTHER_1, 5),
                    stack(TestResource.OTHER_2, 7));
            var target = handler(EMPTY_SLOT, EMPTY_SLOT, EMPTY_SLOT);

            int moved = ResourceHandlerUtil.move(source, target, r -> r == TestResource.OTHER_1
                    || r == TestResource.OTHER_2, 20, null);

            assertEquals(12, moved);

            // Verify source state
            assertThat(describeStacks(source))
                    .containsExactly(
                            stack(TestResource.SOME, 10),
                            EMPTY_STACK,
                            EMPTY_STACK);

            // Verify target state
            assertThat(describeStacks(target))
                    .containsExactly(
                            stack(TestResource.OTHER_1, 5),
                            stack(TestResource.OTHER_2, 7),
                            EMPTY_STACK);
        }

        @Test
        void transactionIsRespected() {
            var source = handlerForStacks(stack(TestResource.SOME, 10));
            var target = handler(EMPTY_SLOT);

            try (Transaction tx = Transaction.openRoot()) {
                int moved = ResourceHandlerUtil.move(source, target, r -> true, 5, tx);

                assertEquals(5, moved);

                // Verify intermediate state
                assertThat(describeStacks(source))
                        .containsExactly(stack(TestResource.SOME, 5));
                assertThat(describeStacks(target))
                        .containsExactly(stack(TestResource.SOME, 5));

                // Do not commit the transaction
            }

            // Changes should be rolled back
            assertThat(describeStacks(source))
                    .containsExactly(stack(TestResource.SOME, 10));
            assertThat(describeStacks(target))
                    .containsOnly(EMPTY_STACK);
        }

        @Test
        void limitedByTargetCapacity() {
            var source = handlerForStacks(stack(TestResource.SOME, 10));
            var target = handler(slotInfo(TestResource.EMPTY, 0, 3));

            int moved = ResourceHandlerUtil.move(source, target, r -> true, 10, null);

            assertEquals(3, moved);

            // Verify source and target state
            assertThat(describeStacks(source))
                    .containsExactly(stack(TestResource.SOME, 7));
            assertThat(describeStacks(target))
                    .containsExactly(stack(TestResource.SOME, 3));
        }
    }

    @Nested
    class MoveFirst {
        @Test
        void nullSourceReturnsNull() {
            var target = new MockResourceHandler(1);
            var result = ResourceHandlerUtil.moveFirst(null, target, r -> true, 10, null);
            assertNull(result);
        }

        @Test
        void nullTargetReturnsNull() {
            var source = handlerForStacks(stack(TestResource.SOME, 5));
            var result = ResourceHandlerUtil.moveFirst(source, null, r -> true, 10, null);
            assertNull(result);
        }

        @Test
        void zeroAmountReturnsNull() {
            var source = handlerForStacks(stack(TestResource.SOME, 5));
            var target = handler(EMPTY_SLOT);
            var result = ResourceHandlerUtil.moveFirst(source, target, r -> true, 0, null);
            assertNull(result);
        }

        @Test
        void emptySourceReturnsNull() {
            var source = handler(EMPTY_SLOT);
            var target = handler(EMPTY_SLOT);
            var result = ResourceHandlerUtil.moveFirst(source, target, r -> true, 10, null);
            assertNull(result);
        }

        @Test
        void limitedByTargetCapacity() {
            var source = handlerForStacks(stack(TestResource.SOME, 10));
            var target = handler(slotInfo(TestResource.EMPTY, 0, 5));

            var result = ResourceHandlerUtil.moveFirst(source, target, r -> true, 10, null);

            assertEquals(new ResourceStack<>(TestResource.SOME, 5), result);

            // Verify source and target state
            assertThat(describeStacks(source))
                    .containsExactly(stack(TestResource.SOME, 5));
            assertThat(describeStacks(target))
                    .containsExactly(stack(TestResource.SOME, 5));
        }

        @Test
        void skipsResourceIfTargetRejectsIt() {
            var source = handlerForStacks(
                    stack(TestResource.SOME, 1),
                    stack(TestResource.OTHER_1, 1));
            var target = handler(slotInfo(TestResource.EMPTY, 0, 5));
            target.setValid(0, TestResource.OTHER_1);

            var result = ResourceHandlerUtil.moveFirst(source, target, r -> true, 1, null);
            assertEquals(stack(TestResource.OTHER_1, 1), result);

            // Verify source and target state
            assertThat(describeStacks(source))
                    .containsExactly(stack(TestResource.SOME, 1), null);
            assertThat(describeStacks(target))
                    .containsExactly(stack(TestResource.OTHER_1, 1));
        }

        @Test
        void moveFirstRespectingResourceFilter() {
            var source = handlerForStacks(
                    stack(TestResource.SOME, 10),
                    stack(TestResource.OTHER_1, 5),
                    stack(TestResource.OTHER_2, 7),
                    stack(TestResource.OTHER_1, 2));
            var target = handler(EMPTY_SLOT);

            var result = ResourceHandlerUtil.moveFirst(source, target, r -> r == TestResource.OTHER_1, 6, null);

            assertEquals(new ResourceStack<>(TestResource.OTHER_1, 6), result);

            // Verify source state
            assertThat(describeStacks(source))
                    .containsExactly(
                            stack(TestResource.SOME, 10),
                            EMPTY_STACK,
                            stack(TestResource.OTHER_2, 7),
                            stack(TestResource.OTHER_1, 1));

            // Verify target state
            assertThat(describeStacks(target))
                    .containsExactly(stack(TestResource.OTHER_1, 6));
        }

        @Test
        void transactionIsRespected() {
            var source = handlerForStacks(stack(TestResource.SOME, 10));
            var target = handler(EMPTY_SLOT);

            try (Transaction tx = Transaction.openRoot()) {
                var result = ResourceHandlerUtil.moveFirst(source, target, r -> true, 5, tx);

                assertEquals(new ResourceStack<>(TestResource.SOME, 5), result);

                // Verify intermediate state
                assertThat(describeStacks(source))
                        .containsExactly(stack(TestResource.SOME, 5));
                assertThat(describeStacks(target))
                        .containsExactly(stack(TestResource.SOME, 5));

                // Do not commit the transaction
            }

            // Changes should be rolled back
            assertThat(describeStacks(source))
                    .containsExactly(stack(TestResource.SOME, 10));
            assertThat(describeStacks(target))
                    .containsOnly(EMPTY_STACK);
        }
    }

    @Nested
    class Contains {
        @Test
        void emptyHandlerDoesNotContainResource() {
            var handler = new MockResourceHandler(1);
            assertFalse(ResourceHandlerUtil.contains(handler, TestResource.SOME));
        }

        @Test
        void handlerContainsResource() {
            var handler = handlerForStacks(
                    stack(TestResource.SOME, 5),
                    stack(TestResource.OTHER_1, 10));

            assertTrue(ResourceHandlerUtil.contains(handler, TestResource.SOME));
            assertTrue(ResourceHandlerUtil.contains(handler, TestResource.OTHER_1));
            assertFalse(ResourceHandlerUtil.contains(handler, TestResource.OTHER_2));
        }

        @Test
        void handlerWithZeroAmountContainsResource() {
            var handler = handlerForStacks(stack(TestResource.SOME, 0));

            assertTrue(ResourceHandlerUtil.contains(handler, TestResource.SOME));
        }
    }

    @Nested
    class IndexOf {
        @Test
        void emptyHandlerReturnsMinusOne() {
            var handler = new MockResourceHandler(1);
            assertEquals(-1, ResourceHandlerUtil.indexOf(handler, TestResource.SOME));
        }

        @Test
        void returnsCorrectIndex() {
            var handler = handlerForStacks(
                    stack(TestResource.EMPTY, 0),
                    stack(TestResource.SOME, 5),
                    stack(TestResource.OTHER_1, 10));

            assertEquals(1, ResourceHandlerUtil.indexOf(handler, TestResource.SOME));
            assertEquals(2, ResourceHandlerUtil.indexOf(handler, TestResource.OTHER_1));
            assertEquals(-1, ResourceHandlerUtil.indexOf(handler, TestResource.OTHER_2));
        }
    }

    @Nested
    class FindExtractableResource {
        @Test
        void emptyHandlerReturnsNull() {
            var handler = new MockResourceHandler(1);
            var result = ResourceHandlerUtil.findExtractableResource(handler, r -> true, null);
            assertNull(result);
        }

        @Test
        void resourceNotMatchingFilterReturnsNull() {
            var handler = handlerForStacks(stack(TestResource.SOME, 5));
            var result = ResourceHandlerUtil.findExtractableResource(handler, r -> r == TestResource.OTHER_1, null);
            assertNull(result);
        }

        @Test
        void findsMatchingExtractableResource() {
            var handler = handlerForStacks(
                    stack(TestResource.SOME, 0),  // Zero amount, not extractable
                    stack(TestResource.OTHER_1, 5),
                    stack(TestResource.OTHER_2, 7));

            var result = ResourceHandlerUtil.findExtractableResource(handler, r -> true, null);
            assertThat(result).isEqualTo(TestResource.OTHER_1);
        }

        @Test
        void nonExtractableResourceReturnsNull() {
            var handler = new MockResourceHandler(1) {
                @Override
                public int extract(int index, TestResource resource, int amount, TransactionContext transaction) {
                    // Override to make extraction always fail
                    return 0;
                }
            };
            handler.set(0, TestResource.SOME, 5);

            var result = ResourceHandlerUtil.findExtractableResource(handler, r -> true, null);
            assertNull(result);
        }

        @Test
        void transactionIsRespectedWithAutoTransaction() {
            var handler = handlerForStacks(stack(TestResource.SOME, 5));

            // This call should not modify the handler since findExtractableResource uses a nested transaction
            ResourceHandlerUtil.findExtractableResource(handler, r -> true, null);

            // Verify handler state is unchanged
            assertThat(describeStacks(handler))
                    .containsExactly(stack(TestResource.SOME, 5));
        }

        @Test
        void transactionIsRespectedWithGivenTransaction() {
            var handler = handlerForStacks(stack(TestResource.SOME, 5));

            // This call should not modify the handler since findExtractableResource uses a nested transaction
            try (var tx = Transaction.openRoot()) {
                ResourceHandlerUtil.findExtractableResource(handler, r -> true, tx);

                // Verify handler state is unchanged even within the transaction
                assertThat(describeStacks(handler))
                        .containsExactly(stack(TestResource.SOME, 5));
            }
        }
    }
}
