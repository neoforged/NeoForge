/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import static org.assertj.core.api.Assertions.assertThat;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TransactionTests {
    @Test
    void testHierarchy() {
        try (Transaction transaction = Transaction.open(null)) {
            Assertions.assertEquals(0, transaction.depth());

            try (Transaction subTransaction = Transaction.open(transaction)) {
                Assertions.assertEquals(1, subTransaction.depth());
            }
        }
    }

    @Test
    void testSimultaneousRootValidation() {
        try (Transaction root1 = Transaction.open(null)) {
            Assertions.assertThrows(IllegalStateException.class, () -> {
                try (Transaction root2 = Transaction.open(null)) {
                    throw new AssertionError("Two root transactions on the same thread were opened and permitted.");
                }
            }, "Two root transactions should not be openable simultaneously");

        }
        Assertions.assertDoesNotThrow(() -> {
            try (Transaction root2 = Transaction.open(null)) {

            }
        }, "The sub transaction should be able to be opened as a root since `root1` should be closed.");
    }

    @Test
    void testSimultaneousParentValidation() {
        // Ensures that 2 transactions cannot share the same parent at the same time, but reusing a parent is fine
        // just as long as the transactions are fully completed before doing so.
        try (Transaction root = Transaction.open(null)) {
            try (Transaction sub1 = Transaction.open(root)) {
                Assertions.assertThrows(IllegalStateException.class, () -> {
                    try (Transaction sub2 = Transaction.open(root)) {
                        throw new AssertionError("Two transactions on the same thread were opened and permitted with the same parent.");
                    }
                }, "Two transactions should not be openable simultaneously on the same parent");

            }
            Assertions.assertDoesNotThrow(() -> {
                try (Transaction sub2 = Transaction.open(root)) {

                }
            }, "The sub transaction should be able to be opened with the root as the parent since sub1 should be closed.");
        }
    }

    @Test
    void testCommit() {
        final int expectedValueAfterCommit = 2;
        final Container container = new Container();
        IntSnapshotJournal journal = IntSnapshotJournal.of(container::set, container::get);

        try (Transaction transaction = Transaction.open(null)) {
            Assertions.assertEquals(0, transaction.depth());
            try (Transaction subTransaction = Transaction.open(transaction)) {
                journal.updateSnapshots(subTransaction);
                container.set(expectedValueAfterCommit);
                subTransaction.commit();
            }
        }

        Assertions.assertEquals(0, container.value);

        try (Transaction transaction = Transaction.open(null)) {
            Assertions.assertEquals(0, transaction.depth());
            try (Transaction subTransaction = Transaction.open(transaction)) {
                journal.updateSnapshots(subTransaction);
                container.set(expectedValueAfterCommit);
                subTransaction.commit();
            }
            transaction.commit();
        }
        Assertions.assertEquals(expectedValueAfterCommit, container.value);
    }

    @SuppressWarnings("deprecation")
    @Test
    void getTheCurrentTransaction() {
        //Providing a way we can open transactions while inside a method that may not have the context available
        try (Transaction transaction = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
            Assertions.assertNotNull(transaction);
            Assertions.assertTrue(Transaction.hasActiveTransaction());
        }

        Assertions.assertNull(Transaction.getCurrentOpenedTransaction());
        Assertions.assertFalse(Transaction.hasActiveTransaction());

        try (Transaction transaction = Transaction.open(null)) {
            Assertions.assertEquals(transaction, Transaction.getCurrentOpenedTransaction());

            try (Transaction subTransaction = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
                Assertions.assertEquals(subTransaction, Transaction.getCurrentOpenedTransaction());
            }

            try (Transaction subTransaction = Transaction.open(transaction)) {
                Assertions.assertEquals(subTransaction, Transaction.getCurrentOpenedTransaction());
            }

        }
    }

    private static class Container {
        int value;

        void set(int value) {
            this.value = value;
        }

        int get() {
            return this.value;
        }
    }

    /**
     * A snapshot journal that can keep track of an {@code int}.
     */
    private static class IntSnapshotJournal extends SnapshotJournal<Integer> {
        /**
         * Apply the value for snapshotting. This value should be the last valid value from the {@link IntSnapshotJournal.Snapshot}
         * during the transaction chain.
         */
        @FunctionalInterface
        public interface Revert {
            void set(int value);
        }

        /**
         * Gets the current value for snapshotting.
         */
        @FunctionalInterface
        public interface Snapshot {
            Integer get();
        }

        private final IntSnapshotJournal.Revert setter;
        private final IntSnapshotJournal.Snapshot getter;

        public static IntSnapshotJournal of(IntSnapshotJournal.Revert setter, IntSnapshotJournal.Snapshot getter) {
            return new IntSnapshotJournal(setter, getter);
        }

        private IntSnapshotJournal(IntSnapshotJournal.Revert setter, IntSnapshotJournal.Snapshot getter) {
            this.setter = setter;
            this.getter = getter;
        }

        @Override
        protected Integer createSnapshot() {
            return getter.get();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            setter.set(snapshot);
        }
    }

    @Test
    void testNullSnapshots() {
        class VoidJournal extends SnapshotJournal<Void> {
            int createdSnapshots = 0;
            int rootCommits = 0;

            @Override
            protected @Nullable Void createSnapshot() {
                ++createdSnapshots;
                return null;
            }

            @Override
            protected void revertToSnapshot(@Nullable Void snapshot) {}

            @Override
            protected void onRootCommit(@Nullable Void originalState) {
                if (originalState != null) {
                    throw new AssertionError("originalState should have been null");
                }
                ++rootCommits;
            }
        }

        var journal = new VoidJournal();
        try (var tx = Transaction.open(null)) {
            journal.updateSnapshots(tx);
            assertThat(journal.createdSnapshots).isEqualTo(1);
            // Second update is a no-op because the snapshot already exists
            journal.updateSnapshots(tx);
            assertThat(journal.createdSnapshots).isEqualTo(1);

            try (var nested = Transaction.open(tx)) {
                journal.updateSnapshots(nested);
                assertThat(journal.createdSnapshots).isEqualTo(2);
            }

            tx.commit();
        }
        assertThat(journal.rootCommits).isOne();
    }
}
