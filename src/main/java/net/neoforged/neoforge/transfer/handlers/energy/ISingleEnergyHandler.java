/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.energy;

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
    /**
     * Inserts a given amount into the handler. Distribution is up to the handler.
     *
     * @param amount      The amount to insert.
     * @param transaction the transaction chain that the insertion is part of. The developer is expected to handle snapshotting as necessary to handle rollbacks when the transaction is not committed.
     * @return The amount that was inserted. This should be non-negative.
     */
    @Override
    int insert(int amount, TransactionContext transaction);

    /**
     * Extracts a given amount from the handler. Distribution is up to the handler.
     *
     * @param amount      The amount of energy to extract.
     * @param transaction the transaction chain that the extraction is part of. The developer is expected to handle snapshotting as necessary to handle rollbacks when the transaction is not committed.
     * @return The amount that was extracted. This should be non-negative.
     */
    @Override
    int extract(int amount, TransactionContext transaction);

    /**
     * @return {@code true} if at any point your handler can be inserted into, {@code false} otherwise. This should not be called in your insert method.
     */
    @Override
    boolean supportsInsertion();

    /**
     * @return {@code true} if at any point your handler can be extracted from, {@code false} otherwise. This should not be called in your extract method.
     */
    @Override
    boolean supportsExtraction();

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
    default boolean supportsInsertion(int index) {
        return supportsInsertion();
    }

    @ApiStatus.NonExtendable
    @Override
    default boolean supportsExtraction(int index) {
        return supportsExtraction();
    }

    @ApiStatus.NonExtendable
    @Override
    default int insert(int index, int amount, TransactionContext transaction) {
        return insert(amount, transaction);
    }

    @ApiStatus.NonExtendable
    @Override
    default int extract(int index, int amount, TransactionContext transaction) {
        return extract(amount, transaction);
    }
}
