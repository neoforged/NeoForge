/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.energy;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

@Deprecated(since = "1.21.9", forRemoval = true)
class EnergyHandlerAdapter implements IEnergyStorage {
    private final EnergyHandler handler;

    EnergyHandlerAdapter(EnergyHandler handler) {
        this.handler = handler;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        try (var tx = Transaction.openRoot()) {
            int inserted = handler.insert(toReceive, tx);
            if (!simulate) tx.commit();
            return inserted;
        }
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        try (var tx = Transaction.openRoot()) {
            int extracted = handler.extract(toExtract, tx);
            if (!simulate) tx.commit();
            return extracted;
        }
    }

    @Override
    public int getEnergyStored() {
        return handler.getAmountAsInt();
    }

    @Override
    public int getMaxEnergyStored() {
        return handler.getCapacityAsInt();
    }

    @Override
    public boolean canExtract() {
        // Make a best guess here
        return handler.getCapacityAsLong() > 0;
    }

    @Override
    public boolean canReceive() {
        // Make a best guess here
        return handler.getCapacityAsLong() > 0;
    }
}
