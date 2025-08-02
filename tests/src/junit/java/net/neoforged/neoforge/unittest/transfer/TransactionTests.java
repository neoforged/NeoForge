/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.snapshots.IndexedIntSnapshot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransactionTests {
    private static class Container {
        int value;

        void set(int index, int value) {
            this.value = value;
        }

        int get(int index) {
            return this.value;
        }
    }

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
        final int valueToBe = 2;
        final Container container = new Container();
        IndexedIntSnapshot journal = IndexedIntSnapshot.of(container::set, container::get, null);

        try (Transaction transaction = Transaction.open(null)) {
            Assertions.assertEquals(0, transaction.depth());
            try (Transaction subTransaction = Transaction.open(transaction)) {
                journal.updateSnapshots(subTransaction);
                container.set(0, valueToBe);
                subTransaction.commit();
            }
        }
        Assertions.assertEquals(0, container.value);

        try (Transaction transaction = Transaction.open(null)) {
            Assertions.assertEquals(0, transaction.depth());
            try (Transaction subTransaction = Transaction.open(transaction)) {
                journal.updateSnapshots(subTransaction);
                container.set(0, valueToBe);
                subTransaction.commit();
            }
            transaction.commit();
        }
        Assertions.assertEquals(valueToBe, container.value);
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
}
