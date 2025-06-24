/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.energy;

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
     * The total amount of energy available in this handler.
     * <p>
     * It should be expected that {@code getAmount() <= getCapacity()}.
     *
     * @return The amount of energy stored in the handler across all indices. <strong>Must be non-negative</strong>
     */
    int getAmount();

    /**
     * This is an optional method that provides the ability to query the contents up to a long should the internals allow for it.
     * This is only needed to be overridden should you store more than an int.
     *
     * @return The amount of energy stored. <strong>Must be non-negative</strong>
     */
    default long getAmountAsLong() {
        return getAmount();
    }

    /**
     * Gets the capacity the handler can hold.
     *
     * @return The capacity of the handler across all indices. <strong>Must be non-negative</strong>
     */
    int getCapacity();

    /**
     * This is an optional method that provides the ability to query the capacity up to a long should the internals allow for it.
     * This is only needed to be overridden should you be able to store more than an int.
     *
     * @return The amount of energy that can be stored in the handler. <strong>Must be non-negative</strong>
     */
    default long getCapacityAsLong() {
        return getCapacity();
    }

    /**
     * <b>IMPORTANT:</b> This doesn't add any control, this is merely a guide for things like pipes to know ahead of time if it can be ever inserted into when the capability invalidates for example.
     *
     * @return True if the handler can be inserted into at this time, false otherwise.
     */
    boolean supportsInsertion();

    /**
     * <b>IMPORTANT:</b> This doesn't add any control, this is merely a guide for things like pipes to know ahead of time if it can be ever extracted from when the capability invalidates for example.
     *
     * @return {@code true} if the handler can be extracted from in its configuration, false otherwise.
     */
    boolean supportsExtraction();

    /**
     * Inserts a given amount of energy into the handler.
     * <p>
     *
     * @param amount      The amount to insert. <strong>Must be non-negative</strong>
     * @param transaction the transaction chain that the insertion is part of.
     * @return The amount that was inserted. <strong>Must be non-negative</strong>
     */
    int insert(int amount, TransactionContext transaction);

    /**
     * Extracts a given amount of energy from the handler.
     *
     * @param amount      The amount of energy to extract. <strong>Must be non-negative</strong>
     * @param transaction the transaction chain that the extraction is part of.
     * @return The amount that was extracted. <strong>Must be non-negative</strong>
     */
    int extract(int amount, TransactionContext transaction);
}
