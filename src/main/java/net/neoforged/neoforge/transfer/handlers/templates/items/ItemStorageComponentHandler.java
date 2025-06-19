/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.items;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageComponent;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageComponentHandler;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import net.neoforged.neoforge.transfer.resources.ItemResource;

public class ItemStorageComponentHandler extends ResourceStorageComponentHandler<ItemResource> {
    public static final Codec<ResourceStorageComponent<ItemResource>> COMPONENT_CODEC = ResourceStorageComponent.codec(ItemResource.OPTIONAL_CODEC, ItemResource::withAmount);
    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceStorageComponent<ItemResource>> COMPONENT_STREAM_CODEC = ResourceStorageComponent.streamCodec(IResourceStack.streamCodec(ItemResource.STREAM_CODEC, ItemResource::withAmount), ItemResource::withAmount);

    public ItemStorageComponentHandler(IItemContext itemContext, DataComponentType<ResourceStorageComponent<ItemResource>> componentType, int size, int indexCapacity) {
        super(itemContext, componentType, size, indexCapacity, ItemResource.EMPTY, ItemResource::withAmount);
    }
}
