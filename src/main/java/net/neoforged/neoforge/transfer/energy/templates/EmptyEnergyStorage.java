/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.energy.templates;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.energy.IEnergyStorage;

/**
 * Implementation of {@link IEnergyStorage} that cannot store, receive, or provide energy.
 * Use the {@link #INSTANCE}, don't instantiate. Example:
 * 
 * <pre>{@code
 * ItemStack stack = ...;
 * IEnergyStorage storage = stack.getCapability(ForgeCapabilities.ENERGY).orElse(EmptyEnergyStorage.INSTANCE);
 * // Use storage without checking whether it's present.
 * }</pre>
 */
public class EmptyEnergyStorage implements IEnergyStorage {
    public static final EmptyEnergyStorage INSTANCE = new EmptyEnergyStorage();

    protected EmptyEnergyStorage() {}

    @Override
    public int insert(int maxReceive, TransferAction action) {
        return 0;
    }

    @Override
    public int extract(int maxExtract, TransferAction action) {
        return 0;
    }

    @Override
    public int getAmount() {
        return 0;
    }

    @Override
    public int getLimit() {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canInsert() {
        return false;
    }
}
