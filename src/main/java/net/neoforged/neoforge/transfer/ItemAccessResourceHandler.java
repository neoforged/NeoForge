/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import java.util.Objects;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Base implementation of a {@link ResourceHandler} backed by an {@link ItemAccess}.
 * This implementation is generic in the type of transferred resources {@code T},
 * and in how they are ultimately stored in the item access.
 *
 * <p>As a result of this flexibility, this base implementation comes with the following methods that will typically be overridden:
 * <ul>
 * <li>(required) {@link #getResourceFrom} and {@link #getAmountFrom} to read the stored resource and amount in the item access.</li>
 * <li>(required) {@link #update} to update an item resource from the item access with new contents of the handler.</li>
 * <li>(required) {@link #getCapacity} to specify the capacity of this handler.</li>
 * <li>(optional) {@link #isValid} to limit which resources are allowed in this handler; by default any resource is allowed.</li>
 * </ul>
 *
 * @param <T> The type of resource this handler manages.
 */
public abstract class ItemAccessResourceHandler<T extends Resource> implements ResourceHandler<T> {
    protected final ItemAccess itemAccess;
    protected final int size;

    protected ItemAccessResourceHandler(ItemAccess itemAccess, int size) {
        this.itemAccess = itemAccess;
        this.size = size;
    }

    /**
     * Retrieves the resource stored in the {@linkplain ItemAccess#getResource() current contents} of the item access.
     */
    protected abstract T getResourceFrom(ItemResource accessResource, int index);

    /**
     * Retrieves the amount stored in the {@linkplain ItemAccess#getResource() current contents} of the item access.
     *
     * @see ResourceHandler#getAmountAsInt
     */
    protected abstract int getAmountFrom(ItemResource accessResource, int index);

    /**
     * Returns a resource with updated resource and amount.
     *
     * @param accessResource current resource, before the update
     * @param index          the index at which the resource and amount should be updated
     * @param newResource    the new resource, <strong>never empty</strong>: empty is indicated by a 0 amount
     * @param newAmount      the new amount
     * @return {@code accessResource} updated with the new resource and amount,
     *         or {@link ItemResource#EMPTY} if the new resource or amount cannot be stored
     * @implNote This function <strong>should not</strong> mutate the {@linkplain #itemAccess item access},
     *           that will be done by the calling code based on the results of this function.
     */
    // TODO: we could allow returning null when the resource/amount should be deleted, to allow for "consumable" implementations with minimal effort
    protected abstract ItemResource update(ItemResource accessResource, int index, T newResource, int newAmount);

    /**
     * Return {@code true} if the passed non-empty resource can fit in this handler, {@code false} otherwise.
     *
     * <p>The result of this function is used in the provided implementations of:
     * <ul>
     * <li>{@link #getCapacityAsLong(int, T)}, to report a capacity of {@code 0} for invalid items;</li>
     * <li>{@link #insert(int, T, int, TransactionContext)}, to reject items that cannot fit in this handler.</li>
     * </ul>
     *
     * @see ResourceHandler#isValid
     */
    @Override
    public boolean isValid(int index, T resource) {
        return true;
    }

    /**
     * Return the maximum capacity of this handler for the passed resource.
     * If the passed resource is empty, an estimate should be returned.
     *
     * @return The maximum capacity of this handler for the passed resource.
     * @see ResourceHandler#getCapacityAsInt
     */
    protected abstract int getCapacity(int index, T resource);

    @Override
    public int size() {
        return size;
    }

    @Override
    public T getResource(int index) {
        Objects.checkIndex(index, size());
        return getResourceFrom(itemAccess.getResource(), index);
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        // Cast as the product of two ints might overflow an int, but fits into a long
        return (long) itemAccess.getAmount() * getAmountFrom(itemAccess.getResource(), index);
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        Objects.checkIndex(index, size());
        if (resource.isEmpty() || isValid(index, resource)) {
            // Cast as the product of two ints might overflow an int, but fits into a long
            return (long) itemAccess.getAmount() * getCapacity(index, resource);
        }
        return 0;
    }

    // TODO: support "all or nothing" resource handlers better by optionally changing how insert and extract round

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int accessAmount = itemAccess.getAmount();
        if (accessAmount == 0) {
            return 0;
        }
        int amountPerItem = amount / accessAmount;

        ItemResource accessResource = itemAccess.getResource();
        int currentAmountPerItem = getAmountFrom(accessResource, index);

        if ((currentAmountPerItem == 0 || resource.equals(getResourceFrom(accessResource, index))) && isValid(index, resource)) {
            int insertedPerItem = Math.min(amountPerItem, getCapacity(index, resource) - currentAmountPerItem);

            if (insertedPerItem > 0) {
                ItemResource filledResource = update(accessResource, index, resource, insertedPerItem + currentAmountPerItem);

                if (!filledResource.isEmpty()) {
                    return insertedPerItem * itemAccess.exchange(filledResource, accessAmount, transaction);
                }
            }
        }

        return 0;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int accessAmount = itemAccess.getAmount();
        if (accessAmount == 0) {
            return 0;
        }

        ItemResource accessResource = itemAccess.getResource();
        T currentResource = getResourceFrom(accessResource, index);

        if (resource.equals(currentResource)) {
            int currentAmountPerItem = getAmountFrom(accessResource, index);
            int extractedPerItem = Math.min(amount / accessAmount, currentAmountPerItem);

            if (extractedPerItem > 0) {
                ItemResource emptiedResource = update(accessResource, index, resource, currentAmountPerItem - extractedPerItem);

                if (!emptiedResource.isEmpty()) {
                    return extractedPerItem * itemAccess.exchange(emptiedResource, accessAmount, transaction);
                }
            }
        }

        return 0;
    }
}
