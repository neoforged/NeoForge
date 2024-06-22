/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.energy;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.energy.templates.AttachmentEnergyStorage;
import net.neoforged.neoforge.transfer.energy.templates.ItemEnergyStorage;

/**
 * An energy storage is the unit of interaction with Energy inventories.
 * <p>
 * A reference implementation can be found at {@link AttachmentEnergyStorage} for Blocks or Entities, and {@link ItemEnergyStorage} for Items.
 *
 * <p>
 * Derived from the Redstone Flux power system designed by King Lemming and originally utilized in Thermal Expansion and related mods.
 * Created with consent and permission of King Lemming and Team CoFH. Released with permission under LGPL 2.1 when bundled with Forge.
 *
 */
public interface IEnergyHandler {
    /**
     * Inserts energy to the storage. Returns quantity of energy that was accepted.
     *
     * @param amount The amount of energy to be inserted.
     * @param action   The kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount of energy that was (or would have been, if simulated) inserted into the storage.
     */
    int insert(int amount, TransferAction action);

    /**
     * Removes energy from the storage. Returns quantity of energy that was removed.
     *
     * @param amount Maximum amount of energy to be extracted.
     * @param action   The kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount of energy that was (or would have been, if simulated) extracted from the storage.
     */
    int extract(int amount, TransferAction action);

    /**
     * Returns the amount of energy currently stored.
     */
    int getAmount();

    /**
     * Returns the maximum amount of energy that can be stored.
     */
    int getLimit();

    /**
     * Returns if this storage can have energy extracted.
     * If this is false, then any calls to extractEnergy will return 0.
     */
    boolean canExtract();

    /**
     * Used to determine if this storage can receive energy.
     * If this is false, then any calls to receiveEnergy will return 0.
     */
    boolean canInsert();
}
