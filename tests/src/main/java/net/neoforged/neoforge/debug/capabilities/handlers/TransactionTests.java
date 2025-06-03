/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.templates.InfiniteResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.container.SimpleItemResourceContainer;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = ResourceHandlerTestSetup.GROUP_ID, idPrefix = "resource.handler.transaction.")

public class TransactionTests {
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that FluidUtil#tryPickupFluid works correctly")
    private static void itemTransfer(ExtendedGameTestHelper helper) {
        var infiniteSource = new InfiniteResourceHandler<>(Items.DIAMOND.defaultResource());

        var internalContainer = SimpleItemResourceContainer.builder(9).capacity(Item.DEFAULT_MAX_STACK_SIZE).build().asHandler();

        //noinspection unchecked
        IResourceHandlerModifiable<ItemResource>[] externalContainers = new IResourceHandlerModifiable[3];
        externalContainers[0] = SimpleItemResourceContainer.builder(4).capacity(Item.DEFAULT_MAX_STACK_SIZE).build().asHandler();
        externalContainers[1] = SimpleItemResourceContainer.builder(2).build().asHandler();
        externalContainers[2] = SimpleItemResourceContainer.builder(100).capacity(32).build().asHandler();

        var ingredient1 = Ingredient.of(Items.STICK);
        var need1 = 2;
        var ingredient2 = Ingredient.of(Items.DIAMOND);
        var need2 = 3;
        var result = Items.DIAMOND_PICKAXE.defaultResource();

        try (var tx = Transaction.open(null)) {
            var current1 = 0;
            var current2 = 0;
            for (var container : externalContainers) {
                try (var innerTx = Transaction.open(tx)) {

                    for (var index = 0; index < container.size(); index++) {
                        var resource = container.getResource(index);
                        if (!resource.test(ingredient1)) continue;

                        current1 += container.extract(index, resource, need1 - current1, innerTx);
                    }
                }
            }
            if (internalContainer.insert(result, 1, tx) > 0)
                tx.commit();
        }

        try (var tx = Transaction.open(null)) {
            var amount = externalContainers[1].extract(Items.APPLE.defaultResource(), 12, tx);
            int inserted;
            try (var attempt1 = Transaction.open(tx)) {
                inserted = externalContainers[2].insert(Items.APPLE.defaultResource(), amount, tx);
                if (inserted == amount) {
                    attempt1.commit();
                }
            }
            if (amount == inserted) {
                tx.commit();
            } else {
                try (var attempt2 = Transaction.open(tx)) {
                    inserted = externalContainers[0].insert(Items.APPLE.defaultResource(), amount, tx);
                    if (inserted == amount) {
                        attempt2.commit();
                    }
                }
                if (amount == inserted)
                    tx.commit();
            }

        }

        helper.succeed();
    }

    /**
     * Attempts to insert 10 apples to the handler.
     *
     * @return how many apples were inserted.
     */
    public static int addApples(IResourceHandler<ItemResource> handler) {
        var apple = Items.APPLE.defaultResource();
        try (var tx = Transaction.open(null)) {
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

        try (var tx = Transaction.open(null)) {
            if (handler.extract(0, coal, 16, tx) != 16) return false;
            if (handler.insert(1, diamond, 1, tx) != 1) return false;
            return action.commit(tx);
        }
    }
}
