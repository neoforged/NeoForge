/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.energy;

import java.util.Objects;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

/**
 * An energy storage is the unit of interaction with Energy inventories.
 * <p>
 * A reference implementation can be found at {@link EnergyStorage}.
 *
 * Derived from the Redstone Flux power system designed by King Lemming and originally utilized in Thermal Expansion and related mods.
 * Created with consent and permission of King Lemming and Team CoFH. Released with permission under LGPL 2.1 when bundled with Forge.
 *
 * @deprecated Use {@link EnergyHandler} instead. Code that is written against {@link IEnergyStorage} but receives
 *             an {@link EnergyHandler} can temporarily use {@link IEnergyStorage#of} to ease migration.
 */
@Deprecated(since = "1.21.9", forRemoval = true)
public interface IEnergyStorage {
    /**
     * Creates a wrapper around a {@link EnergyHandler}, to present it as a legacy {@link IEnergyStorage}.
     *
     * <p>This class is intended to make migration easier for code that expects an {@link IEnergyStorage}.
     *
     * @apiNote The {@link #receiveEnergy} and {@link #extractEnergy} implementations will open new root transactions,
     *          so this wrapper cannot be used from a transactional context (such as {@link EnergyHandler#insert}).
     */
    static IEnergyStorage of(EnergyHandler handler) {
        Objects.requireNonNull(handler, "handler");

        return new EnergyHandlerAdapter(handler);
    }

    /**
     * Adds energy to the storage. Returns the amount of energy that was accepted.
     *
     * @param toReceive The amount of energy being received.
     * @param simulate  If true, the insertion will only be simulated, meaning {@link #getEnergyStored()} will not change.
     * @return Amount of energy that was (or would have been, if simulated) accepted by the storage.
     * @deprecated Use {@link EnergyHandler#insert} instead.
     */
    @Deprecated(since = "1.21.9", forRemoval = true)
    int receiveEnergy(int toReceive, boolean simulate);

    /**
     * Removes energy from the storage. Returns the amount of energy that was removed.
     *
     * @param toExtract The amount of energy being extracted.
     * @param simulate  If true, the extraction will only be simulated, meaning {@link #getEnergyStored()} will not change.
     * @return Amount of energy that was (or would have been, if simulated) extracted from the storage.
     * @deprecated Use {@link EnergyHandler#extract} instead.
     */
    @Deprecated(since = "1.21.9", forRemoval = true)
    int extractEnergy(int toExtract, boolean simulate);

    /**
     * Returns the amount of energy currently stored.
     * 
     * @deprecated Use either {@link EnergyHandler#getAmountAsLong()} or {@link EnergyHandler#getAmountAsInt()} instead.
     */
    @Deprecated(since = "1.21.9", forRemoval = true)
    int getEnergyStored();

    /**
     * Returns the maximum amount of energy that can be stored.
     * 
     * @deprecated Use either {@link EnergyHandler#getCapacityAsLong()} or {@link EnergyHandler#getCapacityAsInt()} instead.
     */
    @Deprecated(since = "1.21.9", forRemoval = true)
    int getMaxEnergyStored();

    /**
     * Returns if this storage can have energy extracted.
     * If this is false, then any calls to extractEnergy will return 0.
     * 
     * @deprecated There is no direct equivalent for this method, since each energy handler is expected to perform this check on extraction already.
     *             Please open an issue on GitHub if you have a use for an equivalent of this method.
     */
    @Deprecated(since = "1.21.9", forRemoval = true)
    boolean canExtract();

    /**
     * Used to determine if this storage can receive energy.
     * If this is false, then any calls to receiveEnergy will return 0.
     * 
     * @deprecated There is no direct equivalent for this method, since each energy handler is expected to perform this check on insertion already.
     *             Please open an issue on GitHub if you have a use for an equivalent of this method.
     */
    @Deprecated(since = "1.21.9", forRemoval = true)
    boolean canReceive();
}
