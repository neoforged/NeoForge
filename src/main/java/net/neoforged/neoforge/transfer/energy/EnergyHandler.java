/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.energy;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * A handler for the transfer and storage of energy.
 *
 * <p>This capability interface is used for NeoForge's energy system,
 * see the exposed capabilities in {@linkplain Capabilities.EnergyHandler}.
 *
 * <p>This interface can be also be used for other energy systems,
 * provided that they register a capability under a different name.
 */
public interface EnergyHandler {
    /**
     * Returns the amount of energy currently stored, as a {@code long}.
     *
     * <p>In general, energy handlers can report {@code long} amounts.
     * However, if the handler is known to only support amounts up to {@code Integer.MAX_VALUE},
     * or if the caller prefers to deal in {@code int}s only,
     * the {@linkplain #getAmountAsInt int-returning overload} can be used instead.
     *
     * <p>The returned amount must be <strong>non-negative</strong>.
     *
     * @return the amount as a long
     * @see #getAmountAsInt()
     */
    long getAmountAsLong();

    /**
     * Returns the amount of energy currently stored, as an {@code int}.
     *
     * <p>This is a convenience method to clamp the amount to an {@code int},
     * for the cases where the handler is known to only support amounts up to {@code Integer.MAX_VALUE},
     * or if the caller prefers to deal in {@code int}s only.
     *
     * <p>The returned amount must be <strong>non-negative</strong>.
     *
     * @return the amount as an {@code int}
     * @implNote This method should not be implemented. The default method will call {@link #getAmountAsLong()} and convert the result appropriately.
     * @see #getAmountAsLong() the long-returning overload
     */
    @ApiStatus.NonExtendable
    default int getAmountAsInt() {
        return Ints.saturatedCast(getAmountAsLong());
    }

    /**
     * Returns the capacity of the handler, irrespective of the current amount, as a {@code long}.
     * <p>
     * In general, energy handlers can report {@code long} capacities.
     * However, if the handler is known to only support capacities up to {@code Integer.MAX_VALUE},
     * or if the caller prefers to deal in {@code int}s only,
     * the {@linkplain #getCapacityAsInt int-returning overload} can be used instead.
     * <p>
     * This function serves as a hint on the maximum {@linkplain #getAmountAsLong() amount} the energy handler might contain,
     * for example the handler can be considered full if {@code amount >= capacity}.
     * Note that the returned capacity may overestimate the actual allowed amount, and it might be smaller than the current amount.
     * The only way to know if a handler will accept a resource, is to try to {@link #insert insert} it.
     *
     * @return the capacity, as a long
     * @see #getCapacityAsInt()
     */
    long getCapacityAsLong();

    /**
     * Returns the capacity of the handler, irrespective of the current amount, as an {@code int}.
     * <p>
     * This is a convenience method to get the capacity clamped to an {@code int},
     * for the cases where the handler is known to only support capacities up to {@code Integer.MAX_VALUE},
     * or if the caller prefers to deal in {@code int}s only.
     * <p>
     * This function serves as a hint on the maximum {@linkplain #getAmountAsInt() amount} the energy handler might contain,
     * for example the handler can be considered full if {@code amount >= capacity}.
     * Note that the returned capacity may overestimate the actual allowed amount, and it might be smaller than the current amount.
     * The only way to know if a handler will accept a resource, is to try to {@link #insert insert} it.
     *
     * @return the capacity, as an {@code int}
     * @implNote This method should not be implemented. The default method will call {@link #getCapacityAsLong()} and convert the result appropriately.
     * @see #getCapacityAsLong()
     */
    @ApiStatus.NonExtendable
    default int getCapacityAsInt() {
        return Ints.saturatedCast(getCapacityAsLong());
    }

    /**
     * Inserts up to the given amount of energy into the handler.
     *
     * <p>Changes to the handler are made in the context of a {@linkplain Transaction transaction}.
     *
     * @param amount      The maximum amount of energy to insert. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     * @return The amount that was inserted. Between {@code 0} (inclusive, nothing was inserted) and {@code amount} (inclusive, everything was inserted).
     * @throws IllegalArgumentException If the amount is negative. See also {@link TransferPreconditions#checkNonNegative} to help perform this check.
     * @implSpec Implementations must properly support {@linkplain Transaction transactions}.
     *           Note that {@link SnapshotJournal} can serve as the base class for a transaction-aware energy handler.
     */
    int insert(int amount, TransactionContext transaction);

    /**
     * Extracts up to the given amount of energy from the handler.
     *
     * <p>Changes to the handler are made in the context of a {@linkplain Transaction transaction}.
     *
     * @param amount      The maximum amount of energy to extract. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     * @return The amount that was extracted. Between {@code 0} (inclusive, nothing was extracted) and {@code amount} (inclusive, everything was extracted).
     * @throws IllegalArgumentException If the amount is negative. See also {@link TransferPreconditions#checkNonNegative} to help perform this check.
     * @implSpec Implementations must properly support {@linkplain Transaction transactions}.
     *           Note that {@link SnapshotJournal} can serve as the base class for a transaction-aware energy handler.
     */
    int extract(int amount, TransactionContext transaction);
}
