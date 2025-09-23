/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.energy;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A simple implementation of an {@link EnergyHandler} backed by an {@link ItemAccess}.
 * It can store any level of energy up to a given capacity, and has per-operation insertion and extraction limits.
 * The energy level is stored in an {@link Integer} data component.
 *
 * <p>To use this class, register a new {@link DataComponentType} which holds an {@link Integer} for your item.
 * Then reference that component from your {@link ICapabilityProvider} passed to {@link RegisterCapabilitiesEvent#registerItem} to create an instance of this class.
 */
public class ItemAccessEnergyHandler implements EnergyHandler {
    protected final ItemAccess itemAccess;
    protected final Item validItem;
    protected final DataComponentType<Integer> energyComponent;
    protected final int capacity;
    protected final int maxInsert;
    protected final int maxExtract;

    /**
     * Creates a new {@link ItemAccessEnergyHandler} instance with {@code 0} stored energy,
     * and no per-operation limit.
     *
     * @see ItemAccessEnergyHandler (ItemAccess, DataComponentType, int, int, int)
     */
    public ItemAccessEnergyHandler(ItemAccess itemAccess, DataComponentType<Integer> energyComponent, int capacity) {
        this(itemAccess, energyComponent, capacity, capacity);
    }

    /**
     * Creates a new {@link ItemAccessEnergyHandler} instance with {@code 0} stored energy,
     * and the same per-insert and per-extraction limit.
     *
     * @see ItemAccessEnergyHandler (ItemAccess, DataComponentType, int, int, int)
     */
    public ItemAccessEnergyHandler(ItemAccess itemAccess, DataComponentType<Integer> energyComponent, int capacity, int maxTransfer) {
        this(itemAccess, energyComponent, capacity, maxTransfer, maxTransfer);
    }

    /**
     * Creates a new {@link ItemAccessEnergyHandler} instance.
     *
     * @param itemAccess      item access backing this handler
     * @param energyComponent type of the data component used to store the energy amount
     * @param capacity        maximum capacity of the handler
     * @param maxInsert       maximum amount that can be accepted in each call to {@link #insert}
     * @param maxExtract      maximum amount that can be accepted in each call to {@link #extract}
     * @throws IllegalArgumentException if any of the int arguments is negative
     */
    public ItemAccessEnergyHandler(ItemAccess itemAccess, DataComponentType<Integer> energyComponent, int capacity, int maxInsert, int maxExtract) {
        TransferPreconditions.checkNonNegative(capacity);
        TransferPreconditions.checkNonNegative(maxInsert);
        TransferPreconditions.checkNonNegative(maxExtract);

        this.itemAccess = itemAccess;
        // Store the current item, such that if the item changes later we don't return any stored content from it.
        this.validItem = itemAccess.getResource().getItem();
        this.energyComponent = energyComponent;
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
    }

    /**
     * Retrieves the amount stored in the {@linkplain ItemAccess#getResource() current contents} of the item access.
     */
    protected int getAmountFrom(ItemResource accessResource) {
        if (!accessResource.is(validItem)) {
            return 0;
        }
        return accessResource.getOrDefault(energyComponent, 0);
    }

    /**
     * Returns a resource with updated amount.
     *
     * @param accessResource current resource, before the update
     * @param newAmount      the new amount
     * @return {@code accessResource} updated with the new amount,
     *         or {@link ItemResource#EMPTY} if the new amount cannot be stored
     * @implNote This function <strong>should not</strong> mutate the {@linkplain #itemAccess item access},
     *           that will be done by the calling code based on the results of this function.
     */
    protected ItemResource update(ItemResource accessResource, int newAmount) {
        return accessResource.with(energyComponent, newAmount);
    }

    @Override
    public long getAmountAsLong() {
        // Cast as the product of two ints might overflow an int, but fits into a long
        return (long) itemAccess.getAmount() * getAmountFrom(itemAccess.getResource());
    }

    @Override
    public long getCapacityAsLong() {
        var accessResource = itemAccess.getResource();
        if (!accessResource.is(validItem)) {
            return 0;
        }

        return (long) itemAccess.getAmount() * this.capacity;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);

        int accessAmount = itemAccess.getAmount();
        if (accessAmount == 0) {
            return 0;
        }
        int amountPerItem = Math.min(maxInsert, amount / accessAmount);
        if (amountPerItem == 0) {
            return 0;
        }

        ItemResource accessResource = itemAccess.getResource();
        if (!accessResource.is(validItem)) {
            return 0;
        }
        int currentAmountPerItem = getAmountFrom(accessResource);

        int insertedPerItem = Math.min(amountPerItem, capacity - currentAmountPerItem);
        if (insertedPerItem > 0) {
            ItemResource filledResource = update(accessResource, currentAmountPerItem + insertedPerItem);

            if (!filledResource.isEmpty()) {
                return insertedPerItem * itemAccess.exchange(filledResource, accessAmount, transaction);
            }
        }

        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);

        int accessAmount = itemAccess.getAmount();
        if (accessAmount == 0) {
            return 0;
        }
        int amountPerItem = Math.min(maxExtract, amount / accessAmount);
        if (amountPerItem == 0) {
            return 0;
        }

        ItemResource accessResource = itemAccess.getResource();
        // If the resource is not validItem this will return 0 and avoid extraction
        int currentAmountPerItem = getAmountFrom(accessResource);

        int extractedPerItem = Math.min(amountPerItem, currentAmountPerItem);
        if (extractedPerItem > 0) {
            ItemResource emptiedResource = update(accessResource, currentAmountPerItem - extractedPerItem);

            if (!emptiedResource.isEmpty()) {
                return extractedPerItem * itemAccess.exchange(emptiedResource, accessAmount, transaction);
            }
        }

        return 0;
    }
}
