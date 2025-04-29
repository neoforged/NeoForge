/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.storage;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import net.neoforged.neoforge.transfer.resources.ResourceStack;

/**
 * An immutable data structure used to store a list of resources and their amounts that can be serialized as a DataComponent. This is ideal when working with ItemStacks to hold the data.
 */
public final class ResourceStorageComponent<T extends IResource> implements IResourceData<T> {
    private final NonNullList<ResourceStack<T>> stacks;
    private final int size;
    private final int hashCode;

    /**
     * Intended to be used as a data component on an ItemStack. This stores an immutable list, and any changes needed, create a new list.
     */
    public ResourceStorageComponent(NonNullList<ResourceStack<T>> stacks) {
        this.stacks = stacks;
        this.size = stacks.size();
        this.hashCode = IResourceStack.hashCode(stacks);
    }

    public ResourceStorageComponent(int size, T emptyResource) {
        this(NonNullList.withSize(size, new ResourceStack<>(emptyResource, 0)));
    }

    public static <T extends IResource> Codec<ResourceStorageComponent<T>> codec(Codec<T> resourceCodec) {
        return NonNullList.codecOf(ResourceStack.codec(resourceCodec)).xmap(ResourceStorageComponent::new, contents -> contents.stacks);
    }

    public static <T extends IResource> StreamCodec<RegistryFriendlyByteBuf, ResourceStorageComponent<T>> streamCodec(StreamCodec<RegistryFriendlyByteBuf, ResourceStack<T>> resourceCodec) {
        return resourceCodec.apply(ByteBufCodecs.collection(NonNullList::<ResourceStack<T>>createWithCapacity)).map(ResourceStorageComponent::new, component -> component.stacks);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public ResourceStack<T> get(int index) {
        return stacks.get(index);
    }

    @Override
    public ResourceStorageComponent<T> modify(int index, T resource, int amount) {
        var list = NonNullList.<ResourceStack<T>>createWithCapacity(stacks.size());
        for (IResourceStack<T> stack : stacks) {
            list.add(stack.immutable());
        }
        list.set(index, new ResourceStack<>(resource, amount));
        return new ResourceStorageComponent<>(list);
    }

    @Override
    public ResourceStorageComponent<T> component() {
        return this;
    }

    @Override
    public ResourceStorageAttachment<T> attachment() {
        var list = NonNullList.<MutableResourceStack<T>>createWithCapacity(stacks.size());
        for (ResourceStack<T> stack : stacks) {
            list.add(stack.mutable());
        }
        return new ResourceStorageAttachment<>(list);
    }

    @SuppressWarnings("EqualsDoesntCheckParameterClass")
    @Override
    public boolean equals(Object otherObj) {
        return IResourceData.equals(this, otherObj);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "ResourceStorageComponent[" + stacks + ']';
    }
}
