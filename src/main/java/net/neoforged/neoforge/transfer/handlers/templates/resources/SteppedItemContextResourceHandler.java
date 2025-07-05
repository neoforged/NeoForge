/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resources;

import com.google.common.math.IntMath;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class SteppedItemContextResourceHandler<T extends IResource> extends ItemContextResourceHandler<T> {
    public SteppedItemContextResourceHandler(IItemContext itemContext, DataComponentType<ResourceStack<T>> componentType, ResourceStack<T> defaultStep, int capacityOfOneItem) {
        super(itemContext, componentType, defaultStep, capacityOfOneItem);
    }

    public SteppedItemContextResourceHandler(IItemContext itemContext, DataComponentType<ResourceStack<T>> componentType, ResourceStack<T> defaultStep, int capacityOfOneItem, Predicate<T> validator) {
        super(itemContext, componentType, defaultStep, capacityOfOneItem);
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        //We don't do partial stepped insertions. It is all or nothing.
        if (amount < capacityOfOneItem) return 0;

        var resourceStack = getStoredResourceStack();
        if (!resourceStack.isEmpty() && !resourceStack.resource().equals(resource)) return 0;

        //we only want to fill when we have a valid resource and no existing amount of the resource already
        //"Stepped" in this case means no values between 0 to capacity.
        if (!isValid(SINGLE_INDEX, resource) || resourceStack.amount() != 0) return 0;

        var filledStack = ResourceStack.of(resource, capacityOfOneItem, emptyStack);
        var filledCount = set(amount / capacityOfOneItem, filledStack, transaction);
        return IntMath.saturatedMultiply(filledCount, capacityOfOneItem);
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        //Must have at least 1 item worth of the resource to extract
        if (amount < capacityOfOneItem) return 0;

        var resourceStack = getStoredResourceStack();
        if (resourceStack.isEmpty() || !resourceStack.resource().equals(resource)) return 0;

        int extractedCount = amount / capacityOfOneItem;
        int exchanged = set(extractedCount, emptyStack, transaction);
        return IntMath.saturatedMultiply(exchanged, capacityOfOneItem);
    }
}
