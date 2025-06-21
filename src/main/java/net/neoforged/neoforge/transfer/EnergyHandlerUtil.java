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

    public static boolean isEmpty(IEnergyHandler handler) {
        return checkEnergy(handler.getAmount());
    }

    public static boolean canAcceptEnergy(IEnergyHandler handler) {
        try (Transaction transaction = TransactionManager.open(TransactionContext.ROOT)) {
            return handler.insert(1, transaction) > 0;
        }
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
        float proportion = (float) handler.getAmountAsLong() / (float) handler.getCapacityAsLong();
        return Mth.lerpDiscrete(proportion, Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
    }

    /**
     * @param handler Energy Handler to iterate
     * @return Total energy stored across all of its sub-buffers. This is a long given the accumulation factor can be several max {@code ints} together.
     *         <p>
     * @deprecated Use {@link IEnergyHandler#getAmount()} or {@link IEnergyHandler#getAmountAsLong()}
     *             Deprecation for PR will be removed before final merge. We abuse 'since' to help find these (though there should only be the ones here)
     */
    @Deprecated(forRemoval = true, since = "now")
    public static long getAmount(IEnergyHandler handler) {
        return handler.getAmount();
    }

    /**
     * @param handler Energy Handler to iterate
     * @return Total capacity across all of its sub-buffers.
     * @deprecated Use {@link IEnergyHandler#getCapacity()} or {@link IEnergyHandler#getCapacityAsLong()}.
     *             Deprecation for PR will be removed before final merge. We abuse 'since' to help find these (though there should only be the ones here)
     */
    @Deprecated(forRemoval = true, since = "now")
    public static long getCapacity(IEnergyHandler handler) {
        return handler.getCapacity();
    }

    /**
     * @deprecated Use {@link IEnergyHandler#getAmountAsLong()}.
     *             Deprecation for PR will be removed before final merge. We abuse 'since' to help find these (though there should only be the ones here)
     */
    @Deprecated(forRemoval = true, since = "now")
    public static long getAmountAsLong(IEnergyHandler handler) {
        return handler.getAmountAsLong();
    }

    /**
     * @deprecated Use {@link IEnergyHandler#getCapacityAsLong()}.
     *             Deprecation for PR will be removed before final merge. We abuse 'since' to help find these (though there should only be the ones here)
     */
    @Deprecated(forRemoval = true, since = "now")
    public static long getCapacityAsLong(IEnergyHandler handler) {
        return handler.getCapacityAsLong();
    }

    /**
     * The result is expected to not be committed.
     *
     * @param handler the energy handler to calculate
     * @return The max value that energy handler could receive
     */
    public static int getMaxInsertableValue(IEnergyHandler handler) {
        try (Transaction transaction = TransactionManager.open(TransactionContext.ROOT)) {
            return handler.insert(Integer.MAX_VALUE, transaction);
        }
    }

    private EnergyHandlerUtil() {}
}
