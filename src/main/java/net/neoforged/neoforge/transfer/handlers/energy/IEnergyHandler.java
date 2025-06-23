/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.energy;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.handlers.ITransactionHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

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
public interface IEnergyHandler extends ITransactionHandler {
    /**
     * @return The number of indices this handler manages. You shouldn't return 0 where avoidable, but this is allowed in cases there are no buffers such as the {@link net.neoforged.neoforge.transfer.handlers.templates.energy.EmptyEnergyHandler Empty handler}.
     *         <p>
     *         <strong>Must be non-negative</strong>
     */
    int size();

    /**
     * It should be expected that {@code getAmount(index) <= getCapacity(index)}.
     *
     * @param index The index to get the amount from. <strong>Must be non-negative</strong>
     * @return The amount of energy stored at the given index. <strong>Must be non-negative</strong>
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #getAmountAsLong(int)
     */
    int getAmount(int index);

    /**
     * The total amount of energy available in this handler.
     * <p>
     * It should be expected that {@code getAmount() <= getCapacity()}.
     *
     * @return The amount of energy stored in the handler across all indices. <strong>Must be non-negative</strong>
     * @see #getAmountAsLong(int)
     */
    default int getAmount() {
        long sum = 0;

        int size = size();
        for (int i = 0; i < size; i++) {
            sum += getAmount(i);
            if (sum >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        }
        return Ints.saturatedCast(sum);
    }

    /**
     * This is an optional method that provides the ability to query the contents up to a long should the internals allow for it.
     * This is only needed to be overridden should you store more than an int in a given index.
     *
     * @param index The index to get the amount from. <strong>Must be non-negative</strong>
     * @return The amount of energy stored at the given index. <strong>Must be non-negative</strong>
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #getAmount(int)
     */
    default long getAmountAsLong(int index) {
        return getAmount(index);
    }

    /**
     * This is an optional method that provides the ability to query the contents up to a long should the internals allow for it.
     * This is only needed to be overridden should you store more than an int.
     *
     * @return The amount of energy stored at the given index. <strong>Must be non-negative</strong>
     * @see #getAmount(int)
     */
    default long getAmountAsLong() {
        long sum = 0;

        int size = size();
        for (int i = 0; i < size; i++) {
            sum += getAmountAsLong(i);
            if (sum < 0) return Long.MAX_VALUE;
        }
        return sum;
    }

    /**
     * Gets the capacity that index can hold.
     *
     * @param index The index to get the limit from. <strong>Must be non-negative</strong>
     * @return The capacity at the given index. <strong>Must be non-negative</strong>
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #getCapacityAsLong(int)
     */
    int getCapacity(int index);

    /**
     * Gets the capacity the handler can hold.
     *
     * @return The capacity of the handler across all indices. <strong>Must be non-negative</strong>
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #getCapacityAsLong(int)
     */
    default int getCapacity() {
        long sum = 0;

        int size = size();
        for (int i = 0; i < size; i++) {
            sum += getCapacity(i);
            if (sum >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        }
        return Ints.saturatedCast(sum);
    }

    /**
     * This is an optional method that provides the ability to query the capacity up to a long should the internals allow for it.
     * This is only needed to be overridden should you be able to store more than an int in a given index.
     *
     * @param index The index to get the amount from. <strong>Must be non-negative</strong>
     * @return The amount of energy that can be stored at the given index. <strong>Must be non-negative</strong>
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #getCapacity(int)
     */
    default long getCapacityAsLong(int index) {
        return getCapacity(index);
    }

    /**
     * This is an optional method that provides the ability to query the capacity up to a long should the internals allow for it.
     * This is only needed to be overridden should you be able to store more than an int.
     *
     * @return The amount of energy that can be stored in the handler. <strong>Must be non-negative</strong>
     * @see #getCapacity(int)
     */
    default long getCapacityAsLong() {
        long sum = 0;

        int size = size();
        for (int i = 0; i < size; i++) {
            sum += getCapacityAsLong(i);
            if (sum < 0) return Long.MAX_VALUE;
        }
        return sum;
    }

    /**
     * <strong>Note:</strong> It is advised to override this and return true or false if you already know the handler will have a possible true result.
     *
     * @return True if the handler can be inserted into at this time, false otherwise. If using indices, this should return true if any index allows insertion
     * @see #supportsInsertion(int)
     */
    default boolean supportsInsertion() {
        var indices = size();
        for (var index = 0; index < indices; index++) {
            if (supportsInsertion(index)) return true;
        }
        return false;
    }

    /**
     * <b>IMPORTANT:</b> This doesn't add any control, this is merely a guide for things like pipes to know ahead of time if it can be ever inserted into when the capability invalidates for example.
     * You shouldn't call this in your own {@link IEnergyHandler#insert} method, but you still need to handle the result if insert wouldn't fill there.
     *
     * @param index The index to check. <strong>Must be non-negative</strong>
     * @return {@code false} if at the given index, the handler can <strong>never</strong> be inserted into; {@code true} otherwise.
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #supportsInsertion()
     */
    boolean supportsInsertion(int index);

    /**
     * <strong>Note:</strong> It is advised to override this and return {@code true} if you already know the handler will have a possible true result or {@code false} if none of them will ever be true;
     * rather than having it require a full iteration to look up the results.
     *
     * @return {@code true} if the handler can be extracted from in its configuration, false otherwise. If using indices, this should return true if any index allows insertion.
     * @see #supportsExtraction(int)
     */
    default boolean supportsExtraction() {
        var indices = size();
        for (var index = 0; index < indices; index++) {
            if (supportsExtraction(index)) return true;
        }
        return false;
    }

    /**
     * <b>IMPORTANT:</b> This doesn't add any control, this is merely a guide for things like pipes to know ahead of time if it can be ever extracted from when the capability invalidates for example.
     * You shouldn't call this in your own {@link IEnergyHandler#extract} method.
     *
     * @param index The index to check <strong>Must be non-negative</strong>
     * @return {@code false} if at the given index, the handler can <strong>never</strong> be extracted from; {@code true} otherwise.
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #supportsExtraction()
     */
    boolean supportsExtraction(int index);

    /**
     * Inserts a given amount of energy into the handler at the target index. If the intent is to just arbitrarily send power to the handler, consider using {@link IEnergyHandler#insert(int, TransactionContext)} instead.
     *
     * @param index       The index to insert into. <strong>Must be non-negative</strong>
     * @param amount      The value to insert. <strong>Must be non-negative</strong>
     * @param transaction the transaction chain that the insertion is part of. The developer is expected to handle snapshotting as necessary to handle rollbacks when the transaction is not committed.
     * @return The amount that was inserted. <strong>Must be non-negative</strong>
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #insert(int, TransactionContext) Inserting into any index in the handler
     */
    int insert(int index, int amount, TransactionContext transaction);

    /**
     * Inserts a given amount into the handler. Distribution is up to the handler.
     * <p>
     * When implementing, it is advised to not make this call {@link IEnergyHandler#insert(int, int, TransactionContext)} for each index directly,
     * but rather reuse the logic already checked. See {@link net.neoforged.neoforge.transfer.handlers.templates.energy.EnergyBufferAttachment#insert(int, TransactionContext) EnergyBuffer.insertCommon} for a reference of an implementation.
     *
     * @param amount      The amount to insert. <strong>Must be non-negative</strong>
     * @param transaction the transaction chain that the insertion is part of. The developer is expected to handle snapshotting as necessary to handle rollbacks when the transaction is not committed.
     * @return The amount that was inserted. <strong>Must be non-negative</strong>
     * @see #insert(int, int, TransactionContext) Inserting by index
     */
    int insert(int amount, TransactionContext transaction);

    /**
     * Extracts a given amount of energy from the handler at the given index. If the intent is to arbitrarily extract power from the handler, consider using {@link IEnergyHandler#extract(int, TransactionContext)} instead.
     *
     * @param index       The index to extract from. <strong>Must be non-negative</strong>
     * @param amount      The amount to extract. <strong>Must be non-negative</strong>
     * @param transaction the transaction chain that the extraction is part of. The developer is expected to handle snapshotting as necessary to handle rollbacks when the transaction is not committed.
     * @return The amount that was extracted. <strong>Must be non-negative</strong>
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #extract(int, TransactionContext) Extracting from any index in the handler
     */
    int extract(int index, int amount, TransactionContext transaction);

    /**
     * Extracts a given amount from the handler. Distribution is up to the handler.
     * <p>
     * When implementing this method, it is advised to not make this call {@link IEnergyHandler#extract(int, int, TransactionContext)} for each index directly,
     * but rather reuse the logic already checked. See {@link net.neoforged.neoforge.transfer.handlers.templates.energy.EnergyBufferAttachment#extract(int, TransactionContext) EnergyBuffer.extractCommon} for a reference of an implementation.
     *
     * @param amount      The amount of energy to extract. <strong>Must be non-negative</strong>
     * @param transaction the transaction chain that the extraction is part of. The developer is expected to handle snapshotting as necessary to handle rollbacks when the transaction is not committed.
     * @return The amount that was extracted. <strong>Must be non-negative</strong>
     * @see #extract(int, int, TransactionContext) Extracting by index
     */
    int extract(int amount, TransactionContext transaction);
}
