/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resource.temp;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.resources.ItemResource;

public class ItemResourceContainerContentsHandler extends ResourceContainerContentsHandler<ItemResource> {
    public ItemResourceContainerContentsHandler(IItemContext itemContext, DataComponentType<ResourceContainerContents<ItemResource>> componentType, int size) {
        super(itemContext, componentType, size, ItemResource.EMPTY, ItemResource::withAmount, ItemResourceContainerContents.EMPTY);
    }
}
