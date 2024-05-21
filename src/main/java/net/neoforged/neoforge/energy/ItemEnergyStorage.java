/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.energy;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Variant of {@link EnergyStorage} for use with {@link ItemStack} components.
 * <p>
 * The actual data storage is managed by a data component, and all changes will write back to that component.
 * <p>
 * To use this class, register a new {@link DataComponentType} which holds an {@link Integer} for your item.
 * Then reference that component from your {@link ICapabilityProvider} passed to {@link RegisterCapabilitiesEvent#registerItem} to create an instance of this class.
 */
public class ItemEnergyStorage implements IEnergyStorage {
    protected final ItemStack parent;
    protected final DataComponentType<Integer> powerComponent;
    protected final int capacity;
    protected final int maxReceive;
    protected final int maxExtract;

    /**
     * Creates a new ItemEnergyStorage with a data component as the backing store for energy capacity.
     * 
     * @param parent         The parent item stack
     * @param powerComponent The data component referencing the stored energy of the item stack
     * @param capacity       The max capacity of the energy being stored
     * @param maxReceive     The max per-transfer power input rate
     * @param maxExtract     The max per-transfer power output rate
     */
    public ItemEnergyStorage(ItemStack parent, DataComponentType<Integer> powerComponent, int capacity, int maxReceive, int maxExtract) {
        this.parent = parent;
        this.powerComponent = powerComponent;
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    /**
     * Creates a new ItemEnergyStorage with a unified receive / extract rate.
     * 
     * @see ItemEnergyStorage#ItemEnergyStorage(ItemStack, DataComponentType, int, int, int)
     */
    public ItemEnergyStorage(ItemStack parent, DataComponentType<Integer> powerComponent, int capacity, int maxTransfer) {
        this(parent, powerComponent, capacity, maxTransfer, maxTransfer);
    }

    /**
     * Creates a new ItemEnergyStorage with a transfer rate equivalent to the capacity.
     * 
     * @see ItemEnergyStorage#ItemEnergyStorage(ItemStack, DataComponentType, int, int, int)
     */
    public ItemEnergyStorage(ItemStack parent, DataComponentType<Integer> powerComponent, int capacity) {
        this(parent, powerComponent, capacity, capacity);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive())
            return 0;

        int energy = this.getEnergyStored();
        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate) {
            this.setEnergy(energy + energyReceived);
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract())
            return 0;

        int energy = this.getEnergyStored();
        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate) {
            this.setEnergy(energy - energyExtracted);
        }
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        int rawEnergy = this.parent.getOrDefault(this.powerComponent, 0);
        return Math.max(0, Math.min(capacity, rawEnergy));
    }

    @Override
    public int getMaxEnergyStored() {
        return this.capacity;
    }

    @Override
    public boolean canExtract() {
        return this.maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return this.maxReceive > 0;
    }

    protected void setEnergy(int energy) {
        this.parent.set(this.powerComponent, Math.clamp(energy, 0, this.capacity));
    }
}
