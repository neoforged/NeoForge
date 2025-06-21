/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.energy;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.transfer.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.energy.ISingleEnergyHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.snapshots.SetChangedSnapshot;
import org.jetbrains.annotations.Nullable;

/**
 * Variant of {@link EnergyBufferAttachment} for use with data components.
 * <p>
 * The actual data storage is managed by a data component, and all changes will write back to that component.
 * <p>
 * To use this class, register a new {@link DataComponentType} which holds an {@link Integer} for your item.
 * Then reference that component from your when registering the item with a capability. Something like the following:
 *
 * <pre>
 * {@code
 * capabilityRegistryEvent.registerItem(
 *         Capabilities.EnergyHandler.ITEM,
 *         EnergyBufferComponentHandler.builder(someCapacity, YourComponent)::build,
 *         YourItem);
 * }
 * </pre>
 */
public final class EnergyBufferComponentHandler implements ISingleEnergyHandler {
    private final IItemContext itemContext;
    private final MutableDataComponentHolder parent;
    private final DataComponentType<Integer> componentType;
    // These are not handled by the component and are constant upon the handler creation.
    // If you'd like to make this controlled by ComponentData, then a different implementation would be required.
    private final int capacity;
    private final int maxInsert;
    private final int maxExtract;
    private final IndexedIntSnapshot snapshot;

    /**
     * Creates a new ComponentEnergyStorage with a data component as the backing store for the energy value.
     *
     * @param parent        The parent component holder, such as an {@link ItemStack}
     * @param componentType The data component referencing the stored energy of the item stack
     * @param capacity      The max capacity of the energy being stored
     * @param maxInsert     The max per-transfer power input rate
     * @param maxExtract    The max per-transfer power output rate
     * @param callback      A callback when the component has been changed.
     */
    public EnergyBufferComponentHandler(IItemContext itemContext, MutableDataComponentHolder parent, DataComponentType<Integer> componentType, int capacity, int maxInsert, int maxExtract, @Nullable Runnable callback) {
        this.itemContext = itemContext;
        this.parent = parent;
        this.componentType = componentType;
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
        this.snapshot = IndexedIntSnapshot.of(this::set, this::getAmount, SetChangedSnapshot.of(callback));
    }

    private int getIndividualAmount() {
        return this.itemContext.getResource().getOrDefault(componentType, 0);
    }

    private int getIndividualLimit() {
        return this.capacity;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        if (maxInsert == 0) return 0;
        if (EnergyHandlerUtil.checkEnergy(amount)) return 0;

        int stackedAmount = maxInsert * itemContext.getAmount();
        //handle overflow
        if (stackedAmount < 0) stackedAmount = Integer.MAX_VALUE;

        amount = Mth.clamp(amount, 0, stackedAmount);

        int containerFill = getIndividualAmount();
        int spaceLeft = getIndividualLimit() - containerFill;
        if (spaceLeft == 0) return 0;

        snapshot.updateSnapshots(transaction);
        if (amount < spaceLeft) {
            return setPartial(amount + containerFill, transaction) == 1 ? amount : 0;
        }
        return setFull(amount / spaceLeft, transaction) * spaceLeft;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        if (maxExtract == 0) return 0;
        if (EnergyHandlerUtil.checkEnergy(amount)) return 0;

        int rawStackExtract = this.maxExtract * this.itemContext.getAmount();
        if (rawStackExtract < 0) rawStackExtract = Integer.MAX_VALUE;

        int clampedValue = Mth.clamp(amount, 0, rawStackExtract);
        if (clampedValue <= 0) return 0;

        int containerFill = getIndividualAmount();
        if (containerFill == 0) return 0;

        snapshot.updateSnapshots(transaction);
        if (clampedValue < containerFill) {
            return setPartial(containerFill - clampedValue, transaction) == 1 ? clampedValue : 0;
        }

        //check to see if this can overflow
        return empty(clampedValue / containerFill, transaction) * containerFill;
    }

    private int empty(int count, TransactionContext context) {
        ItemResource emptiedContainer = itemContext.getResource().without(componentType);
        return itemContext.exchange(emptiedContainer, count, context);
    }

