/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.energy;

import net.neoforged.neoforge.transfer.ResourceHandlerDeprecationHandling;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.handlers.templates.energy.EmptyEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Implementation of {@link IEnergyStorage} that cannot store, receive, or provide energy.
 * Use the {@link #INSTANCE}, don't instantiate. Example:
 *
 * <pre>{@code
 * ItemStack stack = ...;
 * IEnergyStorage storage = stack.getCapability(ForgeCapabilities.ENERGY).orElse(EmptyEnergyStorage.INSTANCE);
 * // Use storage without checking whether it's present.
 * }</pre>
 *
 * @deprecated the new empty is {@link EmptyEnergyHandler}
 */
@Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
public final class EmptyEnergyStorage implements IEnergyStorage, IEnergyHandler {
    /**
     * @deprecated Use {@link EmptyEnergyHandler#INSTANCE} instead.
     */
    public static final EmptyEnergyStorage INSTANCE = new EmptyEnergyStorage();

    private EmptyEnergyStorage() {}

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    //EnergyHandler temporary implementation
    @Override
    public int getAmount() {
        return EmptyEnergyHandler.INSTANCE.getAmount();
    }

    @Override
    public int getCapacity() {
        return EmptyEnergyHandler.INSTANCE.getCapacity();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return EmptyEnergyHandler.INSTANCE.insert(amount, transaction);
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return EmptyEnergyHandler.INSTANCE.extract(amount, transaction);
    }

    @Override
    public int characteristics() {
        return EmptyEnergyHandler.INSTANCE.characteristics();
    }
}
