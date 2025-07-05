/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.capability.templates;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerDeprecationHandling;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.resources.VoidResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * @deprecated Use {@link VoidResourceHandler}
 */
@Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
public class VoidFluidHandler implements IFluidHandler, ISingleResourceHandler<FluidResource> {
    /**
     * @deprecated Use {@link VoidResourceHandler#FLUID}
     */
    @Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
    public static final IResourceHandler<FluidResource> INSTANCE = VoidResourceHandler.FLUID;

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return resource.getAmount();
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public int size() {
        return VoidResourceHandler.FLUID.size();
    }

    @Override
    public FluidResource getResource(int index) {
        return VoidResourceHandler.FLUID.getResource(index);
    }

    @Override
    public int getAmount(int index) {
        return VoidResourceHandler.FLUID.getAmount(index);
    }

    @Override
    public int characteristics(int index) {
        return VoidResourceHandler.FLUID.characteristics(index);
    }

    @Override
    public int characteristics() {
        return VoidResourceHandler.FLUID.characteristics();
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        return VoidResourceHandler.FLUID.insert(resource, amount, transaction);
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        return VoidResourceHandler.FLUID.extract(resource, amount, transaction);
    }

    @Override
    public int getCapacity(int index, FluidResource resource) {
        return VoidResourceHandler.FLUID.getCapacity(index, resource);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return VoidResourceHandler.FLUID.isValid(index, resource);
    }
}
