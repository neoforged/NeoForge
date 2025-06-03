/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resource;

import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemCapabilityContext;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class SteppedItemContextResourceHandler<T extends IResource> extends ItemContextResourceHandler<T> {
    public SteppedItemContextResourceHandler(IItemCapabilityContext itemContext, DataComponentType<Component<T>> componentType, Component<T> defaultStep) {
        super(itemContext, componentType, defaultStep);
    }

    public SteppedItemContextResourceHandler(IItemCapabilityContext itemContext, DataComponentType<Component<T>> componentType, Component<T> defaultStep, Predicate<T> validator) {
        super(itemContext, componentType, defaultStep, validator);
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isInvalidInquiry(resource, amount)) return 0;

        if (!isValid(0, resource) || !isEmpty()) return 0;

        var singleItemLimit = getSingleItemAmount();
        if (amount < singleItemLimit) return 0;

        return fill(resource, amount / singleItemLimit, transaction, singleItemLimit) * singleItemLimit;
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isInvalidInquiry(resource, amount)) return 0;

        if (isEmpty() || !getResource(0).equals(resource)) return 0;

        var singleItemLimit = getSingleItemAmount();

        if (amount <= singleItemLimit) return 0;

        int extractedCount = amount / singleItemLimit;
        int exchanged = empty(extractedCount, transaction);
        return exchanged * singleItemLimit;
    }

    protected int fill(T resource, int count, TransactionContext transaction, int singleItemLimit) {
        ItemResource filledContainer = itemContext.getResource().with(componentType, new Component<>(new ResourceStack<>(resource, singleItemLimit), singleItemLimit));
        return itemContext.exchange(filledContainer, count, transaction);
    }
}
