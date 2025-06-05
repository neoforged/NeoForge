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
import net.neoforged.neoforge.transfer.handlers.templates.container.ResourceContainer;
import net.neoforged.neoforge.transfer.handlers.templates.container.SimpleItemResourceContainer;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.UnsafeResourceUtils;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = ResourceHandlerTestSetup.GROUP_ID, idPrefix = "resource.handler.transaction.")

public class TransactionTests {
    private static <T extends IResource> void resetSandbox(ResourceContainer<T> destination, ResourceContainer<T> src) {
        for (int craftingSlot = 0; craftingSlot < 9; craftingSlot++)
            destination.set(craftingSlot, src.get(craftingSlot));
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Transactional tests. Takes the idea of looking for a subset of items and able to return the crafting ingredients")
    private static void itemTransfer(ExtendedGameTestHelper helper) {
        //todo, the test is still in progress. This is also helping identify if anything should change
        var b = SimpleItemResourceContainer.builder(0).build();

        var infiniteSource = new InfiniteResourceHandler<>(Items.DIAMOND.defaultResource());

        var internalContainer = SimpleItemResourceContainer.builder(9).capacity(Item.DEFAULT_MAX_STACK_SIZE).build().asHandler();

        //noinspection unchecked
        IResourceHandlerModifiable<ItemResource>[] externalContainers = new IResourceHandlerModifiable[3];
        externalContainers[0] = SimpleItemResourceContainer.builder(4).capacity(Item.DEFAULT_MAX_STACK_SIZE).build().asHandler();
        externalContainers[1] = SimpleItemResourceContainer.builder(2).build().asHandler();
        externalContainers[2] = SimpleItemResourceContainer.builder(100).capacity(32).build().asHandler();
        for (var index = 0; index < externalContainers[0].size(); index++) {
            externalContainers[0].set(index, Items.LAVA_BUCKET.defaultResource(), 2);

        }

        var ingredient = Ingredient.of(Items.LAVA_BUCKET);
        var need = 5;
        var result = Items.DIAMOND_PICKAXE.defaultResource();
        var current = 0;
        //        var craftingRecipe = CraftingInput.of(3, 3, );

        try (var craftingTransaction = Transaction.open(TransactionContext.ROOT)) {

            try (var scanningTransaction = Transaction.open(craftingTransaction)) {
                for (var container : externalContainers) {
                    for (var index = 0; index < container.size(); index++) {

                        var resource = container.getResource(index);
                        if (!someFilteredCondition(resource, ingredient)) continue;

                        int simulatedValue = 0;
                        try (var optimisticTransaction = Transaction.open(scanningTransaction)) {
                            var testValue = need - current;
                            simulatedValue = tryIndex(container, optimisticTransaction, resource, index, testValue);
                            if (simulatedValue == 0) continue;

                            current += simulatedValue;
                            optimisticTransaction.commit();
                            if (simulatedValue == testValue) {
                                break;
                            }
                        }
                    }

                    if (current != need) continue;

                    scanningTransaction.commit();
                    break;
                }
            }
//            if (internalContainer.insert(result, 1, craftingTransaction) > 0)
//                craftingTransaction.commit();
        }

        try (var tx = Transaction.open(TransactionContext.ROOT)) {
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

    private static boolean someFilteredCondition(ItemResource resource, Ingredient ingredient) {
        return resource.test(ingredient);
    }

    private static int tryIndex(IResourceHandlerModifiable<ItemResource> container, Transaction transaction, ItemResource resource, int index, int amount) {
        var remainderStack = UnsafeResourceUtils.innerStackOf(resource).getCraftingRemainder();
        var extracted = container.extract(index, resource, amount, transaction);
        if (extracted == 0) return 0;

        if (remainderStack.isEmpty()) return extracted;

        var remainder = ItemResource.of(remainderStack);
        var inserted = container.insert(index, remainder, extracted, transaction);
        if (inserted < extracted)
            inserted += container.insert(remainder, extracted - inserted, transaction);
        return inserted;
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
