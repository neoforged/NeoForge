/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.energy.templates;

import net.minecraft.util.Mth;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.energy.IEnergyHandler;

import java.util.function.Supplier;

/**
 * Reference implementation of {@link IEnergyHandler}. Use/extend this or implement your own.
 *
 * Derived from the Redstone Flux power system designed by King Lemming and originally utilized in Thermal Expansion and related mods.
 * Created with consent and permission of King Lemming and Team CoFH. Released with permission under LGPL 2.1 when bundled with Forge.
 */
public class AttachmentEnergyStorage implements IEnergyHandler {
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
    public int insert(int toReceive, TransferAction action) {
        if (!canInsert() || toReceive <= 0) {
            return 0;
        }

        int storedEnergy = this.getAmount();
        int energyReceived = Mth.clamp(this.capacity - storedEnergy, 0, Math.min(this.maxReceive, toReceive));
        if (action.isExecuting())
            this.setEnergy(storedEnergy + energyReceived);
        return energyReceived;
    }

    @Override
    public int extract(int toExtract, TransferAction action) {
        if (!canExtract() || toExtract <= 0) {
            return 0;
        }

        int storedEnergy = this.getAmount();
        int energyExtracted = Math.min(storedEnergy, Math.min(this.maxExtract, toExtract));
        if (action.isExecuting())
            this.setEnergy(storedEnergy - energyExtracted);
        return energyExtracted;
    }

    protected void setEnergy(int energy) {
        energy = Mth.clamp(energy, 0, this.capacity);
        this.parent.setData(this.attachmentType, energy);
    }

    @Override
    public int getAmount() {
        return this.parent.getData(this.attachmentType);
    }

    @Override
    public int getLimit() {
        return this.capacity;
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
