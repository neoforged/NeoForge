/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.energy;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.handlers.TransferCharacteristics;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.intellij.lang.annotations.MagicConstant;
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
public interface IEnergyHandler {
    /**
     * The total amount of energy available in this handler.
     * <p>
     * It should be expected that {@code getAmount() <= getCapacity()}.
     * 
     * @see #getAmountAsLong()
     * @return The amount of energy stored in the handler across all indices. <strong>Must be non-negative</strong>
     */
    int getAmount();

    /**
     * This is an optional method that provides the ability to query the contents up to a long should the internals allow for it.
     * This is only needed to be overridden should you store more than an int.
     * 
     * @see #getAmount()
     * @return The amount of energy stored. <strong>Must be non-negative</strong>
     */
    default long getAmountAsLong() {
        return getAmount();
    }

    /**
     * Gets the capacity the handler can hold.
     * 
     * @see #getCapacityAsLong()
     * @return The capacity of the handler across all indices. <strong>Must be non-negative</strong>
     */
    int getCapacity();

    /**
     * This is an optional method that provides the ability to query the capacity up to a long should the internals allow for it.
     * This is only needed to be overridden should you be able to store more than an int.
     * 
     * @see #getCapacity()
     * @return The amount of energy that can be stored in the handler. <strong>Must be non-negative</strong>
     */
    default long getCapacityAsLong() {
        return getCapacity();
    }

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

    /**
     * A description of how this handler is intended to be used. For instance, if energy is intended
     * to be insertable, then this would be expected to return a composite value that contains {@link TransferCharacteristics#INSERTABLE}.
     * It should be noted, that this isn't intended to be used as the control logic for your handler, but rather a communication to
     * outside consumers of this energy handler to make some pre-calculated decisions on.
     * <p>
     * If this were to return {@link TransferCharacteristics#UNKNOWN}, then no assumptions can be made about the
     * handler and should be used as you would without this information or alternatively as if your inquiry was true.
     * <p>
     * <strong>For blocks, this value is expected to be the same as long as the capability cache is valid.</strong>
     *
     * <pre>{@code
     * TransferCharacteristics.STATICALLY_SIZED | TransferCharacteristics.INSERT | TransferCharacteristics.EXTRACT
     * }</pre>
     *
     * @return Composite value of characteristics. These can be composed with a bitwise OR, (the '|').
     * @see TransferCharacteristics
     */
    @MagicConstant(flagsFromClass = TransferCharacteristics.class)
    int characteristics();

    /**
     * Transfer characteristics can be used to describe how this handler is intended to be used based on the returns
     * of {@link #characteristics()}
     * <p>
     * <strong>Don't override this method.</strong>
     *
     * @param characteristics The characteristics to test against.
     * @return {@code true} if the current set of characteristics contains the inquiry or is fully {@code UNKNOWN}; {@code false} otherwise.
     * @see #characteristics()
     * @see TransferCharacteristics
     */
    @ApiStatus.NonExtendable
    default boolean hasCharacteristics(@MagicConstant(flagsFromClass = TransferCharacteristics.class) int characteristics) {
        if (characteristics == TransferCharacteristics.UNKNOWN) return true;
        return (characteristics() & characteristics) == characteristics;
    }
}
