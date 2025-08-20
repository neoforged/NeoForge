/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.energy;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * A capability interface providing the methods such as insert/extract a buffered energy amount for a handler.<br>
 * To use Neo's energy capability see the {@link Capabilities.EnergyHandler#BLOCK EnergyCapability} in {@link Capabilities Capabilities}.
 * <br>
 * To make your own energy system using this interface, you can register a new capability using something like the following.
 *
 * <pre>
 * {@code public static final BlockCapability<IEnergyHandler, @Nullable Direction> BLOCK = BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath([MOD_ID], [CUSTOM_ENERGY_NAME]), IEnergyHandler.class);}
 * </pre>
 *
 * <p>
 * This would effectively create a new capability that other mods could utilize so long as they create a new capability with the same id without needing any extra API provided by you.
 */
public interface EnergyHandler {
    /**
     * Returns the amount of energy currently stored, as an {@code int}.
     *
     * <p>This is a convenience method to clamp the amount to an {@code int},
     * for the cases where the handler is known to only support amounts up to {@code Integer.MAX_VALUE},
     * or if the caller prefers to deal in {@code int}s only.
     *
     * <p>The returned amount must be <strong>non-negative</strong>, and should never surpass the {@linkplain #getCapacityAsInt capacity}.
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
     * Returns the amount of energy currently stored, as a {@code long}.
     *
     * <p>In general, energy handlers can report {@code long} amounts.
     * However, if the handler is known to only support amounts up to {@code Integer.MAX_VALUE},
     * or if the caller prefers to deal in {@code int}s only,
     * the {@linkplain #getAmountAsInt int-returning overload} can be used instead.
     *
     * <p>The returned amount must be <strong>non-negative</strong>, and should never surpass the {@linkplain #getCapacityAsLong capacity}.
     *
     * @return the amount as a long
     * @see #getAmountAsInt()
     */
    long getAmountAsLong();

    /**
     * Returns the capacity of the handler, irrespective of the current amount, as an {@code int}.
     * <p>
     * This is a convenience method to get the capacity clamped to an {@code int},
     * for the cases where the handler is known to only support capacities up to {@code Integer.MAX_VALUE},
     * or if the caller prefers to deal in {@code int}s only.
     * <p>
     * This function serves as metadata only, and its result might be approximate.
     * The only way to know if a handler will accept a resource, is to try to {@linkplain #insert insert} it.
     * <p>
     * The capacity should be greater than or equal to the {@linkplain #getAmountAsInt() amount}.
     *
     * @return the capacity, as an {@code int}
     * @implNote This method should not be implemented. The default method will call {@link #getCapacityAsLong()} and convert the result appropriately.
     * @see #getCapacityAsLong()
     */
    @ApiStatus.NonExtendable
    default long getCapacityAsInt() {
        return getCapacityAsLong();
    }

    /**
     * Returns the capacity of the handler, irrespective of the current amount, as a {@code long}.
     * <p>
     * In general, energy handlers can report {@code long} capacities.
     * However, if the handler is known to only support capacities up to {@code Integer.MAX_VALUE},
     * or if the caller prefers to deal in {@code int}s only,
     * the {@linkplain #getCapacityAsInt int-returning overload} can be used instead.
     * <p>
     * This function serves as metadata only, and its result might be approximate.
     * The only way to know if a handler will accept energy, is to try to {@link #insert insert} it.
     * <p>
     * The capacity should be greater than or equal to
     * the {@linkplain #getAmountAsLong() amount}.
     *
     * @return the capacity, as a long
     * @see #getCapacityAsInt()
     */
    long getCapacityAsLong();

    /**
     * Inserts up to the given amount of energy into the handler.
     *
     * <p>Changes to the handler are made in the context of a {@linkplain Transaction transaction}.
     *
     * @param amount      The maximum amount of energy to insert. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     * @return A non-negative integer not greater than {@code amount}: the amount that was inserted.
     * @throws IllegalArgumentException when amount is negative
     * @implSpec Must properly support {@linkplain Transaction transactions}.
     * @implNote {@link SnapshotJournal} can serve as the base class for a transaction-aware energy handler.
     */
    int insert(int amount, TransactionContext transaction);

    /**
     * Extracts up to the given amount of energy from the handler.
     *
     * <p>Changes to the handler are made in the context of a {@linkplain Transaction transaction}.
     *
     * @param amount      The maximum amount of energy to extract. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     * @return A non-negative integer not greater than {@code amount}: the amount that was extracted.
     * @throws IllegalArgumentException when amount is negative
     * @implSpec Implementations must properly support {@linkplain Transaction transactions}.
     * @implNote {@link SnapshotJournal} can serve as the base class for a transaction-aware energy handler.
     */
    int extract(int amount, TransactionContext transaction);
}
