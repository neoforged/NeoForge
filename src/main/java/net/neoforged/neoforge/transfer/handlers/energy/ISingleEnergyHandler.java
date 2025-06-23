/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.energy;

import java.util.Objects;
import net.neoforged.neoforge.transfer.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * A single buffer or indexed energy handler also referred to as a `slotless buffer`. For older mods, this should be rather familiar to you when implementing.
 * <p>
 * <strong>Important:</strong> it is advised not to use {@code instanceof} checks to determine the index count, just call size() to determine how many indices an {@link IEnergyHandler} has or use the index-less methods available either on the handler or in the {@link EnergyHandlerUtil EnergyHandler Util}.
 * With this and the utilities provided, you shouldn't need to interact with the indices yourself should you not want to.
 */
public interface ISingleEnergyHandler extends IEnergyHandler {
    @Override
    int getAmount();

    @Override
    int getCapacity();

    @Override
    int insert(int amount, TransactionContext transaction);

    @Override
    int extract(int amount, TransactionContext transaction);

    @Override
    boolean supportsInsertion();

    @Override
    boolean supportsExtraction();

    // This is defaulted but overriding is fine
    @Override
    default long getAmountAsLong() {
        return getAmount();
    }

    // This is defaulted but overriding is fine
    @Override
    default long getCapacityAsLong() {
        return getCapacity();
    }

    /**
     * @return When implementing {@link ISingleEnergyHandler} the return should always be 1. If you find you need additional indices, please implement {@link IEnergyHandler} instead
     */
    @ApiStatus.NonExtendable
    @Override
    default int size() {
        return 1;
    }

    @ApiStatus.NonExtendable
    @Override
    default int getAmount(int index) {
        Objects.checkIndex(index, size());
        return getAmount();
    }

    @ApiStatus.NonExtendable
    @Override
    default long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        return getAmountAsLong();
    }

    @ApiStatus.NonExtendable
    @Override
    default int getCapacity(int index) {
        Objects.checkIndex(index, size());
        return getCapacity();
    }

    @ApiStatus.NonExtendable
    @Override
    default long getCapacityAsLong(int index) {
        Objects.checkIndex(index, size());
        return getCapacityAsLong();
    }

    @ApiStatus.NonExtendable
    @Override
    default boolean supportsInsertion(int index) {
        Objects.checkIndex(index, size());
        return supportsInsertion();
    }

    @ApiStatus.NonExtendable
    @Override
    default boolean supportsExtraction(int index) {
        Objects.checkIndex(index, size());
        return supportsExtraction();
    }

    @ApiStatus.NonExtendable
    @Override
    default int insert(int index, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        if (EnergyHandlerUtil.checkEnergy(amount)) return 0;
        return insert(amount, transaction);
    }

    @ApiStatus.NonExtendable
    @Override
    default int extract(int index, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        if (EnergyHandlerUtil.checkEnergy(amount)) return 0;
        return extract(amount, transaction);
    }
}
