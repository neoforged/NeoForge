/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.capability.templates;

import java.util.Objects;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerDeprecationHandling;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.resources.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * @deprecated Use {@link EmptyResourceHandler} instead
 */
@Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
public class EmptyFluidHandler implements IResourceHandler<FluidResource>, IFluidHandler {
    /**
     * @deprecated Use {@link EmptyResourceHandler#instance()} instead
     */
    @Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
    public static final EmptyResourceHandler<FluidResource> INSTANCE = EmptyResourceHandler.instance();

    @Override
    public int getTanks() {
        return 0;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        Objects.checkIndex(tank, this.getTanks());
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        Objects.checkIndex(tank, this.getTanks());
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        Objects.checkIndex(tank, this.getTanks());
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
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
        return 0;
    }

    @Override
    public FluidResource getResource(int index) {
        Objects.checkIndex(index, size());
        return FluidResource.EMPTY;
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        return 0;
    }

    @Override
    public int getCapacity(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        return 0;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        return resource.isEmpty();
    }

    @Override
    public int characteristics() {
        return INSTANCE.characteristics();
    }

    @Override
    public int characteristics(int index) {
        return INSTANCE.characteristics(index);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        return 0;
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        return 0;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        return 0;
    }
}
