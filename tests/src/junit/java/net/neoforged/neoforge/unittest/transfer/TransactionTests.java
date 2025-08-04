/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
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
}
