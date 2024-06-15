/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.energy.templates;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.energy.IEnergyHandler;

public class VoidEnergyHandler implements IEnergyHandler {
    public static final VoidEnergyHandler INSTANCE = new VoidEnergyHandler();

    @Override
    public int insert(int toReceive, TransferAction action) {
        return toReceive;
    }

    @Override
    public int extract(int toExtract, TransferAction action) {
        return 0;
    }

    @Override
    public int getAmount() {
        return 0;
    }

    @Override
    public int getLimit() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canInsert() {
        return true;
    }
}
