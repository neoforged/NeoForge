/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resources;

import com.google.common.math.IntMath;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.TransferCharacteristics;
import net.neoforged.neoforge.transfer.handlers.resources.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

public abstract class ItemContextResourceHandler<T extends IResource> implements ISingleResourceHandler<T> {
    protected final IItemContext itemContext;
    protected final DataComponentType<ResourceStack<T>> componentType;
    protected final ResourceStack<T> emptyStack;
    protected final int capacityOfOneItem;
    protected final Predicate<T> validator;

    public ItemContextResourceHandler(IItemContext itemContext, DataComponentType<ResourceStack<T>> componentType, ResourceStack<T> emptyStack, int capacityOfOneItem) {
        this(itemContext, componentType, emptyStack, capacityOfOneItem, r -> true);
    }

    public ItemContextResourceHandler(IItemContext itemContext, DataComponentType<ResourceStack<T>> componentType, ResourceStack<T> emptyStack, int capacityOfOneItem, Predicate<T> validator) {
        this.itemContext = itemContext;
        this.componentType = componentType;
        this.emptyStack = emptyStack;
        this.capacityOfOneItem = capacityOfOneItem;
        this.validator = validator;
    }

    protected final ResourceStack<T> getStoredResourceStack() {
        return itemContext.getResource().getOrDefault(componentType, emptyStack);
    }

    /**
     * This is a helper just to get the amount that is currently stored in the resource stack on the item.
     * If you already have the resource stack, avoid calling this method and use
     * the amount contained in the resource stack instead.
     *
     * @return amount that is stored in one item.
     */
    protected final int getSingleItemAmount() {
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
        if (!resource.isEmpty() && !getResource(index).equals(resource)) return 0;
        return IntMath.saturatedMultiply(capacityOfOneItem, itemContext.getAmount());
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        Objects.checkIndex(index, size());

        if (!resource.isEmpty() && !getResource(index).equals(resource)) return 0;
        return (long) capacityOfOneItem * itemContext.getAmount();
    }

    @Override
    public boolean isValid(int index, T resource) {
        Objects.checkIndex(index, size());
        if (resource.isEmpty()) return true;
        return validator.test(resource);
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount) || !isValid(SINGLE_INDEX, resource)) return 0;
        ResourceStack<T> resourceStack = getStoredResourceStack();

        if (resourceStack.isEmpty()) {
            //if the amount is less than the capacity, we should fill partially.
            if (amount < capacityOfOneItem) {
                var partiallyFilledStack = ResourceStack.of(resource, amount, emptyStack);
                int insertedCount = set(1, partiallyFilledStack, transaction);
                return insertedCount == 1 ? amount : 0;
            }
            // If the capacity is the same, then we exchange the number that would be filled.
            var filledContainerStack = ResourceStack.of(resource, capacityOfOneItem, emptyStack);
            int filledCount = set(amount / capacityOfOneItem, filledContainerStack, transaction);
            return IntMath.saturatedMultiply(filledCount, capacityOfOneItem);
        }

        if (!resourceStack.resource().equals(resource)) return 0;

        int currentStored = resourceStack.amount();
        int spaceLeft = capacityOfOneItem - currentStored;
        if (spaceLeft == 0) return 0;

        if (amount < spaceLeft) {
            var partiallyFilledContainerStack = ResourceStack.of(resource, amount + currentStored, emptyStack);
            int insertedCount = set(1, partiallyFilledContainerStack, transaction);
            return insertedCount == 1 ? amount : 0;
        }

        var filledContainerStack = ResourceStack.of(resource, capacityOfOneItem, emptyStack);
        int filledCount = set(amount / spaceLeft, filledContainerStack, transaction);
        return IntMath.saturatedMultiply(filledCount, spaceLeft);
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount) || getSingleItemAmount() == 0)
            return 0;

        var resourceStack = getStoredResourceStack();
        if (!resourceStack.resource().equals(resource) || resourceStack.isEmpty())
            return 0;

        if (amount < resourceStack.amount()) {
            var partiallyDrainedContainerStack = ResourceStack.of(resource, resourceStack.amount() - amount, emptyStack);
            int extractedCount = set(1, partiallyDrainedContainerStack, transaction);
            return extractedCount == 1 ? amount : 0;
        }

        int extractedCount = amount / resourceStack.amount();
        int exchanged = set(extractedCount, emptyStack, transaction);
        return IntMath.saturatedMultiply(exchanged, resourceStack.amount());
    }

    @Override
    public int characteristics() {
        return TransferCharacteristics.DEFAULT;
    }

    @Override
    public int characteristics(int index) {
        return TransferCharacteristics.DEFAULT;
    }

    /**
     * @param count       How many of the items should be exchanged with the resource
     * @param resultStack The resource that should be applied to the component
     * @param transaction Transaction chain for snapshotting
     * @return How many items were exchanged when mutating components
     */
    @ApiStatus.OverrideOnly
    protected int set(int count, ResourceStack<T> resultStack, TransactionContext transaction) {
        var resultResource = itemContext.getResource().with(componentType, resultStack);
        return itemContext.exchange(resultResource, count, transaction);
    }
}
