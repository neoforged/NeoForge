/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.fluids;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceContainerContents;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.resources.UnsafeResourceUtils;

// Not quite ready for review, but you are welcome to look over it if you wish. Missing docs but that will be coming next.
public class FluidResourceContainerContents {
    public static final ResourceContainerContents<FluidResource> EMPTY = ResourceContainerContents.emptyOf(FluidResource.EMPTY, FluidResource::withAmount, FluidResourceContainerContents::getHoverName);

    public static final Codec<ResourceContainerContents<FluidResource>> CODEC = ResourceContainerContents.Index.codec(FluidResource.CODEC, FluidResource.EMPTY, FluidResource::withAmount)
            .sizeLimitedListOf(256)
            .xmap(FluidResourceContainerContents::fromIndices, ResourceContainerContents::asSlots);

    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceContainerContents<FluidResource>> STREAM_CODEC = IResourceStack.streamCodec(FluidResource.STREAM_CODEC, FluidResource::withAmount)
            .apply(ByteBufCodecs.list(256))
            .map(FluidResourceContainerContents::fromResourceStacks, ResourceContainerContents::getCopyOfList);

    public static ResourceContainerContents<FluidResource> fromResourceStacks(List<ResourceStack<FluidResource>> resourceStackList) {
        return ResourceContainerContents.fromResourceStacks(resourceStackList, FluidResource.EMPTY, FluidResource::withAmount, FluidResourceContainerContents::getHoverName, EMPTY);
    }

    private static ResourceContainerContents<FluidResource> fromIndices(List<ResourceContainerContents.Index<FluidResource>> indexList) {
        return ResourceContainerContents.fromIndices(indexList, FluidResource.EMPTY, FluidResource::withAmount, FluidResourceContainerContents::getHoverName, EMPTY);
    }

    private static Component getHoverName(ResourceStack<FluidResource> resource) {
        return UnsafeResourceUtils.innerStackOf(resource.resource()).getHoverName();
    }
}
