/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.energy;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A simple implementation of {@link EnergyHandler}, that can store any level of energy up to a given capacity,
 * with per-operation insertion and extraction limits.
 *
 * <p>While new instances of {@link SimpleEnergyHandler} can be created and used directly,
 * overriding {@link #onEnergyChanged} to react to changes in the energy level is recommended,
 * for example to trigger {@code setChanged()}.
 */
public class SimpleEnergyHandler implements EnergyHandler, ValueIOSerializable {
    protected int energy;
    protected int capacity;
    protected int maxInsert;
    protected int maxExtract;

    private final EnergyJournal energyJournal = new EnergyJournal();

    /**
     * Creates a new {@link SimpleEnergyHandler} instance with {@code 0} stored energy,
     * and no per-operation limit.
     *
     * @see SimpleEnergyHandler(int, int, int, int)
     */
    public SimpleEnergyHandler(int capacity) {
        this(capacity, capacity);
    }

    /**
     * Creates a new {@link SimpleEnergyHandler} instance with {@code 0} stored energy,
     * and the same per-insert and per-extraction limit.
     *
     * @see SimpleEnergyHandler(int, int, int, int)
     */
    public SimpleEnergyHandler(int capacity, int maxTransfer) {
        this(capacity, maxTransfer, maxTransfer);
    }

    /**
     * Creates a new {@link SimpleEnergyHandler} instance with {@code 0} stored energy.
     *
     * @see SimpleEnergyHandler(int, int, int, int)
     */
    public SimpleEnergyHandler(int capacity, int maxInsert, int maxExtract) {
        this(capacity, maxInsert, maxExtract, 0);
    }

    /**
     * Creates a new {@link SimpleEnergyHandler} instance.
     *
     * @param capacity   maximum capacity of the handler
     * @param maxInsert  maximum amount that can be accepted in each call to {@link #insert}
     * @param maxExtract maximum amount that can be accepted in each call to {@link #extract}
     * @param energy     current amount of energy in the handler. May be larger than the passed capacity,
     *                   for example if the capacity changed since the energy was put into the handler.
     * @throws IllegalArgumentException if any of the arguments is negative
     */
    public SimpleEnergyHandler(int capacity, int maxInsert, int maxExtract, int energy) {
        TransferPreconditions.checkNonNegative(capacity);
        TransferPreconditions.checkNonNegative(maxInsert);
        TransferPreconditions.checkNonNegative(maxExtract);
        TransferPreconditions.checkNonNegative(energy);

        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
        this.energy = energy;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("energy", energy);
    }

    @Override
    public void deserialize(ValueInput input) {
        energy = Math.max(0, input.getIntOr("energy", 0));
    }

    /**
     * Directly overwrites the energy amount of the handler.
     *
     * @throws IllegalArgumentException if the amount is negative
     */
    public void set(int amount) {
        TransferPreconditions.checkNonNegative(amount);

        if (this.energy != amount) {
            int previousAmount = this.energy;
            this.energy = amount;
            onEnergyChanged(previousAmount);
        }
    }

    /**
     * Called after the amount of energy in the handler changed.
     *
     * <p>For changes that happen through {@link #set}, this method is called immediately.
     * For changes that happen through {@link #insert} or {@link #extract},
     * this function will be called at the end of the transaction.
     */
    protected void onEnergyChanged(int previousAmount) {}

    @Override
    public long getAmountAsLong() {
        return this.energy;
    }

    @Override
    public long getCapacityAsLong() {
        return this.capacity;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);

        int inserted = Math.min(capacity - energy, Math.min(amount, maxInsert));
        if (inserted > 0) {
            energyJournal.updateSnapshots(transaction);
            energy += inserted;
            return inserted;
        }
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);

        int extracted = Math.min(energy, Math.min(amount, maxExtract));
        if (extracted > 0) {
            energyJournal.updateSnapshots(transaction);
            energy -= extracted;
            return extracted;
        }
        return 0;
    }

    private class EnergyJournal extends SnapshotJournal<Integer> {
        @Override
        protected Integer createSnapshot() {
            return energy;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            energy = snapshot;
        }

        @Override
        protected void onRootCommit(Integer originalState) {
            int previousAmount = originalState;
            if (energy != previousAmount) {
                onEnergyChanged(previousAmount);
            }
        }
    }
}
