/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.energy.templates;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.items.ItemResource;

/**
 * Variant of {@link AttachmentEnergyStorage} for use with data components.
 * <p>
 * The actual data storage is managed by a data component, and all changes will write back to that component.
 * <p>
 * To use this class, register a new {@link DataComponentType} which holds an {@link Integer} for your item.
 * Then reference that component from your {@link ICapabilityProvider} passed to {@link RegisterCapabilitiesEvent#registerItem} to create an instance of this class.
 */
public class ItemEnergyStorage implements IEnergyHandler {
    protected final IItemContext context;
    protected final DataComponentType<Integer> energyComponent;
    protected final int capacity;
    protected final int maxReceive;
    protected final int maxExtract;

    /**
     * Creates a new ComponentEnergyStorage with a data component as the backing store for the energy value.
     * 
     * @param context         The context for the item stack
     * @param energyComponent The data component referencing the stored energy of the item stack
     * @param capacity        The max capacity of the energy being stored
     * @param maxReceive      The max per-transfer power input rate
     * @param maxExtract      The max per-transfer power output rate
     */
    public ItemEnergyStorage(IItemContext context, DataComponentType<Integer> energyComponent, int capacity, int maxReceive, int maxExtract) {
        this.context = context;
        this.energyComponent = energyComponent;
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    /**
     * Creates a new ItemEnergyStorage with a unified receive / extract rate.
     */
    public ItemEnergyStorage(IItemContext context, DataComponentType<Integer> energyComponent, int capacity, int maxTransfer) {
        this(context, energyComponent, capacity, maxTransfer, maxTransfer);
    }

    /**
     * Creates a new ItemEnergyStorage with a transfer rate equivalent to the capacity.
     */
    public ItemEnergyStorage(IItemContext context, DataComponentType<Integer> energyComponent, int capacity) {
        this(context, energyComponent, capacity, capacity);
    }

    protected int getIndividualAmount() {
        return this.context.getResource().getOrDefault(this.energyComponent, 0);
    }

    protected int getIndividualLimit() {
        return this.capacity;
    }

    @Override
    public int insert(int amount, TransferAction action) {
        amount = Mth.clamp(amount, 0, this.maxReceive * this.context.getAmount());
        if (amount <= 0) return 0;
        int containerFill = getIndividualAmount();
        int spaceLeft = getIndividualLimit() - containerFill;
        if (amount < spaceLeft) return setPartial(amount + containerFill, action) == 1 ? amount : 0;
        return setFull(amount / spaceLeft, action) * spaceLeft;
    }

    @Override
    public int extract(int amount, TransferAction action) {
        amount = Mth.clamp(amount, 0, this.maxExtract * this.context.getAmount());
        if (amount <= 0) return 0;
        int containerFill = getIndividualAmount();
        if (amount < containerFill) {
            int exchanged = setPartial(containerFill - amount, action);
            return exchanged == 1 ? amount : 0;
        } else {
            int extractedCount = amount / containerFill;
            int exchanged = empty(extractedCount, action);
            return exchanged * containerFill;
        }
    }

    protected int empty(int count, TransferAction action) {
        ItemResource emptiedContainer = context.getResource().remove(energyComponent);
        return context.exchange(emptiedContainer, count, action);
    }

    protected int setFull(int count, TransferAction action) {
        ItemResource filledContainer = context.getResource().set(energyComponent, getIndividualLimit());
        return context.exchange(filledContainer, count, action);
    }

    protected int setPartial(int amount, TransferAction action) {
        ItemResource filledContainer = context.getResource().set(energyComponent, amount);
        return context.exchange(filledContainer, 1, action);
    }

    @Override
    public int getAmount() {
        int rawEnergy = getIndividualAmount();
        return Mth.clamp(rawEnergy, 0, this.capacity) * this.context.getAmount();
    }

    @Override
    public int getLimit() {
        return this.capacity * this.context.getAmount();
    }

    @Override
    public boolean canExtract() {
        return this.maxExtract > 0;
    }

    @Override
    public boolean canInsert() {
        return this.maxReceive > 0;
    }
}
