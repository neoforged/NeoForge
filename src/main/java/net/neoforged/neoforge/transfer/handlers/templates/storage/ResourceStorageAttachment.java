/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.storage;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import net.neoforged.neoforge.transfer.resources.ResourceStack;

/**
 * Data structure used to store a list of resource stacks that can be serialized either by Component or DataAttachment
 */
public final class ResourceStorageAttachment<T extends IResource> implements IResourceData<T> {
    private final NonNullList<MutableResourceStack<T>> stacks;
    private final int hashCode;
    private final int size;

    /**
     * @param stacks a list of MutableResourceStacks. The stacks are expected to have their amount mutated internally never externally.
     */
    public ResourceStorageAttachment(NonNullList<MutableResourceStack<T>> stacks) {
        this.stacks = stacks;
        this.size = stacks.size();
        this.hashCode = IResourceStack.hashCode(stacks);
    }

    public static <T extends IResource> Codec<ResourceStorageAttachment<T>> codec(Codec<T> resourceCodec) {
        return NonNullList.codecOf(MutableResourceStack.codec(resourceCodec)).xmap(ResourceStorageAttachment::new, contents -> contents.stacks);
    }

    public static <T extends IResource> StreamCodec<FriendlyByteBuf, ResourceStorageAttachment<T>> streamCodec(StreamCodec<FriendlyByteBuf, MutableResourceStack<T>> resourceCodec) {
        return StreamCodec.of(
                (buf, component) -> buf.writeCollection(component.stacks, resourceCodec),
                buf -> new ResourceStorageAttachment<>(buf.readCollection(NonNullList::<MutableResourceStack<T>>createWithCapacity, resourceCodec)));
    }

    public static <T extends IResource> ResourceStorageAttachment<T> of(int size, T emptyResource) {
        return new ResourceStorageAttachment<>(MutableResourceStack.nonNullListOfSize(size, MutableResourceStack.of(emptyResource, 0)));
    }

    @Override
    public MutableResourceStack<T> get(int index) {
        return stacks.get(index);
    }

    @Override
    public IResourceData<T> modify(int index, T resource, int amount) {
        var current = get(index);
        if (current.resource().equals(resource))
            current.withAmount(amount);
        else
            stacks.set(index, MutableResourceStack.of(resource, amount));
        return this;
    }

    @Override
    public ResourceStorageAttachment<T> attachment() {
        return this;
    }

    @Override
    public ResourceStorageComponent<T> component() {
        var list = NonNullList.<ResourceStack<T>>createWithCapacity(stacks.size());
        for (IResourceStack<T> stack : stacks) {
            list.add(stack.immutable());
        }
        return new ResourceStorageComponent<>(list);
    }

    @SuppressWarnings("EqualsDoesntCheckParameterClass")
    @Override
    public boolean equals(Object otherObj) {
        return IResourceData.equals(this, otherObj);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "ResourceStorageAttachment[" + stacks + ']';
    }
}
