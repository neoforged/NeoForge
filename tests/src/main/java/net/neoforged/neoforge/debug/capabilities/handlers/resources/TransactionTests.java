/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers.resources;

import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = ResourceHandlerTestSetup.GROUP_ID, idPrefix = "resource.handler.transaction.")

public class TransactionTests {

    //These have mostly just been experiments, to see a given result real tests for specifically transactions need to be made still.
    // The other tests using resource handlers only partially test them
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Transactional tests. Takes the idea of looking for a subset of items and able to return the crafting ingredients")
    private static void itemTransfer(ExtendedGameTestHelper helper) {
        //todo, the test is still in progress. This is also helping identify if anything should change
        // The more notable changes to be made is swapping out the container types used.
        helper.succeed();
    }

    /**
     * Attempts to insert 10 apples to the handler.
     *
     * @return how many apples were inserted.
     */
    public static int addApples(IResourceHandler<ItemResource> handler) {
        var apple = Items.APPLE.defaultResource();
        try (var tx = TransactionManager.open(null)) {
            int inserted = handler.insert(apple, 10, tx);
            tx.commit();
            return inserted;
        }
    }

    /**
     * Extracts 16 coal from slot 0 and inserts 1 diamond into slot 1. Only if both succeed.
     *
     * @return {@code true} if both operations succeeded, {@code false} otherwise.
     */
    public static boolean coalToDiamonds(IResourceHandler<ItemResource> handler, TransferAction action) {
        var coal = Items.COAL.defaultResource();
        var diamond = Items.DIAMOND.defaultResource();

        try (var tx = TransactionManager.open(null)) {
            if (handler.extract(0, coal, 16, tx) != 16) return false;
            if (handler.insert(1, diamond, 1, tx) != 1) return false;
            return action.commit(tx);
        }
    }
}
