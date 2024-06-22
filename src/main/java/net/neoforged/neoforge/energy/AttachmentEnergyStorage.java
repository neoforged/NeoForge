/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.energy;

import net.minecraft.util.Mth;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.function.Supplier;

/**
 * Reference implementation of {@link IEnergyStorage}. Use/extend this or implement your own.
 *
 * Derived from the Redstone Flux power system designed by King Lemming and originally utilized in Thermal Expansion and related mods.
 * Created with consent and permission of King Lemming and Team CoFH. Released with permission under LGPL 2.1 when bundled with Forge.
 */
public class AttachmentEnergyStorage implements IEnergyStorage {
    protected final AttachmentHolder parent;
    protected final Supplier<AttachmentType<Integer>> attachmentType;
    protected final int capacity;
    protected final int maxReceive;
    protected final int maxExtract;

    public AttachmentEnergyStorage(AttachmentHolder parent, Supplier<AttachmentType<Integer>> attachmentType, int capacity) {
        this(parent, attachmentType, capacity, capacity, capacity);
    }

    public AttachmentEnergyStorage(AttachmentHolder parent, Supplier<AttachmentType<Integer>> attachmentType, int capacity, int maxTransfer) {
        this(parent, attachmentType, capacity, maxTransfer, maxTransfer);
    }

    public AttachmentEnergyStorage(AttachmentHolder parent, Supplier<AttachmentType<Integer>> attachmentType, int capacity, int maxReceive, int maxExtract) {
        this.parent = parent;
        this.attachmentType = attachmentType;
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        if (!canReceive() || toReceive <= 0) {
            return 0;
        }

        int storedEnergy = this.getEnergyStored();
        int energyReceived = Mth.clamp(this.capacity - storedEnergy, 0, Math.min(this.maxReceive, toReceive));
        if (!simulate)
            this.setEnergy(storedEnergy + energyReceived);
        return energyReceived;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (!canExtract() || toExtract <= 0) {
            return 0;
        }

        int storedEnergy = this.getEnergyStored();
        int energyExtracted = Math.min(storedEnergy, Math.min(this.maxExtract, toExtract));
        if (!simulate)
            this.setEnergy(storedEnergy - energyExtracted);
        return energyExtracted;
    }

    protected void setEnergy(int energy) {
        energy = Mth.clamp(energy, 0, this.capacity);
        this.parent.setData(this.attachmentType, energy);
    }

    @Override
    public int getEnergyStored() {
        return this.parent.getData(this.attachmentType);
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
}
