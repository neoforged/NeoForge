package net.neoforged.neoforge.debug.capabilities.handlers;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.transfer.handlermk2.IResourceHandlerModifiableTransaction;
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

        var internalContainer = SimpleItemResourceContainer.builder(9).capacity(Item.DEFAULT_MAX_STACK_SIZE).build().asHandler2();

        IResourceHandlerModifiableTransaction<ItemResource>[] externalContainers = new IResourceHandlerModifiableTransaction[3];
        externalContainers[0] = SimpleItemResourceContainer.builder(4).capacity(Item.DEFAULT_MAX_STACK_SIZE).build().asHandler2();
        externalContainers[1] = SimpleItemResourceContainer.builder(2).build().asHandler2();
        externalContainers[2] = SimpleItemResourceContainer.builder(100).capacity(32).build().asHandler2();

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


    }
}
