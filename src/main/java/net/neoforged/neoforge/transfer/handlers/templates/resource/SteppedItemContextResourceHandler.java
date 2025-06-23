/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resource;

import com.google.common.math.IntMath;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class SteppedItemContextResourceHandler<T extends IResource> extends ItemContextResourceHandler<T> {
    public SteppedItemContextResourceHandler(IItemContext itemContext, DataComponentType<ResourceStack<T>> componentType, ResourceStack<T> defaultStep, int capacityOfOneItem) {
        super(itemContext, componentType, defaultStep, capacityOfOneItem);
    }

    public SteppedItemContextResourceHandler(IItemContext itemContext, DataComponentType<ResourceStack<T>> componentType, ResourceStack<T> defaultStep, int capacityOfOneItem, Predicate<T> validator) {
        super(itemContext, componentType, defaultStep, capacityOfOneItem, validator);
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        if (!isValid(0, resource) || !isEmpty()) return 0;

        int capacityOfOneItem = getSingleItemAmount();
        if (amount < capacityOfOneItem) return 0;

        return IntMath.saturatedMultiply(fill(resource, amount / capacityOfOneItem, transaction, capacityOfOneItem), capacityOfOneItem);
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        if (isEmpty() || !getResource(0).equals(resource)) return 0;

        int capacityOfOneItem = getSingleItemAmount();

        if (amount <= capacityOfOneItem) return 0;

        int extractedCount = amount / capacityOfOneItem;
        int exchanged = empty(extractedCount, transaction);
        return IntMath.saturatedMultiply(exchanged, capacityOfOneItem);
    }

    protected int fill(T resource, int count, TransactionContext transaction, int capacityOfOneItem) {
        ItemResource filledContainer = itemContext.getResource().with(componentType, ResourceStack.of(resource, capacityOfOneItem));
        return itemContext.exchange(filledContainer, count, transaction);
    }
}
