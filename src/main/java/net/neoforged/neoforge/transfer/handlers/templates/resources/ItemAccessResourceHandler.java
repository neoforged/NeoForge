/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resources;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.handlers.resources.IndexModifier;
import net.neoforged.neoforge.transfer.handlers.resources.ResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.fluids.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.items.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

// TODO: javadoc
/**
 * Base implementation of a {@link ResourceHandler} backed by a list of stacks.
 * This implementation is generic in the type of transferred resources {@code T},
 * and in the type of stack {@code S} used to store the contents of the handler.
 *
 * <p>As a result of this flexibility, this base implementation comes with the following methods will typically be overridden:
 * <ul>
 * <li>(required) {@link #getResourceFrom}, {@link #getAmountFrom}, and {@link #getStackFrom} to convert between amounts, resources and stacks.</li>
 * <li>(required) {@link #copyOf} to copy stacks for snapshotting support.</li>
 * <li>(recommended) {@link #matches} to optimize the frequent operation of checking whether a resource and a stack match.</li>
 * <li>(optional) {@link #isValid} to limit which resources are allowed in this handler; by default any resource is allowed.</li>
 * <li>(required) {@link #getCapacity} to specify the capacity of this handler.</li>
 * <li>(recommended) {@link #onContentsChanged} to react to changes in this handler, for example to trigger {@code setChanged()}.</li>
 * </ul>
 *
 * @param <S> The type of stack used to store the contents of this handler.
 * @param <T> The type of resource this handler manages.
 * @see ItemStacksResourceHandler the ItemStack-based subclass
 * @see FluidStacksResourceHandler the FluidStack-based subclass
 * @see ResourceStacksResourceHandler the ResourceStack-based subclass
 */
public abstract class ItemAccessResourceHandler<T extends IResource> implements ResourceHandler<T> {
    protected final ItemAccess itemAccess;
    protected final int size;

    protected ItemAccessResourceHandler(ItemAccess itemAccess, int size) {
        this.itemAccess = itemAccess;
        this.size = size;
    }

    /**
     * Directly overwrites the contents of the handler.
     *
     * <p>Note that this method can be used as an {@link IndexModifier}, for usage in {@link ResourceHandlerSlot}.
     *
     * @param index    index to change
     * @param resource new resource at the index
     * @param amount   new amount at the index
     * @throws IllegalArgumentException if either the amount is negative; or if the resource is non-empty for a 0 amount
     */
    public void set(int index, T resource, int amount) {
        TransferPreconditions.checkNonNegative(amount);
        if (resource.isEmpty() && amount > 0) {
            throw new IllegalArgumentException("Resource is empty but the amount is positive: " + amount);
        }

        S oldContents = stacks.set(index, getStackFrom(resource, amount));
        onContentsChanged(index, oldContents);
    }

    /**
     * Retrieves the resource stored in the {@linkplain ItemAccess#getResource() current contents} of the item access.
     */
    protected abstract T getResourceFrom(ItemResource accessResource, int index);

    /**
     * Retrieves the amount stored in the {@linkplain ItemAccess#getResource() current contents} of the item access.
     */
    protected abstract int getAmountFrom(ItemResource accessResource, int index);

    // TODO: document
    protected abstract ItemResource update(ItemResource accessResource, int index, T newResource, int newAmount);

    // TODO: should maybe depend on accessResource as well
    /**
     * Return {@code true} if the passed non-empty resource can fit in this handler, {@code false} otherwise.
     *
     * <p>The result of this function is used in the provided implementations of:
     * <ul>
     * <li>{@link #getCapacityAsLong(int, T)}, to report a capacity of {@code 0} for invalid items;</li>
     * <li>{@link #insert(int, T, int, TransactionContext)}, to reject items that cannot fit in this handler.</li>
     * </ul>
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
     */
    protected abstract int getCapacity(int index, T resource);

    // TODO: what to do with this method?
    /**
     * Called after the contents of the handler changed.
     *
     * <p>For changes that happen through {@link #set}, this method is called immediately.
     * For changes that happen through {@link #insert} or {@link #extract},
     * this function will be called at the end of the transaction, once per index that changed.
     *
     * @param index            the index where the change happened
     * @param previousContents the stack before the change
     */
    protected void onContentsChanged(int index, S previousContents) {}

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
        return (long) itemAccess.getAmount() * getAmountFrom(itemAccess.getResource(), index);
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        Objects.checkIndex(index, size());
        return resource.isEmpty() || isValid(index, resource) ? getCapacity(index, resource) : 0;
    }

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
