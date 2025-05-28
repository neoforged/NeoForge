/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluid.base;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.fluid.FluidVariant;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.storage.StorageUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Adapts a {@link Storage} to {@link IFluidHandler} with auto-commit behavior.
 */
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
        return storage.isValid(tank, FluidVariant.of(stack));
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        try (var tx = Transaction.open(null)) {
            var variant = FluidVariant.of(resource);
            int result = clampToMaxAmount(storage.insert(variant, resource.getAmount(), tx));
            if (action.execute()) {
                tx.commit();
            }
            return result;
        }
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        try (var tx = Transaction.open(null)) {
            var variant = FluidVariant.of(resource);
            int result = clampToMaxAmount(storage.extract(variant, resource.getAmount(), tx));
            if (action.execute()) {
                tx.commit();
            }
            return variant.toStack(result);
        }
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        try (var tx = Transaction.open(null)) {
            var extracted = StorageUtil.extractAny(storage, maxDrain, tx);
            if (extracted == null) {
                return FluidStack.EMPTY;
            }
            if (extracted.amount() > maxDrain) {
                throw new IllegalStateException("Extracted more (" + extracted
                        + ") from storage (" + storage + ") than requested (" + maxDrain + ").");
            }
            if (action.execute()) {
                tx.commit();
            }
            return extracted.resource().toStack((int) extracted.amount());
        }
    }
}
