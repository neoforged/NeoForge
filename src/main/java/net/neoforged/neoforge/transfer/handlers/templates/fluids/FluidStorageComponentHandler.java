/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.fluids;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageComponent;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageComponentHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;

public class FluidStorageComponentHandler extends ResourceStorageComponentHandler<FluidResource> {
    public static final Codec<ResourceStorageComponent<FluidResource>> COMPONENT_CODEC = ResourceStorageComponent.codec(FluidResource.OPTIONAL_CODEC, FluidResource::withAmount);
    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceStorageComponent<FluidResource>> COMPONENT_STREAM_CODEC = ResourceStorageComponent.streamCodec(IResourceStack.streamCodec(FluidResource.STREAM_CODEC, FluidResource::withAmount), FluidResource::withAmount);

    public FluidStorageComponentHandler(IItemContext context, DataComponentType<ResourceStorageComponent<FluidResource>> componentType, int size, int indexCapacity) {
        super(context, componentType, size, indexCapacity, FluidResource.EMPTY, FluidResource::withAmount);
    }
}
