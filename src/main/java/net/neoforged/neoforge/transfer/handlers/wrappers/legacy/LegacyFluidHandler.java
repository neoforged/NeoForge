/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.legacy;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerDeprecationHandling;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.UnsafeTransactionManager;

@Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
public sealed class LegacyFluidHandler implements IFluidHandler permits LegacyFluidItemHandler {
    private final IResourceHandler<FluidResource> handler;

    public LegacyFluidHandler(IResourceHandler<FluidResource> handler) {
        this.handler = handler;
    }

    @Override
    public int getTanks() {
        return handler.size();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return handler.getResource(tank).toStack(handler.getAmount(tank));
    }

    @Override
    public int getTankCapacity(int tank) {
        return handler.getCapacity(tank, handler.getResource(tank));
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return handler.isValid(tank, FluidResource.of(stack));
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {
            int inserted = handler.insert(FluidResource.of(resource), resource.getAmount(), transaction);
            if (action.execute()) {
                transaction.commit();
            }
            return inserted;
        }
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {
            int extracted = handler.extract(FluidResource.of(resource), resource.getAmount(), transaction);
            if (action.execute()) {
                transaction.commit();
            }
            return resource.copyWithAmount(extracted);
        }
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {
            FluidStack extracted = ResourceHandlerUtil.extractFiltered(handler, t -> true, maxDrain, FluidResource.EMPTY, transaction, FluidResource::toStack);
            if (action.execute()) {
                transaction.commit();
            }
            return extracted;
        }
    }
}