    private int setFull(int count, TransactionContext context) {
        ItemResource filledContainer = itemContext.getResource().with(componentType, getIndividualLimit());
        return itemContext.exchange(filledContainer, count, context);
    }

    private int setPartial(int amount, TransactionContext context) {
        ItemResource filledContainer = itemContext.getResource().with(componentType, amount);
        return itemContext.exchange(filledContainer, 1, context);
    }

    @Override
    public int getAmount() {
        int rawEnergy = getIndividualAmount();
        int preCalc = Mth.clamp(rawEnergy, 0, this.capacity) * this.itemContext.getAmount();
        if (preCalc < 0) return Integer.MAX_VALUE;
        return preCalc;
    }

    @Override
    public int getCapacity() {
        int stackedAmount = this.capacity * itemContext.getAmount();
        //handle overflow
        if (stackedAmount < 0) return Integer.MAX_VALUE;
        return stackedAmount;
    }

    @Override
    public boolean supportsInsertion() {
        return this.maxInsert > 0;
    }

    @Override
    public boolean supportsExtraction() {
        return this.maxExtract > 0;
    }

    /**
     * Writes a new energy value to the data component. Clamps to [0, capacity]
     *
     * @param energy The new energy value
     */
    public void set(int index, int energy) {
        // we don't check index here given this is an overwrite
        int realEnergy = Mth.clamp(energy, 0, this.capacity);
        this.parent.set(this.componentType, realEnergy);
    }

    /**
     * Creates a builder of a specified size, and capacity. This is the advised way to make an {@link EnergyBufferComponentHandler}.
     * An important note, is by default the transfer rate is 1% of the capacity (but never less than 1).
     * This it to help make a simple feeling of something charging
     *
     * @param capacity How much energy the sub-buffers are set to be able to hold individually. If you desire separate capacities per buffer, then you will need to implement your own variant.
     * @return Chainable builder to allow creation of a new {@link EnergyBufferComponentHandler}
     */
    public static Builder builder(int capacity, Supplier<DataComponentType<Integer>> energyComponentSupplier) {
        return builder(capacity, energyComponentSupplier.get());
    }

    public static Builder builder(int capacity, DataComponentType<Integer> energyComponent) {
        return new Builder(energyComponent).capacity(capacity).maxExtractRate(capacity).maxInsertRate(Mth.ceil(capacity * 0.01f));
    }

    public static class Builder {
        private final DataComponentType<Integer> componentType;
        private int capacity;
        private int maxInsertRate;
        private int maxExtractRate;
        @Nullable
        private Runnable callback;

        private Builder(DataComponentType<Integer> componentType) {
            Objects.requireNonNull(componentType, "component type must be set");
            this.componentType = componentType;
        }

        public Builder onChange(Runnable callback) {
            Objects.requireNonNull(callback, "callback must be set");
            this.callback = callback;
            return this;
        }

        /**
         * @param capacity How much energy each sub-buffer can hold.
         */
        public Builder capacity(int capacity) {
            if (capacity < 0) throw new IllegalArgumentException("capacity must be non-negative");
            this.capacity = capacity;
            return this;
        }

        /**
         * @param rate How much energy the buffer can insert in a single call.
         */
        public Builder maxInsertRate(int rate) {
            if (maxInsertRate < 0) throw new IllegalArgumentException("maxInsertRate must be non-negative");
            this.maxInsertRate = rate;
            return this;
        }

        /**
         * @param rate How much energy the buffer can extract in a single call.
         */
        public Builder maxExtractRate(int rate) {
            if (maxExtractRate < 0) throw new IllegalArgumentException("maxExtractRate must be non-negative");
            this.maxExtractRate = rate;
            return this;
        }

        /**
         * @param rate How much energy the buffer can insert or extract in a single call.
         */
        public Builder maxTransferRate(int rate) {
            return maxExtractRate(rate).maxInsertRate(rate);
        }

        /**
         * Constructs a new {@link EnergyBufferAttachment} to use.
         */
        public EnergyBufferComponentHandler build(MutableDataComponentHolder parent, IItemContext itemContext) {
            return new EnergyBufferComponentHandler(itemContext, parent, componentType, capacity, maxInsertRate, maxExtractRate, callback);
        }
    }
}
