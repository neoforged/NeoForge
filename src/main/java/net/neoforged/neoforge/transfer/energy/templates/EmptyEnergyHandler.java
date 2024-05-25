/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.energy.templates;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.energy.IEnergyHandler;

/**
 * Implementation of {@link IEnergyHandler} that cannot store, receive, or provide energy.
 * Use the {@link #INSTANCE}, don't instantiate. Example:
 * 
 * <pre>{@code
 * ItemStack stack = ...;
 * IEnergyStorage storage = stack.getCapability(ForgeCapabilities.ENERGY).orElse(EmptyEnergyStorage.INSTANCE);
 * // Use storage without checking whether it's present.
 * }</pre>
 */
public class EmptyEnergyHandler implements IEnergyHandler {
    public static final EmptyEnergyHandler INSTANCE = new EmptyEnergyHandler();

    protected EmptyEnergyHandler() {}

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
