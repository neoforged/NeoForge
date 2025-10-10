/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class TransactionTests {
    @Test
    void testHierarchy() {
        try (Transaction transaction = Transaction.openRoot()) {
            Assertions.assertEquals(0, transaction.depth());

            try (Transaction subTransaction = Transaction.open(transaction)) {
                Assertions.assertEquals(1, subTransaction.depth());
            }
        }
    }

    @Test
    void testSimultaneousRootValidation() {
        try (Transaction root1 = Transaction.openRoot()) {
            Assertions.assertThrows(IllegalStateException.class, () -> {
                try (Transaction root2 = Transaction.openRoot()) {
                    throw new AssertionError("Two root transactions on the same thread were opened and permitted.");
                }
            }, "Two root transactions should not be openable simultaneously");

        }
        Assertions.assertDoesNotThrow(() -> {
            try (Transaction root2 = Transaction.openRoot()) {

            }
        }, "The sub transaction should be able to be opened as a root since `root1` should be closed.");
    }

    @Test
    void testSimultaneousParentValidation() {
        // Ensures that 2 transactions cannot share the same parent at the same time, but reusing a parent is fine
        // just as long as the transactions are fully completed before doing so.
        try (Transaction root = Transaction.openRoot()) {
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
        int expectedValueAfterCommit = 2;
        var transactionalInt = new TransactionalInt();

        try (Transaction transaction = Transaction.openRoot()) {
            Assertions.assertEquals(0, transaction.depth());
            try (Transaction subTransaction = Transaction.open(transaction)) {
                transactionalInt.set(expectedValueAfterCommit, subTransaction);
                subTransaction.commit();
            }
        }

        Assertions.assertEquals(0, transactionalInt.value);

        try (Transaction transaction = Transaction.openRoot()) {
            Assertions.assertEquals(0, transaction.depth());
            try (Transaction subTransaction = Transaction.open(transaction)) {
                transactionalInt.set(expectedValueAfterCommit, subTransaction);
                subTransaction.commit();
            }
            transaction.commit();
        }
        Assertions.assertEquals(expectedValueAfterCommit, transactionalInt.value);
    }

    @SuppressWarnings("deprecation")
    @Test
    void getTheCurrentTransaction() {
        //Providing a way we can open transactions while inside a method that may not have the context available
        try (Transaction transaction = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
            Assertions.assertNotNull(transaction);
            Assertions.assertEquals(Transaction.Lifecycle.OPEN, Transaction.getLifecycle());
        }

        Assertions.assertNull(Transaction.getCurrentOpenedTransaction());
        Assertions.assertEquals(Transaction.Lifecycle.NONE, Transaction.getLifecycle());

        try (Transaction transaction = Transaction.openRoot()) {
            Assertions.assertEquals(transaction, Transaction.getCurrentOpenedTransaction());

            try (Transaction subTransaction = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
                Assertions.assertEquals(subTransaction, Transaction.getCurrentOpenedTransaction());
            }

            try (Transaction subTransaction = Transaction.open(transaction)) {
                Assertions.assertEquals(subTransaction, Transaction.getCurrentOpenedTransaction());
            }

        }
    }

    protected static class TransactionalInt extends SnapshotJournal<Integer> {
        int value;

        void set(int value, TransactionContext transaction) {
            updateSnapshots(transaction);
            this.value = value;
        }

        @Override
        protected Integer createSnapshot() {
            return this.value;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            this.value = snapshot;
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
        try (var tx = Transaction.openRoot()) {
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

    /**
     * Tests for "recursive" root transactions, i.e. opened from {@link SnapshotJournal#onRootCommit}.
     */
    @Nested
    class RootCommitTransactions {
        /**
         * Keeps track of the state of a journal for each call to {@link SnapshotJournal#onRootCommit}.
         */
        record RootCommit(int originalState, int currentState) {}

        @Test
        void testModifyingOtherJournalLast() {
            var rootCommits1 = new ArrayList<RootCommit>();
            var rootCommits2 = new ArrayList<RootCommit>();

            var transactionalInt1 = new TransactionalInt() {
                @Override
                protected void onRootCommit(Integer originalState) {
                    rootCommits1.add(new RootCommit(originalState, value));
                }
            };
            var transactionalInt2 = new TransactionalInt() {
                @Override
                protected void onRootCommit(Integer originalState) {
                    rootCommits2.add(new RootCommit(originalState, value));
                    // Trigger a nested transaction which will modify 2
                    try (Transaction tx = Transaction.openRoot()) {
                        transactionalInt1.set(10, tx);
                        tx.commit();
                    }
                }
            };

            // Modify 1 first, then 2.
            // Root commit callbacks should be 1, then 2, then 1 again during 2's callback
            try (var tx = Transaction.openRoot()) {
                transactionalInt1.set(1, tx);
                transactionalInt2.set(2, tx);
                tx.commit();
            }
            assertThat(rootCommits1)
                    .containsExactly(new RootCommit(0, 1), new RootCommit(1, 10));
            assertThat(rootCommits2)
                    .containsExactly(new RootCommit(0, 2));
        }

        @Test
        void testModifyingOtherJournalFirst() {
            var rootCommits1 = new ArrayList<RootCommit>();
            var rootCommits2 = new ArrayList<RootCommit>();

            var transactionalInt1 = new TransactionalInt() {
                @Override
                protected void onRootCommit(Integer originalState) {
                    rootCommits1.add(new RootCommit(originalState, value));
                }
            };
            var transactionalInt2 = new TransactionalInt() {
                @Override
                protected void onRootCommit(Integer originalState) {
                    rootCommits2.add(new RootCommit(originalState, value));
                    // Trigger a nested transaction which will modify 2
                    try (Transaction tx = Transaction.openRoot()) {
                        transactionalInt1.set(10, tx);
                        tx.commit();
                    }
                }
            };

            // Modify 2 first, then 1.
            // Root commit callbacks should be 2, and 1 during 2's callback.
            // The callback of 1 will not be called again from the outer transaction since it was not modified in the meanwhile.
            try (var tx = Transaction.openRoot()) {
                transactionalInt2.set(2, tx);
                transactionalInt1.set(1, tx);
                tx.commit();
            }
            assertThat(rootCommits1)
                    .containsExactly(new RootCommit(0, 10)); // straight from 0 to 10
            assertThat(rootCommits2)
                    .containsExactly(new RootCommit(0, 2));
        }

        @Test
        void testModifyingSelf() {
            var rootCommits = new ArrayList<RootCommit>();

            var transactionalInt = new TransactionalInt() {
                @Override
                protected void onRootCommit(Integer originalState) {
                    rootCommits.add(new RootCommit(originalState, value));
                    if (value < 5) {
                        try (var tx = Transaction.openRoot()) {
                            set(value + 1, tx);
                            tx.commit();
                        }
                    }
                }
            };

            try (var tx = Transaction.openRoot()) {
                transactionalInt.set(1, tx);
                tx.commit();
            }

            assertThat(rootCommits).containsExactly(
                    new RootCommit(0, 1),
                    new RootCommit(1, 2),
                    new RootCommit(2, 3),
                    new RootCommit(3, 4),
                    new RootCommit(4, 5));
        }

        @Test
        void testModifyingSelfThenAbort() {
            var rootCommits = new ArrayList<RootCommit>();

            var transactionalInt = new TransactionalInt() {
                @Override
                protected void onRootCommit(Integer originalState) {
                    rootCommits.add(new RootCommit(originalState, value));
                    if (value < 5) {
                        try (var tx = Transaction.openRoot()) {
                            set(value + 1, tx);
                            // Don't commit it
                        }
                    }
                }
            };

            try (var tx = Transaction.openRoot()) {
                transactionalInt.set(1, tx);
                tx.commit();
            }

            assertThat(rootCommits).containsExactly(
                    new RootCommit(0, 1));
        }
    }
}
