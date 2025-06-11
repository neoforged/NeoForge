/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import javax.annotation.Nonnegative;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.redstone.Redstone;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * Utility class for handling various {@link IEnergyHandler} interactions
 */
public final class EnergyHandlerUtil {
    /**
     * @param handler Energy Handler to iterate
     * @return Total energy stored across all of its sub-buffers. This is a long given the accumulation factor can be several max {@code ints} together.
     */
    @Nonnegative
    public static long getAmount(IEnergyHandler handler) {
        var sum = 0;

        var size = handler.size();
        for (var i = 0; i < size; i++) {
            //this can only ever be 1/4billionth max long
            sum += handler.getAmount(i);
        }
        return sum;
    }

    /**
     * @param handler Energy Handler to iterate
     * @return Total capacity across all of its sub-buffers.
     */
    @Nonnegative
    public static long getCapacity(IEnergyHandler handler) {
        var sum = 0;
        var size = handler.size();
        for (var i = 0; i < size; i++) {
            //this can only ever be 1/4billionth max long
            sum += handler.getCapacity(i);
        }
        return sum;
    }

    @Nonnegative
    public static long getAmountAsLong(IEnergyHandler handler) {
        var sum = 0L;
        var size = handler.size();
        for (var i = 0; i < size; i++) {
            sum += handler.getAmountAsLong(i);
            if (sum < 0) return Long.MAX_VALUE;
        }
        return sum;
    }

    @Nonnegative
    public static long getCapacityAsLong(IEnergyHandler handler) {
        var sum = 0L;
        var size = handler.size();
        for (var i = 0; i < size; i++) {
            sum += handler.getCapacityAsLong(i);
            if (sum < 0) return Long.MAX_VALUE;
        }
        return sum;
    }

    /**
     * Move resources between two storages, matching the passed filter, and return the amount that was successfully transferred.
     *
     * @param from        The source handler. May be null.
     * @param to          The target handler. May be null.
     * @param amount      The maximum amount that will be transferred.
     * @param transaction The transaction this transfer is part of, or {@code null} if a transaction should be opened just for this transfer.
     * @return The total amount of resources that was successfully transferred. This number is not necessarily for one resource, as we only pass in a filter. It is intended to be used to determine a raw number of resources moved.
     * @throws IllegalStateException If no transaction is passed and a transaction is already active on the current thread.
     */
    @Nonnegative
    public static int move(
            @Nullable IEnergyHandler from,
            @Nullable IEnergyHandler to,
            @Nonnegative int amount,
            @Nullable TransactionContext transaction) {
        if (from == null || to == null) return 0;

        try (Transaction subTransaction = Transaction.open(transaction)) {
            int totalMoved = 0;
            int size = from.size();

            for (int index = 0; index < size; ++index) {
                // check how much can be extracted
                int maxExtracted;
                try (var simulatedExtract = Transaction.open(subTransaction)) {
                    maxExtracted = from.extract(index, amount - totalMoved, simulatedExtract);
                }

                try (Transaction transferTransaction = Transaction.open(subTransaction)) {
                    // check how much can be inserted
                    var inserted = to.insert(maxExtracted, transferTransaction);

                    // extract it, or rollback if the amounts don't match
                    if (from.extract(index, inserted, transferTransaction) == inserted) {
                        totalMoved += inserted;
                        transferTransaction.commit();
                    }
                }

                if (amount == totalMoved) {
                    // early return if nothing can be moved anymore
                    subTransaction.commit();
                    return totalMoved;
                }
            }

            subTransaction.commit();
            return totalMoved;
        } catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Moving resources between storages");
            //noinspection DataFlowIssue
            report.addCategory("Move details")
                    .setDetail("Input storage", from::toString)
                    .setDetail("Output storage", to::toString)
                    .setDetail("Max amount", amount)
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
        float proportion = 0.0F;
        int size = handler.size();

        for (int index = 0; index < size; ++index) {
            int indexFill = handler.getAmount(index);
            if (indexFill > 0)
                proportion += (float) indexFill / handler.getCapacity(index);
        }

        proportion /= size;

        return Mth.lerpDiscrete(proportion, Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
    }

    private EnergyHandlerUtil() {}
}
