/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.resources;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.IHandleIOBehaviour;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.resources.SimpleFluidResourceContainer;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.resources.SimpleItemResourceContainer;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

// Note to Orion, still working out how this would work and whether or not it is possible. I've tagged this and a few other things so I can quick remove if needed
// PROTOTYPE
@FunctionalInterface
public interface ITransactionOperation {
    void run(ITransactionHandler handler, Transaction transaction);

    default ITransactionOperation whenSuccessful(ITransactionHandler handlerA, ITransactionOperation after) {
        return (handlerB, transaction) -> {
            var reporter = transaction.reporting();

            run(handlerA, transaction);

            if (reporter.isSuccess()) {
                after.run(handlerA, transaction);
            }
        };
    }

    static void test() {
        var item = Items.SAND.defaultResource();
        var otherItemHandler = SimpleItemResourceContainer.builder(1).build();
        otherItemHandler.set(0, item.withMutableAmount(5));
        var handler = otherItemHandler.slice(0, 5).asHandler(IHandleIOBehaviour.INSERT_ONLY);
        var tankWater = SimpleFluidResourceContainer.builder(1).build();
        var tankLava = SimpleFluidResourceContainer.builder(1).build();

        tankWater.set(0, FluidResource.of(Fluids.WATER).withMutableAmount(FluidType.BUCKET_VOLUME));
        tankLava.set(0, FluidResource.of(Fluids.LAVA).withMutableAmount(FluidType.BUCKET_VOLUME));

        try (var tx = Transaction.open(TransactionContext.ROOT)) {
//            otherItemHandler.asHandler().operate((handler, transaction) -> {
//
//            }, tx);
        }
    }
}
