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
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
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
    public static long getAmount(IEnergyHandler handler) {
        var sum = 0;

        var size = handler.size();
        for (var i = 0; i < size; i++) {
            //this can only ever be 1/4billionth max long
            sum += handler.getAmount(i);
        }
        return sum;
    }

    public static boolean isEmpty(IEnergyHandler handler) {
        return getAmount(handler) == 0;
    }

    public static boolean canAcceptEnergy(IEnergyHandler handler) {
        try (var transaction = TransactionManager.open()) {
            return handler.insert(1, transaction) > 0;
        }
    }

    /**
     * @param handler Energy Handler to iterate
     * @return Total capacity across all of its sub-buffers.
     */
    public static long getCapacity(IEnergyHandler handler) {
        var sum = 0;
        var size = handler.size();
        for (var i = 0; i < size; i++) {
            //this can only ever be 1/4billionth max long
            sum += handler.getCapacity(i);
        }
        return sum;
    }

    public static long getAmountAsLong(IEnergyHandler handler) {
        var sum = 0L;
        var size = handler.size();
        for (var i = 0; i < size; i++) {
            sum += handler.getAmountAsLong(i);
            if (sum < 0) return Long.MAX_VALUE;
        }
        return sum;
    }

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
    public static int move(
            @Nullable IEnergyHandler from, @Nullable IEnergyHandler to,
            int amount,
            @Nullable TransactionContext transaction) {
        if (from == null || to == null) return 0;

        try (var subTransaction = TransactionManager.open(transaction)) {
            var handledAmount = 0;
            try (var simulate = subTransaction.open()) {
                var extracted = from.extract(amount, simulate);
                var inserted = to.insert(extracted, simulate);
                handledAmount = Math.min(extracted, inserted);
            }

            var extracted = from.extract(handledAmount, subTransaction);
            if (to.insert(extracted, subTransaction) == extracted) {
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
