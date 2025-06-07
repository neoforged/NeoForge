/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.energy;

import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An energy storage is the unit of interaction with Energy inventories.
 * <p>
 * A reference implementation can be found at {@link AttachmentEnergyStorage} for attachment holders or {@link ItemEnergyStorage} for items
 * <p>
 * Derived from the Redstone Flux power system designed by King Lemming and originally utilized in Thermal Expansion and related mods.
 * Created with consent and permission of King Lemming and Team CoFH. Released with permission under LGPL 2.1 when bundled with Forge.
 *
 * @deprecated Now {@link net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler IEnergyHandler} to provide a more consistent design with the new resource handlers, as well as introduce an optional indexable system plus transactions.
 *             IF you are looking to keep what you have currently without exposing individual buffers or have mutliple buffers in the energy handler, then consider using {@link net.neoforged.neoforge.transfer.handlers.energy.ISingleEnergyHandler ISingleEnergyHandler} instead as that will be the most familiar.
 */
@Deprecated(since = "1.21.6", forRemoval = true)
public interface IEnergyStorage {
    /**
     * Adds energy to the storage. Returns the amount of energy that was accepted.
     *
     * @param toReceive The amount of energy being received.
     * @param simulate  If true, the insertion will only be simulated, meaning {@link #getEnergyStored()} will not change.
     * @return Amount of energy that was (or would have been, if simulated) accepted by the storage.
     */
    @Deprecated(since = "1.21.6", forRemoval = true)
    int receiveEnergy(int toReceive, boolean simulate);

    /**
     * Removes energy from the storage. Returns the amount of energy that was removed.
     *
     * @param toExtract The amount of energy being extracted.
     * @param simulate  If true, the extraction will only be simulated, meaning {@link #getEnergyStored()} will not change.
     * @return Amount of energy that was (or would have been, if simulated) extracted from the storage.
     * @deprecated becomes {@link net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler#extract(int index, TransactionContext)}
     */

    @Deprecated(since = "1.21.6", forRemoval = true)
    int extractEnergy(int toExtract, boolean simulate);

    /**
     * Returns the amount of energy currently stored.
     *
     * @deprecated split into two, if you want to know the total current amount (index-less) {@link net.neoforged.neoforge.transfer.EnergyHandlerUtil#getAmount(IEnergyHandler)
     *             EnergyHandlerUtil#getAmount(IEnergyHandler)} will be an option; otherwise this has a new parameter in the handler {@link IEnergyHandler#getAmount(int index)}
     */
    @Deprecated(since = "1.21.6", forRemoval = true)
    int getEnergyStored();

    /**
     * Returns the maximum amount of energy that can be stored.
     *
     * @deprecated split into two, if you want to know the total capacity (index-less) {@link net.neoforged.neoforge.transfer.EnergyHandlerUtil#getCapacity(IEnergyHandler)
     *             EnergyHandlerUtil#getCapacity(IEnergyHandler)} will be an option; otherwise this has a new parameter in the handler {@link IEnergyHandler#getCapacity(int index)}
     */
    @Deprecated(since = "1.21.6", forRemoval = true)
    int getMaxEnergyStored();

    /**
     * Returns if this storage can have energy extracted.
     * If this is false, then any calls to extractEnergy will return 0.
     *
     * @deprecated This no longer is controlled by the interface but rather your own implementation. It is expected that it is handled on a case-by-case basis in the extract method.
     */
    @Deprecated(since = "1.21.6", forRemoval = true)
    boolean canExtract();

    /**
     * Used to determine if this storage can receive energy.
     * If this is false, then any calls to receiveEnergy will return 0.
     *
     * @deprecated This no longer is controlled by the interface but rather your own implementation. It is expected that it is handled on a case-by-case basis in the insert method.
     */
    @Deprecated(since = "1.21.6", forRemoval = true)
    boolean canReceive();
}
