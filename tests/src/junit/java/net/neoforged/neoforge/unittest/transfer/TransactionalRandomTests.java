/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import static org.assertj.core.api.Assertions.assertThat;

import net.neoforged.neoforge.transfer.item.TransactionalRandom;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.junit.jupiter.api.Test;

public class TransactionalRandomTests {
    @Test
    void testDeterminismAcrossRevertedTransactions() {
        var transactionalRandom = new TransactionalRandom();

        double d1, d2;
        try (var tx = Transaction.openRoot()) {
            d1 = transactionalRandom.nextDouble(tx);
            d2 = transactionalRandom.nextDouble(tx);
        }

        double d3, d4;
        try (var tx = Transaction.openRoot()) {
            d3 = transactionalRandom.nextDouble(tx);
            d4 = transactionalRandom.nextDouble(tx);
        }

        assertThat(d3).isEqualTo(d1);
        assertThat(d4).isEqualTo(d2);
    }

    @Test
    void testDifferentResultsAfterCommit() {
        var transactionalRandom = new TransactionalRandom();

        double d1, d2;
        try (var tx = Transaction.openRoot()) {
            d1 = transactionalRandom.nextDouble(tx);
            d2 = transactionalRandom.nextDouble(tx);
            tx.commit();
        }

        double d3, d4;
        try (var tx = Transaction.openRoot()) {
            d3 = transactionalRandom.nextDouble(tx);
            d4 = transactionalRandom.nextDouble(tx);
        }

        assertThat(d3).isNotEqualTo(d1);
        assertThat(d4).isNotEqualTo(d2);
    }
}
