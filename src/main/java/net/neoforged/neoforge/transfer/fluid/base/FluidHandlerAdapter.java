/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluid.base;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.fluid.FluidHelper;
import net.neoforged.neoforge.transfer.fluid.FluidVariant;
import net.neoforged.neoforge.transfer.storage.Storage;

/**
 * Adapts a {@link Storage} to {@link IFluidHandler} with auto-commit behavior.
 */
@SuppressWarnings("removal")
public class FluidHandlerAdapter implements IFluidHandler {
    private final Storage<FluidVariant> storage;

    public FluidHandlerAdapter(Storage<FluidVariant> storage) {
        this.storage = storage;
    }

    @Override
    public int getTanks() {
        return storage.size();
    }

    private static int clampToMaxAmount(long amount) {
        return Math.clamp(amount, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        var resource = storage.getResource(tank);
        return resource.toStack(clampToMaxAmount(storage.getAmount(tank)));
    }

    @Override
    public int getTankCapacity(int tank) {
        return clampToMaxAmount(storage.getCapacity(tank, FluidVariant.EMPTY));
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return FluidHelper.isFluidValid(storage, tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return FluidHelper.fill(storage, resource, action.simulate());
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidHelper.drain(storage, resource, action.simulate());
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidHelper.drain(storage, maxDrain, action.simulate());
    }
}
