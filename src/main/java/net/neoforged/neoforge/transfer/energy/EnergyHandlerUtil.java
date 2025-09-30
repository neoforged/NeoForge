/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.energy;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.redstone.Redstone;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for handling various {@link EnergyHandler} interactions
 */
public final class EnergyHandlerUtil {
    /**
     * Checks if an {@link EnergyHandler} is full.
     * <p>
     * An energy handler is considered full if its {@linkplain EnergyHandler#getAmountAsLong() amount}
     * is greater than or equal to its {@linkplain EnergyHandler#getCapacityAsLong() capacity}.
     *
     * @param handler the {@link EnergyHandler} to check
     * @return {@code true} if the {@link EnergyHandler} is full, {@code false} otherwise
     */
    public static boolean isFull(EnergyHandler handler) {
        return handler.getAmountAsLong() >= handler.getCapacityAsLong();
    }

    /**
     * Calculates the redstone signal strength based on the given energy handler's content. This value is between 0 and 15.
     * <p>This method is based on {@link AbstractContainerMenu#getRedstoneSignalFromContainer(Container)}.
     *
     * @param handler the energy handler to calculate the signal from
     * @return the redstone signal strength
     */
    public static int getRedstoneSignalFromEnergyHandler(EnergyHandler handler) {
        long amount = handler.getAmountAsLong();
        if (amount == 0) {
            return Redstone.SIGNAL_NONE;
        }
        long capacity = handler.getCapacityAsLong();
        if (capacity == 0) {
            return Redstone.SIGNAL_NONE;
        }
        return Mth.lerpDiscrete(
                // Clamp to 1 to avoid increasing the signal strength beyond 15
                Math.min(1.0f, (float) amount / capacity),
                Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
    }

    /**
     * Moves energy between two handlers, and return the amount that was successfully transferred.
     *
     * @param from        The source handler. Will no-op if null.
     * @param to          The target handler. Will no-op if null.
     * @param amount      The maximum amount that will be transferred.
     * @param transaction The transaction this transfer is part of, or {@code null} if a transaction should be opened just for this transfer.
     * @return The total amount of energy that was successfully transferred.
     * @throws IllegalStateException    If no transaction is passed.
     * @throws IllegalArgumentException If amount is negative.
     */
    public static int move(
            @Nullable EnergyHandler from, @Nullable EnergyHandler to,
            int amount,
            @Nullable TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (from == null || to == null || amount == 0) return 0;

        try (Transaction subTransaction = Transaction.open(transaction)) {
            int maxExtracted;
            try (Transaction simulatedExtract = Transaction.open(subTransaction)) {
                maxExtracted = from.extract(amount, simulatedExtract);
            }

            if (maxExtracted == 0) return 0;

            // check how much can be inserted
            int inserted = to.insert(maxExtracted, subTransaction);

            // extract it, or rollback if we cannot actually extract the amount we inserted
            // this can happen even for a well-behaving handler if it only supports extracting the exact
            // amount we previously simulated, but the destination only accepted less.
            if (inserted != from.extract(inserted, subTransaction)) {
                return 0;
            }

            subTransaction.commit();
            return inserted;
        } catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Moving energy between handlers");
            //noinspection DataFlowIssue
            report.addCategory("Move details")
                    .setDetail("Input", from::toString)
                    .setDetail("Output", to::toString)
                    .setDetail("Amount", amount)
                    .setDetail("Transaction", transaction);
            throw new ReportedException(report);
        }
    }

    private EnergyHandlerUtil() {}
}
