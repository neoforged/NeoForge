/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.items;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceContainerContents;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceContainerContentsHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;

public class ItemResourceContainerContentsHandler extends ResourceContainerContentsHandler<ItemResource> {
    public ItemResourceContainerContentsHandler(IItemContext itemContext, DataComponentType<ResourceContainerContents<ItemResource>> componentType, int size, int capacity) {
        super(itemContext, componentType, size, capacity, ItemResource.EMPTY, ItemResource::withAmount, ItemResourceContainerContents.EMPTY);
    }

    //If the desire is to surpass the normal max stack size of an item, overriding this class and method is possible.
    @Override
    public int getCapacity(int index, ItemResource resource) {
        return Math.min(super.getCapacity(index, resource), resource.getMaxStackSize());
    }
}
