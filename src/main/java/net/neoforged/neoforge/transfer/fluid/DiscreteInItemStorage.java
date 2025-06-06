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
 * Template class for storages that can store a fixed amount of a single resource at a time in item-form.
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

    /**
     * @return The empty container item.
     */
    protected abstract ItemVariant getEmptyItem();

    /**
     * @return The amount of resource that a filled item contains. This has to be constant.
     */
    protected abstract long getFilledAmount();

    /**
     * Gets the filled item that would contain the given resource.
     * <p>
     * Implementors should ensure that for the returned item, {@link #getContainedResource} returns
     * the given resource.
     */
    protected abstract ItemVariant getFilledItem(T containedResource);

    /**
     * @param filledItem The filled item, which may be blank.
     * @return The resource contained in the item.
     */
    protected abstract T getContainedResource(ItemVariant filledItem);

    @Override
    public int size() {
        var current = context.getCurrent();
        // Handle the case where the underlying item has been swapped out for one that is not compatible
        // with this storage.
        if (current.isBlank() || !current.equals(getEmptyItem()) && getContainedResource(current).isBlank()) {
            return 0;
        }
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
        StoragePreconditions.checkSlot(index, 1);
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        if (!context.getCurrent().equals(getEmptyItem())) {
            return 0; // can't fill non-empty items
        }

        var filledItem = getFilledItem(resource);
        if (filledItem.isBlank()) {
            return 0; // the fluid has no associated filled item
        }

        var itemVolume = getFilledAmount();
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
        StoragePreconditions.checkSlot(index, 1);
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        var containedFluid = getContainedResource(context.getCurrent());
        if (!resource.equals(containedFluid)) {
            return 0; // Incompatible fluid
        }

        var itemVolume = getFilledAmount();
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
        StoragePreconditions.checkSlot(index, 1);
        return getContainedResource(context.getCurrent()).isBlank();
    }

    @Override
    public T getResource(int index) {
        StoragePreconditions.checkSlot(index, 1);
        return getContainedResource(context.getCurrent());
    }

    @Override
    public long getAmount(int index) {
        StoragePreconditions.checkSlot(index, 1);
        return isResourceBlank(index) ? 0 : getFilledAmount() * context.getCurrentAmount();
    }

    @Override
    public long getCapacity(int index, T resource) {
        StoragePreconditions.checkSlot(index, 1);
        return getFilledAmount() * context.getCurrentAmount();
    }

    @Override
    public boolean isValid(int index, T resource) {
        StoragePreconditions.checkSlot(index, 1);
        return !getFilledItem(resource).isBlank();
    }
}
