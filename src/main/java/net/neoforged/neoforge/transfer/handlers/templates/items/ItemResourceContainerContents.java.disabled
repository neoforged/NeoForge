/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.items;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.handlers.templates.resources.ResourceContainerContents;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;

// Not quite ready for review, but you are welcome to look over it if you wish. Missing docs but that will be coming next.
public class ItemResourceContainerContents {
    public static final ResourceContainerContents<ItemResource> EMPTY = ResourceContainerContents.emptyOf(ItemResource.EMPTY, ItemResource::withAmount, ItemResourceContainerContents::getHoverName);

    public static final Codec<ResourceContainerContents<ItemResource>> CODEC = ResourceContainerContents.Index.codec(ItemResource.CODEC, ItemResource.EMPTY, ItemResource::withAmount)
            //Limit matches vanilla's container contents
            .sizeLimitedListOf(256)
            .xmap(ItemResourceContainerContents::fromSlots, ResourceContainerContents::asSlots);

    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceContainerContents<ItemResource>> STREAM_CODEC = ResourceStack.streamCodec(ItemResource.STREAM_CODEC, ItemResource::withAmount)
            //Limit matches vanilla's container contents
            .apply(ByteBufCodecs.list(256))
            .map(ItemResourceContainerContents::fromItems, ResourceContainerContents::getCopyOfList);

    public static ResourceContainerContents<ItemResource> fromItems(List<ResourceStack<ItemResource>> resourceStackList) {
        return ResourceContainerContents.fromResourceStacks(resourceStackList, ItemResource.EMPTY, ItemResource::withAmount, ItemResourceContainerContents::getHoverName, EMPTY);
    }

    private static ResourceContainerContents<ItemResource> fromSlots(List<ResourceContainerContents.Index<ItemResource>> indexList) {
        return ResourceContainerContents.fromIndices(indexList, ItemResource.EMPTY, ItemResource::withAmount, ItemResourceContainerContents::getHoverName, EMPTY);
    }

    private static Component getHoverName(ResourceStack<ItemResource> stack) {
        return stack.resource().getHoverName();
    }
}
