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
import net.neoforged.neoforge.transfer.handlers.TransferCharacteristics;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
import net.neoforged.neoforge.transfer.transaction.UnsafeTransactionManager;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * Utility class for handling various {@link IEnergyHandler} interactions
 */
public final class EnergyHandlerUtil {
    /**
     * Moves energy between two handlers, and return the amount that was successfully transferred.
     *
     * @param from        The source handler. Will no-op if null.
     * @param to          The target handler. Will no-op if null.
     * @param amount      The maximum amount that will be transferred.
     * @param transaction The transaction this transfer is part of, or {@code null} if a transaction should be opened just for this transfer.
     * @return The total amount of energy that was successfully transferred.
     * @throws IllegalStateException If no transaction is passed.
     * @throws ReportedException     If amount is negative.
     */
    public static int move(
            @Nullable IEnergyHandler from, @Nullable IEnergyHandler to,
            int amount,
            @Nullable TransactionContext transaction) {
        if (TransferPreconditions.checkNonNegative(amount) == 0) return 0;
        if (from == null || to == null) return 0;

        //Test if the `from` handler has the extraction characteristic (or is unknown).
        //While this is not strictly necessary, it can reduce our iteration loop cost
        if (!from.hasCharacteristics(TransferCharacteristics.EXTRACTABLE)) return 0;

        //Test if the `to` handler has the insertion characteristic (or is unknown).
        //While this is not strictly necessary, it can reduce our iteration loop cost
        if (!to.hasCharacteristics(TransferCharacteristics.INSERTABLE)) return 0;

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
        var capacity = handler.getCapacityAsLong();
        if (capacity == 0) return Redstone.SIGNAL_NONE;
        var amount = handler.getAmountAsLong();
        if (amount == 0) return Redstone.SIGNAL_NONE;
        float proportion = (float) amount / (float) capacity;
        return Mth.lerpDiscrete(proportion, Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
    }

    /**
     * Returns if the specified {@code IEnergyHandler} could accept energy. The transaction used is not committed.
     *
     * @param handler The energy handler to check
     * @return {@code true} if any energy could be accepted by the handler, otherwise {@code false}.
     */
    public static boolean canAcceptEnergy(IEnergyHandler handler) {
        return getInsertableAmount(handler) > 0;
    }

    /**
     * Returns the maximum value the specified {@code IEnergyHandler} could accept. The transaction used is not committed.
     *
     * @param handler the energy handler to check
     * @return The max value that energy handler could receive
     */
    public static int getInsertableAmount(IEnergyHandler handler) {
        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {
            return handler.insert(Integer.MAX_VALUE, transaction);
        }
    }

    /**
     * Returns the maximum value the specified {@code IEnergyHandler} could provide. The transaction used is not committed.
     *
     * @param handler the energy handler to check
     * @return The max value that energy handler could provide
     */
    public static int getExtractableAmount(IEnergyHandler handler) {
        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {
            return handler.insert(Integer.MAX_VALUE, transaction);
        }
    }

    private EnergyHandlerUtil() {}
}
