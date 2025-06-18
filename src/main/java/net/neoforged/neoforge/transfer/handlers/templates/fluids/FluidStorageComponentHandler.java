/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.fluids;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.resource.IResourceStorageData;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageComponent;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class FluidStorageComponentHandler extends ResourceStorageHandler<FluidResource> {
    protected final IItemContext itemContext;
    protected final DataComponentType<ResourceStorageComponent<FluidResource>> componentType;

    public FluidStorageComponentHandler(IItemContext context, DataComponentType<ResourceStorageComponent<FluidResource>> componentType, int size, int indexCapacity) {
        super(size, indexCapacity, FluidResource.EMPTY);
        this.itemContext = context;
        this.componentType = componentType;
    }

    @Override
    public IResourceStorageData<FluidResource> getContents() {
        return itemContext.getResource().getOrDefault(componentType, new ResourceStorageComponent<>(size, FluidResource.EMPTY));
    }

    @Override
    public void setContents(IResourceStorageData<FluidResource> contents) {
        itemContext.getResource().with(componentType, contents.component());
    }

    @Override
    public int modifyContents(IResourceStorageData<FluidResource> contents, int requestedAmount, int changedAmount, TransactionContext context) {
        if (changedAmount == 0) return 0;
        var exchangeCount = requestedAmount / changedAmount;
        var resourceToExchange = itemContext.getResource().with(componentType, contents.component());
        var result = itemContext.exchange(resourceToExchange, exchangeCount, context);
        return result * changedAmount;
    }
}
