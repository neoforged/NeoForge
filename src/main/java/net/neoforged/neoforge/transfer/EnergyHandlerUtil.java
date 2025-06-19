/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.redstone.Redstone;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * Utility class for handling various {@link IEnergyHandler} interactions
 */
public final class EnergyHandlerUtil {
    /**
     * @return True if the specified amount is 0 and should skip being processed.
     * @throws ReportedException when amount is negative.
     */
    public static boolean checkEnergy(int amount) {
        if (amount < 0) {
            CrashReport report = CrashReport.forThrowable(new IllegalArgumentException("Amount must be non-negative"), "Energy amount was negative");
            report.addCategory("EnergyHandlerUtil#isEmpty")
                    .setDetail("Amount", amount);
            throw new ReportedException(report);
        }
        return amount == 0;
    }

    /**
     * @param handler Energy Handler to iterate
     * @return Total energy stored across all of its sub-buffers. This is a long given the accumulation factor can be several max {@code ints} together.
     */
    public static long getAmount(IEnergyHandler handler) {
        int sum = 0;

        int size = handler.size();
        for (int i = 0; i < size; i++) {
            //this can only ever be 1/4billionth max long
            sum += handler.getAmount(i);
        }
        return sum;
    }

    public static boolean isEmpty(IEnergyHandler handler) {
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            if (handler.getAmount(i) > 0) return false;
        }
        return true;
    }

    public static boolean canAcceptEnergy(IEnergyHandler handler) {
        try (Transaction transaction = TransactionManager.open(TransactionContext.ROOT)) {
            return handler.insert(1, transaction) > 0;
        }
    }

    /**
     * @param handler Energy Handler to iterate
     * @return Total capacity across all of its sub-buffers.
     */
    public static long getCapacity(IEnergyHandler handler) {
        int sum = 0;
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            //this can only ever be 1/4billionth max long
            sum += handler.getCapacity(i);
        }
        return sum;
    }

    public static long getAmountAsLong(IEnergyHandler handler) {
        long sum = 0L;
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            sum += handler.getAmountAsLong(i);
            if (sum < 0) return Long.MAX_VALUE;
        }
        return sum;
    }

    public static long getCapacityAsLong(IEnergyHandler handler) {
        long sum = 0L;
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            sum += handler.getCapacityAsLong(i);
            if (sum < 0) return Long.MAX_VALUE;
        }
        return sum;
    }

    /**
     * Moves energy between two handlers, and return the amount that was successfully transferred.
     *
     * @param from        The source handler. Will no-op if null.
     * @param to          The target handler. Will no-op if null.
     * @param amount      The maximum amount that will be transferred.
     * @param transaction The transaction this transfer is part of, or {@code null} if a transaction should be opened just for this transfer.
     * @return The total amount of energy that was successfully transferred.
     * @throws IllegalStateException If no transaction is passed and a transaction is already active on the current thread.
     */
    public static int move(
            @Nullable IEnergyHandler from, @Nullable IEnergyHandler to,
            int amount,
            @Nullable TransactionContext transaction) {
        if (from == null || to == null) return 0;
        if (checkEnergy(amount)) return 0;

        try (Transaction subTransaction = TransactionManager.open(transaction)) {
            int extractableAmount;
            try (Transaction simulatedTransaction = TransactionManager.open(subTransaction)) {
                extractableAmount = from.extract(amount, simulatedTransaction);
                //Don't commit. This will revert the extraction to allow work with the amount we "simulated".
            }

            int inserted = to.insert(extractableAmount, subTransaction);
            int extracted = from.extract(inserted, subTransaction);
            //Check to be sure the amount we inserted is able to be fully extracted before committing.
            if (extracted == inserted) {
                subTransaction.commit();
                return extracted;
            }
            return 0;
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

    /**
     * Calculates the redstone signal strength based on the given resource handler. This value is between 0 and 15.
     * This method is based on {@link AbstractContainerMenu#getRedstoneSignalFromContainer(Container)}
     *
     * @param handler the energy handler to calculate the signal from
     * @return the redstone signal strength
     */
    @Range(from = Redstone.SIGNAL_NONE, to = Redstone.SIGNAL_MAX)
    public static int getRedstoneSignalStrength(IEnergyHandler handler) {
        float proportion = (float) getAmountAsLong(handler) / (float) getCapacityAsLong(handler);
        return Mth.lerpDiscrete(proportion, Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
    }

    private EnergyHandlerUtil() {}
}
