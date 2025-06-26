/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import net.neoforged.neoforge.transfer.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.templates.energy.VoidEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
import net.neoforged.neoforge.transfer.transaction.UnsafeTransactionManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UnsafeTransactionManagerTest {
    @Test
    void unsafe() {
        //Providing a way we can open transactions while inside a method that may not have the context available
        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {
            Assertions.assertThat(transaction).isNotNull();
            Assertions.assertThat(TransactionManager.isActive()).isTrue();

            someMethodThatStopsProvidingParams();
        }
        Assertions.assertThat(UnsafeTransactionManager.getCurrentOpenedTransaction()).isNull();
        Assertions.assertThat(TransactionManager.isActive()).isFalse();

        //It didn't throw, so this means we succeeded. \o/
    }

    //We want a method that simulates an api boundary where a transactionContext is not provided
    private static void someMethodThatStopsProvidingParams() {
        try (Transaction secondTransaction = UnsafeTransactionManager.openUnsafe()) {
            //These methods actually make use of this functionality, but we want to be doubly sure by opening
            // an outer transaction on top of it.
            var insertable = EnergyHandlerUtil.getInsertableAmount(VoidEnergyHandler.INSTANCE);
            var extractable = EnergyHandlerUtil.getExtractableAmount(VoidEnergyHandler.INSTANCE);

            //This commit is mostly just to see that we can commit this layer without crashing
            secondTransaction.commit();
        }
    }
}
