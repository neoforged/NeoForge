/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.items;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.resource.IResourceStorageData;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageComponent;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ItemStorageComponentHandler extends ResourceStorageHandler<ItemResource> {
    protected final IItemContext itemContext;
    protected final DataComponentType<ResourceStorageComponent<ItemResource>> componentType;

    public ItemStorageComponentHandler(IItemContext itemContext, DataComponentType<ResourceStorageComponent<ItemResource>> componentType, int size, int indexCapacity) {
        super(size, indexCapacity, ItemResource.EMPTY);
        this.itemContext = itemContext;
        this.componentType = componentType;
    }

    @Override
    public IResourceStorageData<ItemResource> getContents() {
        return itemContext.getResource().getOrDefault(componentType, ResourceStorageComponent.of(size, defaultResource));
    }

    @Override
    public void setContents(IResourceStorageData<ItemResource> contents) {
        itemContext.getResource().with(componentType, contents.component());
    }

    @Override
    public int modifyContents(IResourceStorageData<ItemResource> contents, int requestedAmount, int changedAmount, TransactionContext action) {
        if (changedAmount == 0) return 0;
        int exchangeCount = requestedAmount / changedAmount;
        //                            var partial = requestedAmount % changedAmount; // This in theory isn't actually handle here very well.
        ItemResource resourceToExchange = itemContext.getResource().with(componentType, contents.component());
        int result = itemContext.exchange(resourceToExchange, exchangeCount, action);
        return result * changedAmount;
    }
}
