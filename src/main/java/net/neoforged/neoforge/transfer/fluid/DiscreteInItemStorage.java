/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluid;

import net.neoforged.neoforge.transfer.initem.InItemStorageContext;
import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.storage.RegistryObjectVariant;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.storage.StoragePreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Template class for storages that are solely based on items (and not their data components)
 * for storing discrete amounts of a resource.
 * <p>One example for this type of storage is the vanilla bucket (see {@link VanillaBucketFluidStorage}),
 * where the bucket represents an empty storage, while the various filled bucket items represent
 * a full storage of always 1000 amount.
 * <p>Mods can extend this class for various other types of storages that work like that and exchange
 * the underlying item for another when the storage is being extracted from or inserted into.
 */
public abstract class DiscreteInItemStorage<T extends RegistryObjectVariant<?>> implements Storage<T> {
    private final InItemStorageContext context;

    public DiscreteInItemStorage(InItemStorageContext context) {
        this.context = context;
    }

    protected abstract ItemVariant getEmptyItem();

    protected abstract int getItemVolume();

    protected abstract ItemVariant getFilledItem(T fluidContent);

    protected abstract T getContainedResource(ItemVariant filledItem);

    @Override
    public int size() {
        return 1; // we already operate on a single bucket at a time
    }

    @Override
    public boolean supportsInsertion() {
        return context.supportsModification();
    }

    @Override
    public boolean supportsExtraction() {
        return context.supportsModification();
    }

    @Override
    public long insert(int index, T resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.checkSlot(index, size());
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        if (!context.getCurrent().equals(getEmptyItem())) {
            return 0; // can't fill non-empty items
        }

        var filledItem = getFilledItem(resource);
        if (filledItem.isBlank()) {
            return 0; // the fluid has no associated filled item
        }

        int itemVolume = getItemVolume();
        var itemsToFill = maxAmount / itemVolume;
        if (itemsToFill > 0) {
            long itemsFilled = context.exchange(filledItem, itemsToFill, transaction);
            return itemsFilled * itemVolume;
        } else {
            return 0;
        }
    }

    @Override
    public long extract(int index, T resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.checkSlot(index, size());
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        var containedFluid = getContainedResource(context.getCurrent());
        if (!resource.equals(containedFluid)) {
            return 0; // Incompatible fluid
        }

        var itemVolume = getItemVolume();
        var itemsToEmpty = maxAmount / itemVolume;
        if (itemsToEmpty > 0) {
            long itemsEmptied = context.exchange(getEmptyItem(), itemsToEmpty, transaction);
            return itemsEmptied * itemVolume;
        } else {
            return 0;
        }
    }

    @Override
    public boolean isResourceBlank(int index) {
        StoragePreconditions.checkSlot(index, size());
        return getContainedResource(context.getCurrent()).isBlank();
    }

    @Override
    public T getResource(int index) {
        StoragePreconditions.checkSlot(index, size());
        return getContainedResource(context.getCurrent());
    }

    @Override
    public long getAmount(int index) {
        StoragePreconditions.checkSlot(index, size());
        return isResourceBlank(index) ? 0 : getItemVolume() * context.getCurrentAmount();
    }

    @Override
    public long getCapacity(int index, T resource) {
        StoragePreconditions.checkSlot(index, size());
        return getItemVolume() * context.getCurrentAmount();
    }

    @Override
    public boolean isValid(int index, T resource) {
        StoragePreconditions.checkSlot(index, size());
        return !getFilledItem(resource).isBlank();
    }
}
