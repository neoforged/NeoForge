/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resource;

import com.google.common.math.IntMath;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.resources.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class ItemContextResourceHandler<T extends IResource> implements ISingleResourceHandler<T> {
    protected final IItemContext itemContext;
    protected final DataComponentType<ResourceStack<T>> componentType;
    protected final ResourceStack<T> defaultStack;
    protected final int capacityOfOneItem;
    protected final Predicate<T> validator;

    public ItemContextResourceHandler(IItemContext itemContext, DataComponentType<ResourceStack<T>> componentType, ResourceStack<T> defaultStack, int capacityOfOneItem) {
        this(itemContext, componentType, defaultStack, capacityOfOneItem, r -> true);
    }

    public ItemContextResourceHandler(IItemContext itemContext, DataComponentType<ResourceStack<T>> componentType, ResourceStack<T> defaultStack, int capacityOfOneItem, Predicate<T> validator) {
        this.itemContext = itemContext;
        this.componentType = componentType;
        this.defaultStack = defaultStack;
        this.capacityOfOneItem = capacityOfOneItem;
        this.validator = validator;
    }

    private ResourceStack<T> getStoredResourceStack() {
        return itemContext.getResource().getOrDefault(componentType, defaultStack);
    }

    protected int getSingleItemAmount() {
        return getStoredResourceStack().amount();
    }

    @Override
    public T getResource(int index) {
        Objects.checkIndex(index, size());
        return getStoredResourceStack().resource();
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        return IntMath.saturatedMultiply(getSingleItemAmount(), itemContext.getAmount());
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        return (long) getSingleItemAmount() * itemContext.getAmount();
    }

    @Override
    public int getCapacity(int index, T resource) {
        Objects.checkIndex(index, size());
        if (!resource.isEmpty() && !getResource(0).equals(resource)) return 0;
        return IntMath.saturatedMultiply(capacityOfOneItem, itemContext.getAmount());
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        Objects.checkIndex(index, size());

        if (!resource.isEmpty() && !getStoredResourceStack().resource().equals(resource)) return 0;
        return (long) capacityOfOneItem * itemContext.getAmount();
    }

    @Override
    public boolean isValid(int index, T resource) {
        return validator.test(resource);
    }

    public boolean isEmpty() {
        return !itemContext.getResource().has(componentType);
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount) || !isValid(0, resource)) return 0;
        T presentResource = getResource(0);

        if (presentResource.isEmpty()) {
            if (amount < capacityOfOneItem)
                return setPartial(resource, amount, transaction) == 1 ? amount : 0;
            return IntMath.saturatedMultiply(setFull(resource, amount / capacityOfOneItem, capacityOfOneItem, transaction), capacityOfOneItem);
        }

        if (!presentResource.equals(resource)) return 0;

        int containerFill = getSingleItemAmount();
        int spaceLeft = capacityOfOneItem - containerFill;
        if (spaceLeft == 0) return 0;
        if (amount < spaceLeft)
            return setPartial(resource, amount + containerFill, transaction) == 1 ? amount : 0;
        return IntMath.saturatedMultiply(setFull(resource, amount / spaceLeft, capacityOfOneItem, transaction), spaceLeft);
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount) || isEmpty() || !getResource(0).equals(resource)) return 0;
        int containerFill = getSingleItemAmount();
        if (containerFill == 0) return 0;

        if (amount < containerFill) {
            int exchanged = setPartial(resource, containerFill - amount, transaction);
            return exchanged == 1 ? amount : 0;
        } else {
            int extractedCount = amount / containerFill;
            int exchanged = empty(extractedCount, transaction);
            return IntMath.saturatedMultiply(exchanged, containerFill);
        }
    }

    protected int empty(int count, TransactionContext transaction) {
        ItemResource emptiedContainer = itemContext.getResource().without(componentType);
        return itemContext.exchange(emptiedContainer, count, transaction);
    }

    protected int setFull(T resource, int count, int capacity, TransactionContext transaction) {
        ItemResource filledContainer = itemContext.getResource().with(componentType, ResourceStack.of(resource, capacity));
        return itemContext.exchange(filledContainer, count, transaction);
    }

    protected int setPartial(T resource, int amount, TransactionContext transaction) {
        ItemResource filledContainer = itemContext.getResource().with(componentType, ResourceStack.of(resource, amount));
        return itemContext.exchange(filledContainer, 1, transaction);
    }
}
