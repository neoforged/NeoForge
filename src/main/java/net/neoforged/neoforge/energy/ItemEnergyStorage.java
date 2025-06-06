/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.energy;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Variant of {@link AttachmentEnergyStorage} for use with data components.
 * <p>
 * The actual data storage is managed by a data component, and all changes will write back to that component.
 * <p>
 * To use this class, register a new {@link DataComponentType} which holds an {@link Integer} for your item.
 * Then reference that component from your {@link ICapabilityProvider} passed to {@link RegisterCapabilitiesEvent#registerItem} to create an instance of this class.
 */
public class ItemEnergyStorage implements IEnergyStorage {
    protected final IItemContext itemContext;
    protected final DataComponentType<Integer> energyComponent;
    protected final int capacity;
    protected final int maxReceive;
    protected final int maxExtract;

    /**
     * Creates a new ComponentEnergyStorage with a data component as the backing store for the energy value.
     *
     * @param itemContext     The context for the item stack
     * @param energyComponent The data component referencing the stored energy of the item stack
     * @param capacity        The max capacity of the energy being stored
     * @param maxReceive      The max per-transfer power input rate
     * @param maxExtract      The max per-transfer power output rate
     */
    public ItemEnergyStorage(IItemContext itemContext, DataComponentType<Integer> energyComponent, int capacity, int maxReceive, int maxExtract) {
        this.itemContext = itemContext;
        this.energyComponent = energyComponent;
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    /**
     * Creates a new ItemEnergyStorage with a unified receive / extract rate.
     */
    public ItemEnergyStorage(IItemContext itemContext, DataComponentType<Integer> energyComponent, int capacity, int maxTransfer) {
        this(itemContext, energyComponent, capacity, maxTransfer, maxTransfer);
    }

    /**
     * Creates a new ItemEnergyStorage with a transfer rate equivalent to the capacity.
     */
    public ItemEnergyStorage(IItemContext itemContext, DataComponentType<Integer> energyComponent, int capacity) {
        this(itemContext, energyComponent, capacity, capacity);
    }

    protected int getIndividualAmount() {
        return this.itemContext.getResource().getOrDefault(this.energyComponent, 0);
    }

    protected int getIndividualLimit() {
        return this.capacity;
    }

    @Override
    public int receiveEnergy(int amount, boolean simulate) {
        amount = Mth.clamp(amount, 0, this.maxReceive * this.itemContext.getAmount());
        if (amount <= 0) return 0;
        int containerFill = getIndividualAmount();
        int spaceLeft = getIndividualLimit() - containerFill;
        try (var transaction = Transaction.open(TransactionContext.ROOT)) {
            var handled = 0;
            if (amount < spaceLeft) {
                handled = setPartial(amount + containerFill, transaction) == 1 ? amount : 0;
            } else {
                handled = setFull(amount / spaceLeft, transaction) * spaceLeft;
            }
            if (!simulate) transaction.commit();

            return handled;
        }
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        maxExtract = Mth.clamp(maxExtract, 0, this.maxExtract * this.itemContext.getAmount());
        if (maxExtract <= 0) return 0;
        int containerFill = getIndividualAmount();
        try (var transaction = Transaction.open(TransactionContext.ROOT)) {
            if (maxExtract < containerFill) {
                int exchanged = setPartial(containerFill - maxExtract, transaction);
                if (!simulate) transaction.commit();

                return exchanged == 1 ? maxExtract : 0;
            } else {
                int exchanged = empty(maxExtract / containerFill, transaction);
                if (!simulate) transaction.commit();

                return exchanged * containerFill;
            }
        }
    }

    protected int empty(int count, TransactionContext context) {
        ItemResource emptiedContainer = itemContext.getResource().without(energyComponent);
        return itemContext.exchange(emptiedContainer, count, context);
    }

    protected int setFull(int count, TransactionContext context) {
        ItemResource filledContainer = itemContext.getResource().with(energyComponent, getIndividualLimit());
        return itemContext.exchange(filledContainer, count, context);
    }

    protected int setPartial(int amount, TransactionContext context) {
        ItemResource filledContainer = itemContext.getResource().with(energyComponent, amount);
        return itemContext.exchange(filledContainer, 1, context);
    }

    @Override
    public int getEnergyStored() {
        int rawEnergy = getIndividualAmount();
        return Mth.clamp(rawEnergy, 0, this.capacity) * this.itemContext.getAmount();
    }

    @Override
    public int getMaxEnergyStored() {
        return this.capacity * this.itemContext.getAmount();
    }

    @Override
    public boolean canExtract() {
        return this.maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return this.maxReceive > 0;
    }
}
