/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.energy;

import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * A simple reference implementation of {@link EnergyHandler}. Use this or implement your own if you need custom logic.
 * It is recommended to make your own implementation of {@link EnergyHandler}.
 * <p>
 * If using this, then it is recommended to use the {@link Builder} to construct an {@link EnergyBuffer} such as:
 *
 * <pre>
 * {@code
 * EnergyBuffer.builder(1000).maxTransfer(10).build();
 * }
 * </pre>
 *
 * <p>
 * Unlike the {@link EnergyBufferComponentHandler}, the handler is mutable and is expected to be used as a block entity field, attachment, or similar.
 */
public final class EnergyBuffer implements EnergyHandler, ValueIOSerializable {
    /**
     * Current amount of energy stored in the buffer
     */
    private int energy;

    /**
     * How much energy can be stored in the buffer.
     */
    private final int capacity;

    /**
     * How much energy can be inserted in a single call of `insert`.
     * Note, if you need to limit how much can be inserted in a single tick,
     * then you will need to make your own implementation of {@link EnergyHandler} that has the required information.
     */
    private final int maxInsert;

    /**
     * How much energy can be inserted in a single call of `extract`.
     * Note, if you need to limit how much can be extracted in a single tick,
     * then you will need to make your own implementation of {@link EnergyHandler} that has the required information.
     */
    private final int maxExtract;

    @Nullable
    private final Runnable onChangedCallback;
    private final SnapshotJournal<Integer> snapshot = new IntJournal();

    /**
     * A simple {@link EnergyHandler} implementation.
     * <p>
     * Use of constructor is allowed, but it is HIGHLY recommended to use the builder.
     * <p>
     * Example:
     *
     * <pre>
     * {@code
     * //Creates a buffer with a capacity of 1000 units, a max insertion and extraction rate of 10, and a callback to the block entity to mark it was changed when a commit occurs.
     * EnergyBuffer.builder(1000).maxTransfer(10).callback(blockEntity::setChanged).build();
     * }
     * </pre>
     *
     * @param capacity          Amount of energy that can be stored in the buffer.
     * @param maxInsertionRate  How much energy can be inserted in a single {@link EnergyHandler#insert} call.
     * @param maxExtractionRate How much energy can be extracted in a single {@link EnergyHandler#extract} call.
     * @param energy            The initial or serialized amount of energy in the buffer.
     */
    public EnergyBuffer(int capacity, int maxInsertionRate, int maxExtractionRate, int energy, @Nullable Runnable onChangedCallback) {
        TransferPreconditions.checkNonNegative(capacity);
        TransferPreconditions.checkNonNegative(maxInsertionRate);
        TransferPreconditions.checkNonNegative(maxExtractionRate);
        TransferPreconditions.checkNonNegative(energy);

        this.capacity = capacity;
        this.maxInsert = maxInsertionRate;
        this.maxExtract = maxExtractionRate;
        this.energy = energy;
        this.onChangedCallback = onChangedCallback;
    }

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

        amount = Math.min(maxInsert, amount);
        if (amount == 0 || energy == capacity) return 0;

        int inserted = Math.min(capacity - energy, amount);
        snapshot.updateSnapshots(transaction);
        energy += inserted;
        return inserted;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);

        amount = Math.min(maxExtract, amount);
        if (amount == 0 || energy == 0) return 0;

        int handledAmount = Math.min(energy, amount);
        snapshot.updateSnapshots(transaction);
        energy -= handledAmount;

        return handledAmount;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("energy", energy);
    }

    @Override
    public void deserialize(ValueInput input) {
        energy = input.getIntOr("energy", 0);
    }

    /**
     * Creates a builder of a specified capacity. This is the advised way to make an {@link EnergyBuffer}.
     * An important note, by default the transfer rate is 1% of the capacity (but never less than 1).
     * This it to help make a simple feeling of something charging.
     *
     * @param capacity How much energy the buffer is set to be able to hold.
     * @return Chainable builder to allow creation of a new {@link EnergyBuffer}
     */
    public static Builder builder(int capacity) {
        return new Builder(capacity).maxExtractRate(capacity).maxInsertRate(Mth.ceil(capacity * 0.01f));
    }

    public static class Builder {
        protected int energy = 0;
        protected int capacity;
        protected int maxInsertRate;
        protected int maxExtractRate;
        @Nullable
        protected Runnable onChangedCallback;

        /**
         * @param capacity How much energy the buffer can hold.
         */
        private Builder(int capacity) {
            TransferPreconditions.checkNonNegative(capacity);
            this.capacity = capacity;
        }

        /**
         * @param rate How much energy can be inserted in a single call.
         */
        public Builder maxInsertRate(int rate) {
            TransferPreconditions.checkNonNegative(rate);
            this.maxInsertRate = rate;
            return this;
        }

        /**
         * @param rate How much energy can be extracted in a single call.
         */
        public Builder maxExtractRate(int rate) {
            TransferPreconditions.checkNonNegative(rate);
            this.maxExtractRate = rate;
            return this;
        }

        /**
         * @param rate How much energy can be inserted or extracted in a single call.
         */
        public Builder maxTransferRate(int rate) {
            return maxExtractRate(rate).maxInsertRate(rate);
        }

        /**
         * @param amount Amount to set the initial energy buffer to
         */
        public Builder energy(int amount) {
            TransferPreconditions.checkNonNegative(amount);
            energy = amount;
            return this;
        }

        public Builder callback(Runnable onChangedCallback) {
            this.onChangedCallback = onChangedCallback;
            return this;
        }

        /**
         * Constructs a new {@link EnergyBuffer} to use from the values assigned while building.
         */
        public EnergyBuffer build() {
            return new EnergyBuffer(capacity, maxInsertRate, maxExtractRate, energy, onChangedCallback);
        }
    }

    private class IntJournal extends SnapshotJournal<Integer> {
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
            if (onChangedCallback != null)
                onChangedCallback.run();
        }
    }
}
