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

    protected EmptyEnergyStorage() {}

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

    @Override
    public int size() {
        return EmptyEnergyHandler.INSTANCE.size();
    }

    @Override
    public int getAmount(int index) {
        return EmptyEnergyHandler.INSTANCE.getAmount(index);
    }

    @Override
    public int getCapacity(int index) {
        return EmptyEnergyHandler.INSTANCE.getCapacity(index);
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return EmptyEnergyHandler.INSTANCE.insert(amount, transaction);
    }

    @Override
    public int extract(int index, int amount, TransactionContext transaction) {
        return EmptyEnergyHandler.INSTANCE.extract(index, amount, transaction);
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return EmptyEnergyHandler.INSTANCE.extract(amount, transaction);
    }

    @Override
    public boolean supportsInsertion() {
        return EmptyEnergyHandler.INSTANCE.supportsInsertion();
    }

    @Override
    public boolean supportsInsertion(int index) {
        return EmptyEnergyHandler.INSTANCE.supportsInsertion(index);
    }

    @Override
    public boolean supportsExtraction() {
        return EmptyEnergyHandler.INSTANCE.supportsExtraction();
    }

    @Override
    public boolean supportsExtraction(int index) {
        return EmptyEnergyHandler.INSTANCE.supportsExtraction(index);
    }

    @Override
    public int insert(int index, int amount, TransactionContext transaction) {
        return EmptyEnergyHandler.INSTANCE.insert(index, amount, transaction);
    }
}
