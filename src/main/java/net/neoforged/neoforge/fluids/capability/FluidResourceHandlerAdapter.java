/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.capability;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

@Deprecated(since = "1.21.9", forRemoval = true)
class FluidResourceHandlerAdapter implements IFluidHandler {
    private final ResourceHandler<FluidResource> handler;

    FluidResourceHandlerAdapter(ResourceHandler<FluidResource> handler) {
        this.handler = handler;
    }

    @Override
    public int getTanks() {
        return handler.size();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return FluidUtil.getStack(handler, tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return handler.getCapacityAsInt(tank, FluidResource.EMPTY);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return handler.isValid(tank, FluidResource.of(stack));
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        try (var tx = Transaction.openRoot()) {
            int inserted = handler.insert(FluidResource.of(resource), resource.getAmount(), tx);
            if (action.execute()) {
                tx.commit();
            }
            return inserted;
        }
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        try (var tx = Transaction.openRoot()) {
            int extracted = handler.extract(FluidResource.of(resource), resource.getAmount(), tx);
            if (action.execute()) {
                tx.commit();
            }
            return extracted == 0 ? FluidStack.EMPTY : resource.copyWithAmount(extracted);
        }
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        try (var tx = Transaction.openRoot()) {
            var extracted = ResourceHandlerUtil.extractFirst(handler, fr -> true, maxDrain, tx);
            if (action.execute()) {
                tx.commit();
            }
            return extracted == null ? FluidStack.EMPTY : extracted.resource().toStack(extracted.amount());
        }
    }
}
