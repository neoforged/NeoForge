/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers.resources;

import net.neoforged.neoforge.transfer.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.templates.energy.VoidEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.UnsafeTransactionManager;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = ResourceHandlerTestSetup.GROUP_ID, idPrefix = "resource.handler.transaction.")

public class TransactionTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Unsafe manager tests")
    private static void unsafe(ExtendedGameTestHelper helper) {
        //Providing a way we can open transactions while inside a method that may not have the context available
        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {
            someMethodThatStopsProvidingParams();
        }

        //It didn't throw, so this means we succeeded. \o/
        helper.succeed();
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
